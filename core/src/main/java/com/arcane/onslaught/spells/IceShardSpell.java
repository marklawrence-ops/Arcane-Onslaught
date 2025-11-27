package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.spells.Spell;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

// ============================================
// ICE SHARD - Slower enemies on hit
// ============================================

public class IceShardSpell extends Spell {
    private float projectileSpeed = 280f;
    private float projectileSize = 14f;
    private float slowAmount = 0.5f; // 50% slow
    private float slowDuration = 2f;

    public IceShardSpell() {
        super("Ice Shard", 1.8f, 12f);
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos) {
        Entity projectile = new Entity();

        Vector2 direction = new Vector2(targetPos).sub(playerPos).nor();

        projectile.add(new PositionComponent(playerPos.x, playerPos.y));

        VelocityComponent vel = new VelocityComponent(projectileSpeed);
        vel.velocity.set(direction).scl(projectileSpeed);
        projectile.add(vel);

        projectile.add(new VisualComponent(projectileSize, projectileSize, new Color(0.5f, 0.8f, 1f, 1f)));
        projectile.add(new ProjectileComponent(damage, 5f, "ice_shard"));

        // SLOW EFFECT
        projectile.add(new SlowComponent(slowAmount, slowDuration));

        engine.addEntity(projectile);
    }
}
