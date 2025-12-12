package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.arcane.onslaught.entities.components.*;

public class RenderSystem extends IteratingSystem {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;

    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<VisualComponent> vm = ComponentMapper.getFor(VisualComponent.class);
    private ComponentMapper<VelocityComponent> vmVel = ComponentMapper.getFor(VelocityComponent.class);

    public RenderSystem(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch, OrthographicCamera camera) {
        super(Family.all(PositionComponent.class, VisualComponent.class).get());
        this.shapeRenderer = shapeRenderer;
        this.spriteBatch = spriteBatch;
        this.camera = camera;
    }

    @Override
    public void update(float deltaTime) {
        // 1. Render Shapes
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        try {
            for (Entity entity : getEntities()) {
                VisualComponent visual = vm.get(entity);
                if (!visual.useSprite) {
                    renderShape(entity);
                }
            }
        } finally {
            shapeRenderer.end();
        }

        // 2. Render Sprites
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.enableBlending();
        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (spriteBatch.isDrawing()) spriteBatch.end();

        spriteBatch.begin();
        try {
            for (Entity entity : getEntities()) {
                VisualComponent visual = vm.get(entity);
                if (visual.useSprite) {
                    renderSprite(entity);
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

    private void renderSprite(Entity entity) {
        PositionComponent pos = pm.get(entity);
        VisualComponent visual = vm.get(entity);

        if (visual.sprite != null) {
            // --- FIX: SYNC SPRITE SIZE WITH COMPONENT ---
            // This ensures "Projectile Size" upgrades actually visually scale the sprite
            if (visual.sprite.getWidth() != visual.width || visual.sprite.getHeight() != visual.height) {
                visual.sprite.setSize(visual.width, visual.height);
            }

            // --- FLIP LOGIC ---
            VelocityComponent vel = vmVel.get(entity);
            if (vel != null) {
                if (vel.velocity.x < 0) {
                    visual.sprite.setFlip(true, false);
                }
                else if (vel.velocity.x > 0) {
                    visual.sprite.setFlip(false, false);
                }
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
