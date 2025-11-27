package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.spells.Spell;
import com.arcane.onslaught.spells.SpellManager;

/**
 * Handles automatic spell casting with multiple spells
 */
public class SpellCastSystem extends IteratingSystem {
    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private SpellManager spellManager;

    public SpellCastSystem(SpellManager spellManager) {
        super(Family.all(PlayerComponent.class, PositionComponent.class).get());
        this.spellManager = spellManager;
    }

    @Override
    public void update(float deltaTime) {
        // Update all spell cooldowns
        spellManager.updateAllSpells(deltaTime);

        // Then cast spells
        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent pos = pm.get(entity);

        // Find nearest enemy
        Entity nearestEnemy = findNearestEnemy(pos.position);

        if (nearestEnemy != null) {
            PositionComponent enemyPos = nearestEnemy.getComponent(PositionComponent.class);

            // Cast all ready spells
            for (Spell spell : spellManager.getActiveSpells()) {
                if (spell.canCast()) {
                    spell.cast(getEngine(), pos.position, enemyPos.position);
                    spell.resetCooldown();
                }
            }
        }
    }

    private Entity findNearestEnemy(Vector2 playerPos) {
        Entity nearest = null;
        float minDist = Float.MAX_VALUE;

        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();

        for (Entity entity : getEngine().getEntitiesFor(enemyFamily)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health == null || !health.isAlive()) continue;

            PositionComponent pos = entity.getComponent(PositionComponent.class);
            float dist = playerPos.dst(pos.position);
            if (dist < minDist) {
                minDist = dist;
                nearest = entity;
            }
        }

        return nearest;
    }

    public SpellManager getSpellManager() {
        return spellManager;
    }
}
