package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.UpgradeHelper;
import com.arcane.onslaught.utils.TextureManager;
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
    public void cast(Engine engine, Entity caster, Vector2 playerPos, Vector2 targetPos, PlayerBuild playerBuild) {
        Vector2 baseDirection = new Vector2(targetPos).sub(playerPos).nor();
        if (baseDirection.isZero()) baseDirection.set(1, 0);

        int count = UpgradeHelper.getProjectileCount(playerBuild);
        float spread = 15f;

        for (int i = 0; i < count; i++) {
            float angleOffset = spread * (i - (count - 1) / 2f);
            Vector2 direction = baseDirection.cpy().rotateDeg(angleOffset);

            Entity projectile = new Entity();
            projectile.add(new PositionComponent(playerPos.x, playerPos.y));

            VelocityComponent vel = new VelocityComponent(projectileSpeed);
            vel.velocity.set(direction).scl(projectileSpeed);
            projectile.add(vel);

            projectile.add(new VisualComponent(projectileSize, projectileSize,
                TextureManager.getInstance().getTexture("lightning")));

            // CRIT
            float finalDamage = calculateDamage(caster);
            projectile.add(new ProjectileComponent(finalDamage, 3f, "lightning"));

            // Chain logic
            projectile.add(new ChainComponent(chainCount, chainRange, finalDamage * 0.7f));

            // Lightning doesn't typically "pierce" in a straight line because it chains,
            // but we can add it if you really want. Usually redundant with ChainComponent.

            UpgradeHelper.applyProjectileUpgrades(projectile, playerBuild, this.name);
            engine.addEntity(projectile);
        }
    }
}
