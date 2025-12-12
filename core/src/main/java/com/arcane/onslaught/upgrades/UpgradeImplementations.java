package com.arcane.onslaught.upgrades;

import com.badlogic.ashley.core.Entity;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.spells.*;

// ============================================
// SPELL UNLOCKS (Already correct, but double-checking)
// ============================================

class UnlockFireballUpgrade extends Upgrade {
    public UnlockFireballUpgrade() {
        super("Unlock: Fireball", "Explosive projectile with AOE damage", UpgradeRarity.UNCOMMON);
        addTag("spell");
        addTag("fireball");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        spellManager.addSpell(new FireballSpell());
    }
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        // ONE TIME ONLY
        return !spellManager.hasSpell("Fireball");
    }
}

class UnlockLightningBoltUpgrade extends Upgrade {
    public UnlockLightningBoltUpgrade() {
        super("Unlock: Lightning Bolt", "Fast projectile that chains to nearby enemies", UpgradeRarity.UNCOMMON);
        addTag("spell");
        addTag("lightning");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        spellManager.addSpell(new LightningBoltSpell());
    }
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return !spellManager.hasSpell("Lightning Bolt");
    }
}

class UnlockIceShardUpgrade extends Upgrade {
    public UnlockIceShardUpgrade() {
        super("Unlock: Ice Shard", "Projectile that slows enemies", UpgradeRarity.UNCOMMON);
        addTag("spell");
        addTag("ice");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        spellManager.addSpell(new IceShardSpell());
    }
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return !spellManager.hasSpell("Ice Shard");
    }
}

class UnlockArcaneMissilesUpgrade extends Upgrade {
    public UnlockArcaneMissilesUpgrade() {
        super("Unlock: Arcane Missiles", "3 piercing projectiles in a spread", UpgradeRarity.UNCOMMON);
        addTag("spell");
        addTag("arcane");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        spellManager.addSpell(new ArcaneMissilesSpell());
    }
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return !spellManager.hasSpell("Arcane Missiles");
    }
}

class UnlockPoisonDartUpgrade extends Upgrade {
    public UnlockPoisonDartUpgrade() {
        super("Unlock: Poison Dart", "Applies deadly poison damage over time", UpgradeRarity.UNCOMMON);
        addTag("spell");
        addTag("poison");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        spellManager.addSpell(new PoisonDartSpell());
    }
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return !spellManager.hasSpell("Poison Dart");
    }
}

// ============================================
// PASSIVE UPGRADES (Stackable - No Changes Needed)
// ============================================
// Damage, Speed, Health, etc. are fine to repeat.

class DamageUpgrade extends Upgrade {
    public DamageUpgrade() {
        super("Power Surge", "+20% Damage to all spells", UpgradeRarity.COMMON);
        addTag("damage");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        for (Spell spell : spellManager.getActiveSpells()) {
            spell.setDamage(spell.getDamage() * 1.2f);
        }
    }
}

class AttackSpeedUpgrade extends Upgrade {
    public AttackSpeedUpgrade() {
        super("Rapid Fire", "-15% Cooldown on all spells", UpgradeRarity.COMMON);
        addTag("attack_speed");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        for (Spell spell : spellManager.getActiveSpells()) {
            spell.setCooldown(spell.getCooldown() * 0.85f);
        }
    }
}

class CriticalHitUpgrade extends Upgrade {
    public CriticalHitUpgrade() {
        super("Critical Mass", "10% chance to deal 2x damage", UpgradeRarity.UNCOMMON);
        addTag("critical");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        CriticalComponent crit = player.getComponent(CriticalComponent.class);
        if (crit == null) {
            player.add(new CriticalComponent(0.10f, 2.0f));
        } else {
            crit.critChance += 0.10f;
        }
    }
}

class MaxHealthUpgrade extends Upgrade {
    public MaxHealthUpgrade() {
        super("Vitality", "+25% Max Health", UpgradeRarity.COMMON);
        addTag("health");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        HealthComponent health = player.getComponent(HealthComponent.class);
        if (health != null) {
            health.maxHealth *= 1.25f;
            health.currentHealth = health.maxHealth;
        }
    }
}

class RegenerationUpgrade extends Upgrade {
    public RegenerationUpgrade() {
        super("Regeneration", "Heal 2 HP per second", UpgradeRarity.UNCOMMON);
        addTag("regen");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        RegenerationComponent regen = player.getComponent(RegenerationComponent.class);
        if (regen == null) {
            player.add(new RegenerationComponent(2f, 1f));
        } else {
            regen.healAmount += 2f;
        }
    }
}

// Fixed LifeSteal logic (from previous step)
class LifeStealUpgrade extends Upgrade {
    public LifeStealUpgrade() {
        super("Life Steal", "Heal 1 HP on kill (Stackable)", UpgradeRarity.UNCOMMON);
        addTag("lifesteal");
        addTag("lifesteal_active");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        LifeStealComponent ls = player.getComponent(LifeStealComponent.class);
        if (ls == null) {
            player.add(new LifeStealComponent(1));
        } else {
            ls.amount++;
        }
    }
}

