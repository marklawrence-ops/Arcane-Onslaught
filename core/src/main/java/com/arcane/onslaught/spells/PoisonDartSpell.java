package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.spells.Spell;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

// ============================================
// POISON DART - Damage over time
// ============================================

public class PoisonDartSpell extends Spell {
    private float projectileSpeed = 350f;
    private float projectileSize = 10f;
    private float poisonDPS = 5f;
    private float poisonDuration = 3f;

    public PoisonDartSpell() {
        super("Poison Dart", 1.2f, 5f); // Low initial damage + 15 total DOT
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos) {
        Entity projectile = new Entity();

        Vector2 direction = new Vector2(targetPos).sub(playerPos).nor();

        projectile.add(new PositionComponent(playerPos.x, playerPos.y));

        VelocityComponent vel = new VelocityComponent(projectileSpeed);
        vel.velocity.set(direction).scl(projectileSpeed);
        projectile.add(vel);

        projectile.add(new VisualComponent(projectileSize, projectileSize, Color.GREEN));
        projectile.add(new ProjectileComponent(damage, 5f, "poison_dart"));

        // POISON DOT EFFECT
        projectile.add(new PoisonComponent(poisonDPS, poisonDuration));

        engine.addEntity(projectile);
    }
}
