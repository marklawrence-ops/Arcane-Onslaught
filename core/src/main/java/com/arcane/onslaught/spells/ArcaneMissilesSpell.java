package com.arcane.onslaught.spells;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.UpgradeHelper;
import com.arcane.onslaught.utils.TextureManager;

public class ArcaneMissilesSpell extends Spell {
    private float projectileSpeed = 320f;
    private float projectileSize = 14f;
    private int baseMissileCount = 3; // Renamed to clarify
    private int pierceCount = 2;

    public ArcaneMissilesSpell() {
        super("Arcane Missiles", 2.0f, 8f);
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos, PlayerBuild playerBuild) {
        Vector2 baseDirection = new Vector2(targetPos).sub(playerPos).nor();
        if (baseDirection.isZero()) baseDirection.set(1, 0);

        // Base 3 + Multishot Bonus (e.g., +1 per upgrade)
        // Note: getProjectileCount returns (1 + stacks). Since we start at 3, we add (stacks).
        int bonus = UpgradeHelper.getProjectileCount(playerBuild) - 1;
        int totalMissiles = baseMissileCount + bonus;

        float spread = 15f;

        for (int i = 0; i < totalMissiles; i++) {
            float angleOffset = spread * (i - (totalMissiles - 1) / 2f);
            Vector2 direction = baseDirection.cpy().rotateDeg(angleOffset);

            Entity projectile = new Entity();
            projectile.add(new PositionComponent(playerPos.x, playerPos.y));

            VelocityComponent vel = new VelocityComponent(projectileSpeed);
            vel.velocity.set(direction).scl(projectileSpeed);
            projectile.add(vel);

            projectile.add(new VisualComponent(projectileSize, projectileSize,
                TextureManager.getInstance().getTexture("arcane_missile"))); // Ensure this matches TextureManager key

            projectile.add(new ProjectileComponent(damage, 5f, "arcane_missile"));
            projectile.add(new PierceComponent(pierceCount));

            UpgradeHelper.applyProjectileUpgrades(projectile, playerBuild, this.name);
            engine.addEntity(projectile);
        }
    }
}
