package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.UpgradeHelper;
import com.arcane.onslaught.utils.TextureManager; // Import
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

public class PoisonDartSpell extends Spell {
    private float projectileSpeed = 350f;
    private float projectileSize = 16f;
    private float poisonDPS = 5f;
    private float poisonDuration = 3f;

    public PoisonDartSpell() {
        super("Poison Dart", 1.2f, 8f);
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
            TextureManager.getInstance().getTexture("poison")));

        projectile.add(new ProjectileComponent(damage, 5f, "poison_dart"));

        projectile.add(new PoisonComponent(poisonDPS, poisonDuration));

        UpgradeHelper.applyProjectileUpgrades(projectile, playerBuild, this.name);
        engine.addEntity(projectile);
    }
}
