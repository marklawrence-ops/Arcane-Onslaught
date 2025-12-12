package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array; // Import LibGDX Array
import com.arcane.onslaught.entities.components.*;
import java.util.Comparator; // Import Comparator

public class RenderSystem extends IteratingSystem {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;

    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<VisualComponent> vm = ComponentMapper.getFor(VisualComponent.class);
    private ComponentMapper<VelocityComponent> vmVel = ComponentMapper.getFor(VelocityComponent.class);

    // --- NEW: Render Queue & Comparator ---
    private Array<Entity> renderQueue;
    private Comparator<Entity> comparator;

    public RenderSystem(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch, OrthographicCamera camera) {
        super(Family.all(PositionComponent.class, VisualComponent.class).get());
        this.shapeRenderer = shapeRenderer;
        this.spriteBatch = spriteBatch;
        this.camera = camera;

        this.renderQueue = new Array<>();

        // Sorter: Lower Z-Index first (Background), Higher Z-Index last (Foreground)
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
        // 1. Collect all entities into a list
        renderQueue.clear();
        for (Entity entity : getEntities()) {
            renderQueue.add(entity);
        }

        // 2. Sort them by Z-Index
        renderQueue.sort(comparator);

        // 3. Render Shapes (Debug/Fallback)
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

        // 4. Render Sprites (Sorted)
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.enableBlending();
        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (spriteBatch.isDrawing()) spriteBatch.end();

        spriteBatch.begin();
        try {
            for (Entity entity : renderQueue) {
                VisualComponent visual = vm.get(entity);
                if (visual.useSprite || visual.animation != null) {
                    renderSprite(entity, deltaTime); // Pass delta for animation
                }
            }
        } finally {
            if (spriteBatch.isDrawing()) {
                spriteBatch.end();
            }
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Empty
    }

    private void renderShape(Entity entity) {
        PositionComponent pos = pm.get(entity);
        VisualComponent visual = vm.get(entity);

        shapeRenderer.setColor(visual.color);
        shapeRenderer.rect(
            pos.position.x - visual.width / 2,
            pos.position.y - visual.height / 2,
            visual.width,
            visual.height
        );
    }

    private void renderSprite(Entity entity, float deltaTime) {
        PositionComponent pos = pm.get(entity);
        VisualComponent visual = vm.get(entity);

        // -- ANIMATION SUPPORT --
        if (visual.animation != null) {
            visual.stateTime += deltaTime;
            com.badlogic.gdx.graphics.g2d.TextureRegion region = visual.animation.getKeyFrame(visual.stateTime, visual.isLooping);

            // Handle Flip for Animations
            VelocityComponent vel = vmVel.get(entity);
            boolean flipX = false;
            if (vel != null && vel.velocity.x < 0) flipX = true;

            // Draw Animation Frame
            spriteBatch.draw(region,
                pos.position.x - visual.width / 2,
                pos.position.y - visual.height / 2,
                visual.width / 2, visual.height / 2, // Origin
                visual.width, visual.height, // Size
                1f, 1f, // Scale
                0f // Rotation (Animations usually don't rotate freely like projectiles)
            );
            return;
        }

        // -- STATIC SPRITE SUPPORT --
        if (visual.sprite != null) {
            // Sync Size
            if (visual.sprite.getWidth() != visual.width || visual.sprite.getHeight() != visual.height) {
                visual.sprite.setSize(visual.width, visual.height);
                visual.sprite.setOriginCenter();
            }

            // Flip Logic
            VelocityComponent vel = vmVel.get(entity);
            if (vel != null) {
                if (vel.velocity.x < 0) visual.sprite.setFlip(true, false);
                else if (vel.velocity.x > 0) visual.sprite.setFlip(false, false);
            }

            visual.sprite.setPosition(
                pos.position.x - visual.width / 2,
                pos.position.y - visual.height / 2
            );
            visual.sprite.setColor(visual.color);
            visual.sprite.draw(spriteBatch);
        }
    }
}
