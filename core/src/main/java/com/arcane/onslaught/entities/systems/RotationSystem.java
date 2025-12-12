package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.arcane.onslaught.entities.components.*;

public class RotationSystem extends IteratingSystem {
    private ComponentMapper<VisualComponent> vm = ComponentMapper.getFor(VisualComponent.class);
    private ComponentMapper<RotationComponent> rm = ComponentMapper.getFor(RotationComponent.class);

    public RotationSystem() {
        super(Family.all(VisualComponent.class, RotationComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        VisualComponent vis = vm.get(entity);
        RotationComponent rot = rm.get(entity);

        if (vis.sprite != null) {
            float currentRot = vis.sprite.getRotation();
            vis.sprite.setRotation(currentRot + (rot.degreesPerSecond * deltaTime));
        }
    }
}
