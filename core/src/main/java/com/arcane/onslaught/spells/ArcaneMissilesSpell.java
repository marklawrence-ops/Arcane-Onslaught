package com.arcane.onslaught.spells;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.UpgradeHelper;
import com.arcane.onslaught.utils.TextureManager; // Import

public class ArcaneMissilesSpell extends Spell {
    private float projectileSpeed = 320f;
    private float projectileSize = 14f;
    private int missileCount = 3;
    private int pierceCount = 2;

    public ArcaneMissilesSpell() {
        super("Arcane Missiles", 2.0f, 8f);
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos, PlayerBuild playerBuild) {
        Vector2 baseDirection = new Vector2(targetPos).sub(playerPos).nor();

        for (int i = 0; i < missileCount; i++) {
            float angleOffset = (i - 1) * 15f;
            Vector2 direction = baseDirection.cpy().rotateDeg(angleOffset);

            Entity projectile = new Entity();
            projectile.add(new PositionComponent(playerPos.x, playerPos.y));

            VelocityComponent vel = new VelocityComponent(projectileSpeed);
            vel.velocity.set(direction).scl(projectileSpeed);
            projectile.add(vel);

            // --- FIX: Use Texture ---
            // Re-using "magic_bolt" since we don't have "arcane_missile" loaded yet
            projectile.add(new VisualComponent(projectileSize, projectileSize,
                TextureManager.getInstance().getTexture("arcane_missile")));

            projectile.add(new ProjectileComponent(damage, 5f, "arcane_missile"));

            projectile.add(new PierceComponent(pierceCount));

            UpgradeHelper.applyProjectileUpgrades(projectile, playerBuild, this.name);
            engine.addEntity(projectile);
        }
    }
}
