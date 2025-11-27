package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.spells.Spell;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

// ============================================
// LIGHTNING BOLT - Fast, low damage
// ============================================

public class LightningBoltSpell extends Spell {
    private float projectileSpeed = 500f;
    private float projectileSize = 12f;
    private int chainCount = 3;
    private float chainRange = 150f;

    public LightningBoltSpell() {
        super("Lightning Bolt", 0.8f, 8f);
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos) {
        Entity projectile = new Entity();

        Vector2 direction = new Vector2(targetPos).sub(playerPos).nor();

        projectile.add(new PositionComponent(playerPos.x, playerPos.y));

        VelocityComponent vel = new VelocityComponent(projectileSpeed);
        vel.velocity.set(direction).scl(projectileSpeed);
        projectile.add(vel);

        projectile.add(new VisualComponent(projectileSize, projectileSize, Color.YELLOW));
        projectile.add(new ProjectileComponent(damage, 3f, "lightning"));

        // CHAIN LIGHTNING EFFECT
        projectile.add(new ChainComponent(chainCount, chainRange, damage * 0.7f));

        engine.addEntity(projectile);
    }
}
