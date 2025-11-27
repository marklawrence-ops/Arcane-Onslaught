package com.arcane.onslaught.spells;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.spells.Spell;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

// ============================================
// FIREBALL - Slower but more damage
// ============================================

public class FireballSpell extends Spell {
    private float projectileSpeed = 200f;
    private float projectileSize = 24f;
    private float explosionRadius = 80f;

    public FireballSpell() {
        super("Fireball", 2.5f, 25f);
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos) {
        Entity projectile = new Entity();

        Vector2 direction = new Vector2(targetPos).sub(playerPos).nor();

        projectile.add(new PositionComponent(playerPos.x, playerPos.y));

        VelocityComponent vel = new VelocityComponent(projectileSpeed);
        vel.velocity.set(direction).scl(projectileSpeed);
        projectile.add(vel);

        projectile.add(new VisualComponent(projectileSize, projectileSize, Color.ORANGE));
        projectile.add(new ProjectileComponent(damage, 5f, "fireball"));

        // AOE EXPLOSION EFFECT
        projectile.add(new ExplosiveComponent(explosionRadius, damage * 0.5f));

        engine.addEntity(projectile);
    }
}
