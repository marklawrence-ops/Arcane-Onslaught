package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.UpgradeHelper;
import com.arcane.onslaught.utils.TextureManager; // Import
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

public class LightningBoltSpell extends Spell {
    private float projectileSpeed = 500f;
    private float projectileSize = 18f;
    private int chainCount = 3;
    private float chainRange = 150f;

    public LightningBoltSpell() {
        super("Lightning Bolt", 0.8f, 10f);
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
            TextureManager.getInstance().getTexture("lightning")));

        projectile.add(new ProjectileComponent(damage, 3f, "lightning"));

        projectile.add(new ChainComponent(chainCount, chainRange, damage * 0.7f));

        UpgradeHelper.applyProjectileUpgrades(projectile, playerBuild, this.name);
        engine.addEntity(projectile);
    }
}
