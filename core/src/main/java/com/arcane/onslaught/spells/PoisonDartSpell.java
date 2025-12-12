package com.arcane.onslaught.spells;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.UpgradeHelper;
import com.arcane.onslaught.utils.TextureManager;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

public class PoisonDartSpell extends Spell {
    private float projectileSpeed = 450f;
    private float projectileSize = 16f;
    private float poisonDps = 5f;
    private float poisonDuration = 4f;

    public PoisonDartSpell() {
        super("Poison Dart", 0.6f, 5f); // Low base damage, high DOT
    }

    @Override
    public void cast(Engine engine, Vector2 playerPos, Vector2 targetPos, PlayerBuild playerBuild) {
        Vector2 baseDirection = new Vector2(targetPos).sub(playerPos).nor();
        if (baseDirection.isZero()) baseDirection.set(1, 0);

        int count = UpgradeHelper.getProjectileCount(playerBuild);
        float spread = 12f; // Slightly tighter spread for precision

        for (int i = 0; i < count; i++) {
            float angleOffset = spread * (i - (count - 1) / 2f);
            Vector2 direction = baseDirection.cpy().rotateDeg(angleOffset);

            Entity projectile = new Entity();
            projectile.add(new PositionComponent(playerPos.x, playerPos.y));

            VelocityComponent vel = new VelocityComponent(projectileSpeed);
            vel.velocity.set(direction).scl(projectileSpeed);
            projectile.add(vel);

            projectile.add(new VisualComponent(projectileSize, projectileSize,
                TextureManager.getInstance().getTexture("poison")));

            projectile.add(new ProjectileComponent(damage, 4f, "poison"));

            // Poison Component
            projectile.add(new PoisonComponent(poisonDps, poisonDuration));

            UpgradeHelper.applyProjectileUpgrades(projectile, playerBuild, this.name);
            engine.addEntity(projectile);
        }
    }
}
