package com.arcane.onslaught.entities.systems;

import com.arcane.onslaught.utils.SoundManager;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.spells.Spell;
import com.arcane.onslaught.spells.SpellManager;
import com.arcane.onslaught.upgrades.PlayerBuild;

public class SpellCastSystem extends IteratingSystem {
    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private SpellManager spellManager;
    private PlayerBuild playerBuild;

    public SpellCastSystem(SpellManager spellManager, PlayerBuild playerBuild) {
        super(Family.all(PlayerComponent.class, PositionComponent.class).get());
        this.spellManager = spellManager;
        this.playerBuild = playerBuild;
    }

    @Override
    public void update(float deltaTime) {
        spellManager.updateAllSpells(deltaTime);
        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent pos = pm.get(entity);
        Entity nearestEnemy = findNearestEnemy(pos.position);

        if (nearestEnemy != null) {
            PositionComponent enemyPos = nearestEnemy.getComponent(PositionComponent.class);

            for (Spell spell : spellManager.getActiveSpells()) {
                if (spell.canCast()) {
                    SoundManager.getInstance().play("cast", 1.0f + (float)Math.random() * 0.2f);
                    // --- PASS 'entity' (Caster) HERE ---
                    spell.cast(getEngine(), entity, pos.position, enemyPos.position, playerBuild);
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

    public SpellManager getSpellManager() { return spellManager; }
}
