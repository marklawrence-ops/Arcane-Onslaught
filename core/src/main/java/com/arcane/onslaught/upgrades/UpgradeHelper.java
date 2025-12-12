package com.arcane.onslaught.upgrades;

import com.badlogic.ashley.core.Entity;
import com.arcane.onslaught.entities.components.*;
import com.badlogic.gdx.graphics.Color;

/**
 * Helper class to apply upgrade tags to projectiles and game systems
 */
public class UpgradeHelper {

    /**
     * Apply player build upgrades to a newly created projectile
     */
    public static void applyProjectileUpgrades(Entity projectile, PlayerBuild build, String spellName) {
        ProjectileComponent proj = projectile.getComponent(ProjectileComponent.class);
        VisualComponent vis = projectile.getComponent(VisualComponent.class);
        VelocityComponent vel = projectile.getComponent(VelocityComponent.class);

        if (proj == null) return;

        // Projectile Size
        if (build.hasTag("projectile_size") && vis != null) {
            vis.width *= 1.3f;
            vis.height *= 1.3f;
        }

        // Projectile Speed
        if (build.hasTag("projectile_speed") && vel != null) {
            vel.velocity.scl(1.4f);
        }

        // Pierce for all projectiles
        int pierceBonus = 0;
        if (build.hasTag("pierce_all")) {
            pierceBonus += 1;
        }

        // Universal DOT
        if (build.hasTag("universal_dot")) {
            PoisonComponent poison = projectile.getComponent(PoisonComponent.class);
            if (poison == null) {
                projectile.add(new PoisonComponent(3f, 2f));
            }
        }

        // Spell-specific upgrades
        applySpellSpecificUpgrades(projectile, build, spellName, pierceBonus);
    }

    private static void applySpellSpecificUpgrades(Entity projectile, PlayerBuild build, String spellName, int pierceBonus) {
        switch (spellName) {
            case "Fireball":
                applyFireballUpgrades(projectile, build);
                break;
            case "Lightning Bolt":
                applyLightningUpgrades(projectile, build);
                break;
            case "Ice Shard":
                applyIceUpgrades(projectile, build);
                break;
            case "Poison Dart":
                applyPoisonUpgrades(projectile, build);
                break;
            case "Arcane Missiles":
                applyArcaneUpgrades(projectile, build, pierceBonus);
                break;
        }
    }

    private static void applyFireballUpgrades(Entity projectile, PlayerBuild build) {
        ExplosiveComponent explosive = projectile.getComponent(ExplosiveComponent.class);
        if (explosive != null && build.hasTag("fireball_size")) {
            explosive.explosionRadius *= 1.5f;
            explosive.explosionDamage *= 1.3f;
        }
    }

    private static void applyLightningUpgrades(Entity projectile, PlayerBuild build) {
        ChainComponent chain = projectile.getComponent(ChainComponent.class);
        if (chain != null && build.hasTag("lightning_chain")) {
            chain.maxChains += 2;
        }

        // Lightning-Poison synergy
        if (build.hasTag("electrocution")) {
            projectile.add(new PoisonComponent(5f, 3f));
        }
    }

    private static void applyIceUpgrades(Entity projectile, PlayerBuild build) {
        SlowComponent slow = projectile.getComponent(SlowComponent.class);
        if (slow != null && build.hasTag("ice_slow")) {
            slow.slowAmount = 0.7f; // 70% slow
            slow.slowDuration = 3f;
        }
    }

    private static void applyPoisonUpgrades(Entity projectile, PlayerBuild build) {
        PoisonComponent poison = projectile.getComponent(PoisonComponent.class);
        if (poison != null && build.hasTag("poison_power")) {
            poison.damagePerSecond *= 2f;
        }
    }

    private static void applyArcaneUpgrades(Entity projectile, PlayerBuild build, int pierceBonus) {
        PierceComponent pierce = projectile.getComponent(PierceComponent.class);
        if (pierce != null && build.hasTag("arcane_pierce")) {
            pierce.maxPierces += 2;
            pierce.remainingPierces += 2;
        }

        // Add general pierce bonus
        if (pierceBonus > 0) {
            if (pierce == null) {
                projectile.add(new PierceComponent(pierceBonus));
            } else {
                pierce.maxPierces += pierceBonus;
                pierce.remainingPierces += pierceBonus;
            }
        }
    }

    /**
     * Calculate XP multiplier from build
     */
    public static float getXPMultiplier(PlayerBuild build) {
        float multiplier = 1f;
        if (build.hasTag("xp_boost")) {
            multiplier += 0.25f;
        }
        return multiplier;
    }

    /**
     * Calculate pickup range multiplier from build
     */
    public static float getPickupRangeMultiplier(PlayerBuild build) {
        float multiplier = 1f;
        if (build.hasTag("pickup_range_2")) {
            multiplier += 1f;
        }
        return multiplier;
    }

    /**
     * Check if enemy death should trigger explosion
     */
    public static boolean shouldExplodeOnDeath(PlayerBuild build) {
        return build.hasTag("death_explosion");
    }

    /**
     * Check if player should lifesteal
     */
    public static boolean hasLifeSteal(PlayerBuild build) {
        return build.hasTag("lifesteal_active");
    }

    /**
     * Get health drop chance multiplier
     */
    public static float getHealthDropMultiplier(PlayerBuild build) {
        return build.hasTag("health_drop_boost") ? 2f : 1f;
    }
}
