package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;

/**
 * Handles poison damage over time on enemies
 */
public class PoisonSystem extends IteratingSystem {
    private ComponentMapper<PoisonedComponent> poisonMapper = ComponentMapper.getFor(PoisonedComponent.class);
    private ComponentMapper<HealthComponent> healthMapper = ComponentMapper.getFor(HealthComponent.class);

    public PoisonSystem() {
        super(Family.all(PoisonedComponent.class, HealthComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PoisonedComponent poison = poisonMapper.get(entity);
        HealthComponent health = healthMapper.get(entity);

        poison.timeRemaining -= deltaTime;
        poison.tickTimer += deltaTime;

        // Damage every 0.5 seconds
        if (poison.tickTimer >= 0.5f) {
            float damage = poison.damagePerSecond * 0.5f;
            health.damage(damage);
            poison.tickTimer -= 0.5f;
        }

        // Remove poison when expired
        if (poison.timeRemaining <= 0) {
            entity.remove(PoisonedComponent.class);
        }
    }
}
