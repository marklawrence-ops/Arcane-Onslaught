package com.arcane.onslaught.spells;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.entities.components.PositionComponent;
import com.arcane.onslaught.entities.components.ProjectileComponent;
import com.arcane.onslaught.entities.components.VelocityComponent;
import com.arcane.onslaught.entities.components.VisualComponent;

// ============================================
// ARCANE MISSILES - 3 weak projectiles
// ============================================

public class ArcaneMissilesSpell extends Spell {
    private float projectileSpeed = 320f;
    private float projectileSize = 10f;
    private int missileCount = 3;
    private int pierceCount = 2;

    public ArcaneMissilesSpell() {
        super("Arcane Missiles", 2.0f, 6f); // 6 damage per missile = 18 total
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos) {
        Vector2 baseDirection = new Vector2(targetPos).sub(playerPos).nor();

        // Fire missiles in a spread pattern
        for (int i = 0; i < missileCount; i++) {
            float angleOffset = (i - 1) * 15f; // -15, 0, +15 degrees
            Vector2 direction = baseDirection.cpy().rotateDeg(angleOffset);

            Entity projectile = new Entity();
            projectile.add(new PositionComponent(playerPos.x, playerPos.y));

            VelocityComponent vel = new VelocityComponent(projectileSpeed);
            vel.velocity.set(direction).scl(projectileSpeed);
            projectile.add(vel);

            projectile.add(new VisualComponent(projectileSize, projectileSize, Color.PURPLE));
            projectile.add(new ProjectileComponent(damage, 5f, "arcane_missile"));

            // PIERCE EFFECT
            projectile.add(new PierceComponent(pierceCount));

            engine.addEntity(projectile);
        }
    }
}
