package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.PositionComponent;
import com.arcane.onslaught.entities.components.ProjectileComponent;
import com.arcane.onslaught.entities.components.VelocityComponent;
import com.arcane.onslaught.entities.components.VisualComponent;
import com.arcane.onslaught.spells.Spell;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

// ============================================
// MAGIC BOLT - Basic single projectile
// ============================================

public class MagicBoltSpell extends Spell {
    private float projectileSpeed = 300f;
    private float projectileSize = 16f;

    public MagicBoltSpell() {
        super("Magic Bolt", 1.5f, 10f);
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos) {
        Entity projectile = new Entity();

        Vector2 direction = new Vector2(targetPos).sub(playerPos).nor();

        projectile.add(new PositionComponent(playerPos.x, playerPos.y));

        VelocityComponent vel = new VelocityComponent(projectileSpeed);
        vel.velocity.set(direction).scl(projectileSpeed);
        projectile.add(vel);

        projectile.add(new VisualComponent(projectileSize, projectileSize, Color.CYAN));
        projectile.add(new ProjectileComponent(damage, 5f, "magic_bolt"));

        engine.addEntity(projectile);
    }
}
