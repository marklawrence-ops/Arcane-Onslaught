package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;

/**
 * Handles slow effects on enemies
 */
public class SlowSystem extends IteratingSystem {
    private ComponentMapper<SlowedComponent> slowMapper = ComponentMapper.getFor(SlowedComponent.class);
    private ComponentMapper<VelocityComponent> velMapper = ComponentMapper.getFor(VelocityComponent.class);

    public SlowSystem() {
        super(Family.all(SlowedComponent.class, VelocityComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SlowedComponent slow = slowMapper.get(entity);
        VelocityComponent vel = velMapper.get(entity);

        slow.duration -= deltaTime;

        // Apply slow
        vel.maxSpeed = slow.originalSpeed * (1f - slow.slowAmount);

        // Remove slow when expired
        if (slow.duration <= 0) {
            vel.maxSpeed = slow.originalSpeed; // Restore speed
            entity.remove(SlowedComponent.class);
        }
    }
}
