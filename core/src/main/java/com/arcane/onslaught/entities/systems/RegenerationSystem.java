package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.arcane.onslaught.entities.components.*;

/**
 * Handles health regeneration for entities
 */
public class RegenerationSystem extends IteratingSystem {
    private ComponentMapper<RegenerationComponent> regenMapper = ComponentMapper.getFor(RegenerationComponent.class);
    private ComponentMapper<HealthComponent> healthMapper = ComponentMapper.getFor(HealthComponent.class);

    public RegenerationSystem() {
        super(Family.all(RegenerationComponent.class, HealthComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        RegenerationComponent regen = regenMapper.get(entity);
        HealthComponent health = healthMapper.get(entity);

        regen.timeSinceLastHeal += deltaTime;

        // Heal at intervals
        if (regen.timeSinceLastHeal >= regen.healInterval) {
            if (health.currentHealth < health.maxHealth) {
                health.heal(regen.healAmount);
                regen.timeSinceLastHeal = 0;
            }
        }
    }
}
