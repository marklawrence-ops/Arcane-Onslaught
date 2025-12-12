package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.arcane.onslaught.entities.components.*;

public class PoisonSystem extends IteratingSystem {
    private ComponentMapper<PoisonedComponent> pm = ComponentMapper.getFor(PoisonedComponent.class);
    private ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);
    private ComponentMapper<PositionComponent> posMapper = ComponentMapper.getFor(PositionComponent.class);

    public PoisonSystem() {
        super(Family.all(PoisonedComponent.class, HealthComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PoisonedComponent poison = pm.get(entity);
        HealthComponent health = hm.get(entity);

        // Tick down duration
        poison.timeRemaining -= deltaTime;
        poison.tickTimer += deltaTime;

        // Apply damage every 0.5 seconds (prevents flooding the screen with numbers)
        if (poison.tickTimer >= 0.5f) {
            float damageToDeal = poison.damagePerSecond * 0.5f;
            health.damage(damageToDeal);
            poison.tickTimer = 0;

            // Visual feedback (Tiny green numbers)
            PositionComponent pos = posMapper.get(entity);
            if (pos != null) {
                spawnDamageNumber(pos.position.x, pos.position.y, damageToDeal);
            }
        }

        // Remove poison when time is up
        if (poison.timeRemaining <= 0) {
            entity.remove(PoisonedComponent.class);
        }
    }

    private void spawnDamageNumber(float x, float y, float amount) {
        Entity indicator = new Entity();
        indicator.add(new PositionComponent(x, y + 10)); // Slightly offset
        indicator.add(new DamageIndicatorComponent(amount, false));
        // Force green color for poison
        indicator.getComponent(DamageIndicatorComponent.class).color = Color.CHARTREUSE;

        VelocityComponent vel = new VelocityComponent(0);
        vel.velocity.set(0, 30f); // Float up slowly
        indicator.add(vel);

        getEngine().addEntity(indicator);
    }
}
