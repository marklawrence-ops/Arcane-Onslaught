package com.arcane.onslaught.upgrades;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.arcane.onslaught.entities.components.*;

public class UpgradeHelper {

    // --- NEW: Calculate Total Projectiles ---
    public static int getProjectileCount(PlayerBuild build) {
        int base = 1;
        int stacks = build.getUpgradeStacks("Multicast");
        return base + stacks;
    }

    public static void applyProjectileUpgrades(Entity projectile, PlayerBuild build, String spellName) {
        ProjectileComponent proj = projectile.getComponent(ProjectileComponent.class);
        VisualComponent vis = projectile.getComponent(VisualComponent.class);
        VelocityComponent vel = projectile.getComponent(VelocityComponent.class);

        if (proj == null) return;

        // 1. Projectile Size
        int sizeStacks = build.getUpgradeStacks("Giant Projectiles");
        if (sizeStacks > 0 && vis != null) {
            float multiplier = 1f + (0.30f * sizeStacks);
            vis.width *= multiplier;
            vis.height *= multiplier;
        }

        // Elemental Master Synergy
        if (build.hasTag("ultimate") && vis != null && vis.sprite != null) {
            vis.sprite.setColor(new Color(1f, 0.9f, 0.5f, 1f));
            vis.width *= 1.25f;
            vis.height *= 1.25f;
        }

        // 2. Projectile Speed
        int speedStacks = build.getUpgradeStacks("Quickshot");
        if (speedStacks > 0 && vel != null) {
            float multiplier = 1f + (0.40f * speedStacks);
            vel.velocity.scl(multiplier);
        }

        // 3. Pierce
        int pierceBonus = build.getUpgradeStacks("Piercing Shot");

        // 4. Universal DOT
        int dotStacks = build.getUpgradeStacks("Lingering Pain");
        if (dotStacks > 0) {
            float damage = 3f * dotStacks;
            PoisonComponent poison = projectile.getComponent(PoisonComponent.class);
            if (poison == null) {
                projectile.add(new PoisonComponent(damage, 2f));
            } else {
                poison.damagePerSecond += damage;
            }
        }

        applySpellSpecificUpgrades(projectile, build, spellName, pierceBonus);
    }

    private static void applySpellSpecificUpgrades(Entity projectile, PlayerBuild build, String spellName, int pierceBonus) {
        switch (spellName) {
            case "Fireball":
                ExplosiveComponent explosive = projectile.getComponent(ExplosiveComponent.class);
                int fireStacks = build.getUpgradeStacks("Bigger Boom");
                if (explosive != null && fireStacks > 0) {
                    explosive.explosionRadius *= (1f + 0.5f * fireStacks);
                    explosive.explosionDamage *= (1f + 0.3f * fireStacks);
                }
                break;

            case "Lightning Bolt":
                ChainComponent chain = projectile.getComponent(ChainComponent.class);
                int lightningStacks = build.getUpgradeStacks("Arc Welder");
                if (chain != null && lightningStacks > 0) {
                    chain.maxChains += (2 * lightningStacks);
                    chain.remainingChains += (2 * lightningStacks);
                }
                if (build.hasTag("electrocution")) {
                    projectile.add(new PoisonComponent(5f, 3f));
                }
                break;

            case "Ice Shard":
                SlowComponent slow = projectile.getComponent(SlowComponent.class);
                if (slow != null && build.hasTag("ice_slow")) {
                    slow.slowAmount = 0.7f;
                    slow.slowDuration = 3f;
                }
                break;

            case "Poison Dart":
                PoisonComponent poison = projectile.getComponent(PoisonComponent.class);
                int poisonStacks = build.getUpgradeStacks("Toxic Venom");
                if (poison != null && poisonStacks > 0) {
                    poison.damagePerSecond *= (1f + 1f * poisonStacks);
                }
                break;

            case "Arcane Missiles":
                PierceComponent pierce = projectile.getComponent(PierceComponent.class);
                int arcaneStacks = build.getUpgradeStacks("Unstoppable Force");
                if (pierce != null && arcaneStacks > 0) {
                    pierceBonus += (2 * arcaneStacks);
                }
                break;
        }

        if (pierceBonus > 0) {
            PierceComponent pierce = projectile.getComponent(PierceComponent.class);
            if (pierce == null) {
                projectile.add(new PierceComponent(pierceBonus));
            } else {
                pierce.maxPierces += pierceBonus;
                pierce.remainingPierces += pierceBonus;
            }
        }
    }

    // ... (Keep existing getters for XP, Health, Pickup, etc.) ...
    public static float getXPMultiplier(PlayerBuild build) {
        int stacks = build.getUpgradeStacks("Knowledge");
        return 1f + (0.25f * stacks);
    }
    public static float getPickupRangeMultiplier(PlayerBuild build) {
        int stacks = build.getUpgradeStacks("Magnetism");
        return 1f + (1.0f * stacks);
    }
    public static float getHealthDropMultiplier(PlayerBuild build) {
        int stacks = build.getUpgradeStacks("Blood Harvest");
        return 1f + (1.0f * stacks);
    }
    public static boolean shouldExplodeOnDeath(PlayerBuild build) {
        return build.hasTag("death_explosion");
    }
    public static boolean hasLifeSteal(PlayerBuild build) {
        return build.hasTag("lifesteal_active");
    }
}
