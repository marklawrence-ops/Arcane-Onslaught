package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.UpgradeHelper;
import com.arcane.onslaught.utils.TextureManager;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

public class FireballSpell extends Spell {
    private float projectileSpeed = 200f;
    private float projectileSize = 30f;
    private float explosionRadius = 80f;

    public FireballSpell() {
        super("Fireball", 2.5f, 30f);
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
                TextureManager.getInstance().getTexture("fireball")));

            // CRIT & PIERCE
            float finalDamage = calculateDamage(caster);
            projectile.add(new ProjectileComponent(finalDamage, 5f, "fireball"));

            // Fireballs usually explode on impact, but if you have pierce, they might pass through!
            if (playerBuild.hasTag("piercing")) {
                projectile.add(new PierceComponent(1));
            }

            projectile.add(new ExplosiveComponent(explosionRadius, finalDamage * 0.5f));

            UpgradeHelper.applyProjectileUpgrades(projectile, playerBuild, this.name);
            engine.addEntity(projectile);
        }
    }
}
