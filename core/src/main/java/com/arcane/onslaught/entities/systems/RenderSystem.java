package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.arcane.onslaught.entities.components.*;

public class RenderSystem extends IteratingSystem {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<VisualComponent> vm = ComponentMapper.getFor(VisualComponent.class);

    public RenderSystem(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        super(Family.all(PositionComponent.class, VisualComponent.class).get());
        this.shapeRenderer = shapeRenderer;
        this.camera = camera;
    }

    @Override
    public void update(float deltaTime) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        super.update(deltaTime);
        shapeRenderer.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
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
}
