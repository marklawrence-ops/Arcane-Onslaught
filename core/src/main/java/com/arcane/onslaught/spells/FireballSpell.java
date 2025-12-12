package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.UpgradeHelper;
import com.arcane.onslaught.utils.TextureManager; // Import this
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

public class FireballSpell extends Spell {
    private float projectileSpeed = 200f;
    private float projectileSize = 30f; // Increased size for sprite
    private float explosionRadius = 80f;

    public FireballSpell() {
        super("Fireball", 2.5f, 30f);
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos, PlayerBuild playerBuild) {
        Entity projectile = new Entity();

        Vector2 direction = new Vector2(targetPos).sub(playerPos).nor();

        projectile.add(new PositionComponent(playerPos.x, playerPos.y));

        VelocityComponent vel = new VelocityComponent(projectileSpeed);
        vel.velocity.set(direction).scl(projectileSpeed);
        projectile.add(vel);

        // --- FIX: Use Texture ---
        projectile.add(new VisualComponent(projectileSize, projectileSize,
            TextureManager.getInstance().getTexture("fireball")));

        projectile.add(new ProjectileComponent(damage, 5f, "fireball"));

        // AOE EXPLOSION EFFECT
        projectile.add(new ExplosiveComponent(explosionRadius, damage * 0.5f));

        UpgradeHelper.applyProjectileUpgrades(projectile, playerBuild, this.name);
        engine.addEntity(projectile);
    }
}