class HealthDropUpgrade extends Upgrade {
    public HealthDropUpgrade() {
        super("Blood Harvest", "Enemies drop health orbs 2x more often", UpgradeRarity.UNCOMMON);
        addTag("health_drop");
        addTag("health_drop_boost");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
}

class SpeedUpgrade extends Upgrade {
    public SpeedUpgrade() {
        super("Swift Step", "+25% Movement Speed", UpgradeRarity.COMMON);
        addTag("speed");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        if (vel != null) {
            vel.maxSpeed *= 1.25f;
        }
    }
}

class PickupRangeUpgrade extends Upgrade {
    public PickupRangeUpgrade() {
        super("Magnetism", "2x XP Pickup Range", UpgradeRarity.COMMON);
        addTag("pickup");
        addTag("pickup_range_boost");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
}

// ============================================
// SPELL-SPECIFIC UPGRADES (Stackable)
// ============================================

class FireballSizeUpgrade extends Upgrade {
    public FireballSizeUpgrade() {
        super("Bigger Boom", "Fireball explosion radius +50%", UpgradeRarity.UNCOMMON);
        addTag("fireball");
        addTag("explosion");
        addTag("fireball_size");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Fireball");
    }
}

class LightningChainUpgrade extends Upgrade {
    public LightningChainUpgrade() {
        super("Arc Welder", "Lightning chains 2 more times", UpgradeRarity.UNCOMMON);
        addTag("lightning");
        addTag("chain");
        addTag("lightning_chain");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Lightning Bolt");
    }
}

class IceSlowUpgrade extends Upgrade {
    public IceSlowUpgrade() {
        super("Deep Freeze", "Ice slow increased to 70%", UpgradeRarity.UNCOMMON);
        addTag("ice");
        addTag("slow");
        addTag("ice_slow");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Ice Shard");
    }
}

class PoisonPowerUpgrade extends Upgrade {
    public PoisonPowerUpgrade() {
        super("Toxic Venom", "Poison damage +100%", UpgradeRarity.UNCOMMON);
        addTag("poison");
        addTag("poison_power");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Poison Dart");
    }
}

class ArcanePierceUpgrade extends Upgrade {
    public ArcanePierceUpgrade() {
        super("Unstoppable Force", "Arcane Missiles pierce 2 more enemies", UpgradeRarity.UNCOMMON);
        addTag("arcane");
        addTag("pierce");
        addTag("arcane_pierce");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Arcane Missiles");
    }
}

// ============================================
// SYNERGY UPGRADES (FIXED: ONE TIME ONLY)
// ============================================

class FireAndIceSynergy extends Upgrade {
    public FireAndIceSynergy() {
        super("Thermal Shock", "Fire spells deal 50% more damage to slowed enemies", UpgradeRarity.RARE);
        addTag("synergy");
        addTag("fire");
        addTag("ice");
        addTag("thermal_shock");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        // Must have both spells AND NOT HAVE THIS UPGRADE ALREADY
        return spellManager.hasSpell("Fireball")
            && spellManager.hasSpell("Ice Shard")
            && !build.hasTag("thermal_shock");
    }
}

class LightningPoisonSynergy extends Upgrade {
    public LightningPoisonSynergy() {
        super("Electrocution", "Lightning chains also spread poison", UpgradeRarity.RARE);
        addTag("synergy");
        addTag("lightning");
        addTag("poison");
        addTag("electrocution");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Lightning Bolt")
            && spellManager.hasSpell("Poison Dart")
            && !build.hasTag("electrocution"); // FIXED
    }
}

class ExplosiveChainSynergy extends Upgrade {
    public ExplosiveChainSynergy() {
        super("Chain Reaction", "Explosions trigger chain lightning", UpgradeRarity.EPIC);
        addTag("synergy");
        addTag("explosion");
        addTag("chain");
        addTag("chain_reaction");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Fireball")
            && spellManager.hasSpell("Lightning Bolt")
            && !build.hasTag("chain_reaction"); // FIXED
    }
}

class PoisonExplosionSynergy extends Upgrade {
    public PoisonExplosionSynergy() {
        super("Toxic Cloud", "Explosions leave poison clouds", UpgradeRarity.RARE);
        addTag("synergy");
        addTag("poison");
        addTag("explosion");
        addTag("toxic_cloud");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Fireball")
            && spellManager.hasSpell("Poison Dart")
            && !build.hasTag("toxic_cloud"); // FIXED
    }
}

class FrozenExplosionSynergy extends Upgrade {
    public FrozenExplosionSynergy() {
        super("Shatter", "Explosions deal 100% more damage to frozen enemies", UpgradeRarity.RARE);
        addTag("synergy");
        addTag("ice");
        addTag("explosion");
        addTag("shatter");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Fireball")
            && spellManager.hasSpell("Ice Shard")
            && !build.hasTag("shatter"); // FIXED
    }
}

class AllElementsSynergy extends Upgrade {
    public AllElementsSynergy() {
        super("Elemental Master", "All spells deal 50% more damage", UpgradeRarity.EPIC);
        addTag("synergy");
        addTag("ultimate");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        for (Spell spell : spellManager.getActiveSpells()) {
            spell.setDamage(spell.getDamage() * 1.5f);
        }
    }
    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        // Can only get "Ultimate" once
        return spellManager.getActiveSpells().size() >= 5
            && !build.hasTag("ultimate"); // FIXED
    }
}

// ============================================
// OTHER UNIQUE UPGRADES
// ============================================

class MultiShotUpgrade extends Upgrade {
    public MultiShotUpgrade() {
        super("Multicast", "+1 projectile to all spells", UpgradeRarity.UNCOMMON);
        addTag("multishot");
        addTag("multicast_active");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
    // Multishot is powerful, usually stackable, but if you want limit, add check here.
    // For now, let's allow stacking.
}

class PiercingUpgrade extends Upgrade {
    public PiercingUpgrade() {
        super("Piercing Shot", "All projectiles pierce +1 enemy", UpgradeRarity.UNCOMMON);
        addTag("pierce");
        addTag("pierce_all");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
}

class ProjectileSizeUpgrade extends Upgrade {
    public ProjectileSizeUpgrade() {
        super("Giant Projectiles", "+30% projectile size", UpgradeRarity.COMMON);
        addTag("size");
        addTag("projectile_size");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
}

class ProjectileSpeedUpgrade extends Upgrade {
    public ProjectileSpeedUpgrade() {
        super("Quickshot", "+40% projectile speed", UpgradeRarity.COMMON);
        addTag("proj_speed");
        addTag("projectile_speed");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
}

class DamageOverTimeUpgrade extends Upgrade {
    public DamageOverTimeUpgrade() {
        super("Lingering Pain", "All attacks apply 3 damage/sec for 2s", UpgradeRarity.RARE);
        addTag("dot");
        addTag("universal_dot");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
}

// --- FIXED: One-time only logic for Explosive Death ---
class ExplosiveFinishUpgrade extends Upgrade {
    public ExplosiveFinishUpgrade() {
        super("Explosive Death", "Enemies explode when killed", UpgradeRarity.RARE);
        addTag("explosive_death");
        addTag("death_explosion");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return !build.hasTag("death_explosion"); // FIXED: Only offer once
    }
}

class ArmorUpgrade extends Upgrade {
    public ArmorUpgrade() {
        super("Tough Skin", "Take 15% less damage", UpgradeRarity.UNCOMMON);
        addTag("armor");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        ArmorComponent armor = player.getComponent(ArmorComponent.class);
        if (armor == null) {
            player.add(new ArmorComponent(0.15f));
        } else {
            armor.damageReduction += 0.15f;
        }
    }
}

class DodgeUpgrade extends Upgrade {
    public DodgeUpgrade() {
        super("Evasion", "10% chance to dodge attacks", UpgradeRarity.RARE);
        addTag("dodge");
        addTag("dodge_chance");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
}

class MaxHealthUpgrade2 extends Upgrade {
    public MaxHealthUpgrade2() {
        super("Fortitude II", "+35% Max Health", UpgradeRarity.UNCOMMON);
        addTag("health");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        HealthComponent health = player.getComponent(HealthComponent.class);
        if (health != null) {
            health.maxHealth *= 1.35f;
        }
    }
}

// Revive Logic (Stackable, so no canOffer limit needed)
class ReviveUpgrade extends Upgrade {
    public ReviveUpgrade() {
        super("Second Chance", "Revive once with 50% HP (Stackable)", UpgradeRarity.EPIC);
        addTag("revive");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        ReviveComponent rev = player.getComponent(ReviveComponent.class);
        if (rev == null) {
            player.add(new ReviveComponent(1));
        } else {
            rev.lives++;
        }
        System.out.println("Extra Life Added! Total: " + (rev != null ? rev.lives : 1));
    }
}

class ExperienceGainUpgrade extends Upgrade {
    public ExperienceGainUpgrade() {
        super("Knowledge", "+25% XP gain", UpgradeRarity.COMMON);
        addTag("xp_gain");
        addTag("xp_boost");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
}

class PickupRangeUpgrade2 extends Upgrade {
    public PickupRangeUpgrade2() {
        super("Vacuum", "+100% pickup range", UpgradeRarity.UNCOMMON);
        addTag("pickup");
        addTag("pickup_range_2");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
}

class LuckUpgrade extends Upgrade {
    public LuckUpgrade() {
        super("Fortune", "Better upgrade rarity chances", UpgradeRarity.RARE);
        addTag("luck");
        addTag("lucky");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {}
}

class SpeedUpgrade2 extends Upgrade {
    public SpeedUpgrade2() {
        super("Haste", "+35% Movement Speed", UpgradeRarity.UNCOMMON);
        addTag("speed");
    }
    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        if (vel != null) {
            vel.maxSpeed *= 1.35f;
        }
    }
}
