package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.UpgradeHelper;
import com.arcane.onslaught.utils.TextureManager;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

public class IceShardSpell extends Spell {
    private float projectileSpeed = 280f;
    private float projectileSize = 20f;
    private float slowAmount = 0.5f;
    private float slowDuration = 2f;

    public IceShardSpell() {
        super("Ice Shard", 1.5f, 18f);
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos, PlayerBuild playerBuild) {
        Vector2 baseDirection = new Vector2(targetPos).sub(playerPos).nor();
        if (baseDirection.isZero()) baseDirection.set(1, 0);

        int count = UpgradeHelper.getProjectileCount(playerBuild);
        float spread = 20f;

        for (int i = 0; i < count; i++) {
            float angleOffset = spread * (i - (count - 1) / 2f);
            Vector2 direction = baseDirection.cpy().rotateDeg(angleOffset);

            Entity projectile = new Entity();
            projectile.add(new PositionComponent(playerPos.x, playerPos.y));

            VelocityComponent vel = new VelocityComponent(projectileSpeed);
            vel.velocity.set(direction).scl(projectileSpeed);
            projectile.add(vel);

            projectile.add(new VisualComponent(projectileSize, projectileSize,
                TextureManager.getInstance().getTexture("ice_shard")));

            projectile.add(new ProjectileComponent(damage, 5f, "ice_shard"));
            projectile.add(new SlowComponent(slowAmount, slowDuration));

            UpgradeHelper.applyProjectileUpgrades(projectile, playerBuild, this.name);
            engine.addEntity(projectile);
        }
    }
}
