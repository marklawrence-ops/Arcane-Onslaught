package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.arcane.onslaught.entities.components.*;
import java.util.Comparator;

public class RenderSystem extends IteratingSystem {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;

    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<VisualComponent> vm = ComponentMapper.getFor(VisualComponent.class);
    private ComponentMapper<VelocityComponent> vmVel = ComponentMapper.getFor(VelocityComponent.class);
    private ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);

    private Array<Entity> renderQueue;
    private Comparator<Entity> comparator;

    public RenderSystem(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch, OrthographicCamera camera) {
        super(Family.all(PositionComponent.class, VisualComponent.class).get());
        this.shapeRenderer = shapeRenderer;
        this.spriteBatch = spriteBatch;
        this.camera = camera;

        this.renderQueue = new Array<>();
        this.comparator = new Comparator<Entity>() {
            @Override
            public int compare(Entity entityA, Entity entityB) {
                VisualComponent visA = vm.get(entityA);
                VisualComponent visB = vm.get(entityB);
                return Integer.compare(visA.zIndex, visB.zIndex);
            }
        };
    }

    @Override
    public void update(float deltaTime) {
        renderQueue.clear();
        for (Entity entity : getEntities()) {
            renderQueue.add(entity);
        }
        renderQueue.sort(comparator);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        try {
            for (Entity entity : renderQueue) {
                VisualComponent visual = vm.get(entity);
                if (!visual.useSprite && visual.animation == null) {
                    renderShape(entity);
                }
            }
        } finally {
            shapeRenderer.end();
        }

        spriteBatch.setColor(Color.WHITE);
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.enableBlending();
        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (spriteBatch.isDrawing()) spriteBatch.end();

        spriteBatch.begin();
        try {
            for (Entity entity : renderQueue) {
                VisualComponent visual = vm.get(entity);
                HealthComponent health = hm.get(entity);
                if (health != null && health.hitFlashTimer > 0) {
                    health.hitFlashTimer -= deltaTime;
                }

                if (visual.useSprite || visual.animation != null) {
                    renderSprite(entity, deltaTime);
                }
            }
        } finally {
            if (spriteBatch.isDrawing()) spriteBatch.end();
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {}

    private void renderShape(Entity entity) {
        PositionComponent pos = pm.get(entity);
        VisualComponent visual = vm.get(entity);
        shapeRenderer.setColor(visual.color);
        shapeRenderer.rect(pos.position.x - visual.width / 2, pos.position.y - visual.height / 2, visual.width, visual.height);
    }

    private void renderSprite(Entity entity, float deltaTime) {
        PositionComponent pos = pm.get(entity);
        VisualComponent visual = vm.get(entity);
        VelocityComponent vel = vmVel.get(entity);
        HealthComponent health = hm.get(entity);

        float drawX = pos.position.x - visual.width / 2;
        float drawY = pos.position.y - visual.height / 2;
        float rotation = 0f;

        // 1. Bobbing
        if (visual.isBobbing) {
            visual.bobTimer += deltaTime;
            boolean isMoving = (vel != null && (vel.velocity.x != 0 || vel.velocity.y != 0));
            float currentSpeed = isMoving ? visual.bobSpeed : visual.bobSpeed * 0.4f;
            float currentHeight = isMoving ? visual.bobHeight : visual.bobHeight * 0.5f;
            drawY += MathUtils.sin(visual.bobTimer * currentSpeed) * currentHeight;

            if (vel != null && isMoving) {
                float tiltStrength = 5f;
                if (vel.velocity.x > 0) rotation = -tiltStrength;
                else if (vel.velocity.x < 0) rotation = tiltStrength;
            }
        }

        TextureRegion region = null;
        if (visual.animation != null) {
            visual.stateTime += deltaTime;
            region = visual.animation.getKeyFrame(visual.stateTime, visual.isLooping);
        } else if (visual.sprite != null) {
            region = visual.sprite;
        }
        if (region == null) return;

        // 2. Calculate Final Alpha
        float fadeAlpha = 1.0f;
        if (visual.isFadingIn) {
            visual.fadeInTimer += deltaTime;
            if (visual.fadeInDuration > 0) {
                fadeAlpha = MathUtils.clamp(visual.fadeInTimer / visual.fadeInDuration, 0f, 1f);
            } else {
                fadeAlpha = 1f;
            }
            if (visual.fadeInTimer >= visual.fadeInDuration) {
                visual.isFadingIn = false;
                fadeAlpha = 1.0f;
            }
        }

        // --- CRITICAL FIX START ---
        // Capture the "Base Alpha" (e.g., 0.0 for spawning player, 1.0 for enemy)
        float baseAlpha = (visual.sprite != null) ? visual.sprite.getColor().a : 1f;

        // Combine with fade effect
        float finalAlpha = fadeAlpha * baseAlpha;
        // --------------------------

        // 3. Draw Shadow
        if (visual.isBobbing) {
            spriteBatch.setColor(0f, 0f, 0f, 0.4f * finalAlpha);
            float shadowW = visual.width * 0.8f;
            float shadowH = visual.height * 0.25f;
            spriteBatch.draw(region,
                pos.position.x - shadowW / 2,
                pos.position.y - visual.height / 2 - 2,
                shadowW, shadowH
            );
            spriteBatch.setColor(Color.WHITE);
        }

        boolean flipX = false;
        if (region instanceof TextureRegion) {
            if (vel != null && vel.velocity.x < 0 && !region.isFlipX()) flipX = true;
        }

        // 4. Draw Sprite
        if (visual.sprite != null && visual.animation == null) {
            // Set temporary draw color
            if (health != null && health.hitFlashTimer > 0) {
                visual.sprite.setColor(1f, 0f, 0f, finalAlpha);
            } else {
                visual.sprite.setColor(visual.color.r, visual.color.g, visual.color.b, finalAlpha);
            }

            visual.sprite.setPosition(drawX, drawY);
            visual.sprite.setRotation(rotation);
            if (vel != null) {
                if (vel.velocity.x < 0) visual.sprite.setFlip(true, false);
                else if (vel.velocity.x > 0) visual.sprite.setFlip(false, false);
            }
            visual.sprite.draw(spriteBatch);

            // --- CRITICAL FIX END ---
            // Restore the Original Alpha so we don't overwrite logic from other systems
            visual.sprite.setAlpha(baseAlpha);
            // ------------------------
        }
        else {
            if (health != null && health.hitFlashTimer > 0) {
                spriteBatch.setColor(1f, 0f, 0f, finalAlpha);
            } else {
                spriteBatch.setColor(visual.color.r, visual.color.g, visual.color.b, finalAlpha);
            }

            spriteBatch.draw(region,
                drawX, drawY,
                visual.width / 2, visual.height / 2,
                visual.width, visual.height,
                flipX ? -1f : 1f, 1f,
                rotation
            );
            spriteBatch.setColor(Color.WHITE);
        }
    }
}
