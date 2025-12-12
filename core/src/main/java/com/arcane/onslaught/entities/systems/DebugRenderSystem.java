package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.arcane.onslaught.entities.components.*;

public class DebugRenderSystem extends IteratingSystem {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<CollisionComponent> cm = ComponentMapper.getFor(CollisionComponent.class);
    private ComponentMapper<VisualComponent> vm = ComponentMapper.getFor(VisualComponent.class);

    public boolean isDebugMode = false;

    public DebugRenderSystem(OrthographicCamera camera, ShapeRenderer shapeRenderer) {
        super(Family.all(PositionComponent.class).get());
        this.camera = camera;
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    public void update(float deltaTime) {
        if (!isDebugMode) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (Entity entity : getEntities()) {
            PositionComponent pos = pm.get(entity);
            if (pos == null) continue;

            float radius = 10f; // Default fallback

            CollisionComponent col = cm.get(entity);
            VisualComponent vis = vm.get(entity);

            if (col != null) {
                shapeRenderer.setColor(Color.RED); // Explicit hitbox = Red
                radius = col.radius;
            } else if (vis != null) {
                shapeRenderer.setColor(Color.YELLOW); // Implicit hitbox = Yellow
                // Fallback logic matching CollisionSystem
                radius = Math.min(vis.width, vis.height) / 2.5f;
            }

            shapeRenderer.circle(pos.position.x, pos.position.y, radius);
        }

        shapeRenderer.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Processing happens in update()
    }
}
