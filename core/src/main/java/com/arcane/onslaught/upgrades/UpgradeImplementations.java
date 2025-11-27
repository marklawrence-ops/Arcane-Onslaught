package com.arcane.onslaught.upgrades;

import com.badlogic.ashley.core.Entity;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.spells.*;

// ============================================
// PASSIVE UPGRADES - OFFENSE
// ============================================

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
        // TODO: Implement crit system
        // For now, just increase damage
        for (Spell spell : spellManager.getActiveSpells()) {
            spell.setDamage(spell.getDamage() * 1.1f);
        }
    }
}

// ============================================
// PASSIVE UPGRADES - DEFENSE
// ============================================

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
            health.currentHealth = health.maxHealth; // Full heal
        }
    }
}

//class RegenerationUpgrade extends Upgrade {
//    public RegenerationUpgrade() {
//        super("Regeneration", "Heal 2 HP per second", UpgradeRarity.UNCOMMON);
//        addTag("regen");
//    }
//
//    @Override
//    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
//        RegenerationComponent regen = player.getComponent(RegenerationComponent.class);
//        if (regen == null) {
//            player.add(new RegenerationComponent(2f, 1f));
//        } else {
//            regen.healAmount += 2f;
//        }
//    }
//}

// ============================================
// PASSIVE UPGRADES - UTILITY
// ============================================

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
    }

    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        // Pickup range is in Constants, hard to modify dynamically
        // For now, just add a tag for future use
    }
}

// ============================================
// SPELL-SPECIFIC UPGRADES
// ============================================

class FireballSizeUpgrade extends Upgrade {
    public FireballSizeUpgrade() {
        super("Bigger Boom", "Fireball explosion radius +50%", UpgradeRarity.UNCOMMON);
        addTag("fireball");
        addTag("explosion");
    }

    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        // This would need to modify the ExplosiveComponent when created
        // Mark in build for spell creation
        addTag("fireball_size");
    }

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
    }

    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        addTag("lightning_chain");
    }

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
    }

    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        addTag("ice_slow");
    }

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Ice Shard");
    }
}

class PoisonPowerUpgrade extends Upgrade {
    public PoisonPowerUpgrade() {
        super("Toxic Venom", "Poison damage +100%", UpgradeRarity.UNCOMMON);
        addTag("poison");
    }

    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        addTag("poison_power");
    }

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
    }

    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        addTag("arcane_pierce");
    }

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Arcane Missiles");
    }
}

// ============================================
// SYNERGY UPGRADES
// ============================================

class FireAndIceSynergy extends Upgrade {
    public FireAndIceSynergy() {
        super("Thermal Shock", "Fire spells deal 50% more damage to slowed enemies", UpgradeRarity.RARE);
        addTag("synergy");
        addTag("fire");
        addTag("ice");
    }

    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        addTag("thermal_shock");
    }

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Fireball") && spellManager.hasSpell("Ice Shard");
    }
}

class LightningPoisonSynergy extends Upgrade {
    public LightningPoisonSynergy() {
        super("Electrocution", "Lightning chains also spread poison", UpgradeRarity.RARE);
        addTag("synergy");
        addTag("lightning");
        addTag("poison");
    }

    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        addTag("electrocution");
    }

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Lightning Bolt") && spellManager.hasSpell("Poison Dart");
    }
}

class ExplosiveChainSynergy extends Upgrade {
    public ExplosiveChainSynergy() {
        super("Chain Reaction", "Explosions trigger chain lightning", UpgradeRarity.EPIC);
        addTag("synergy");
        addTag("explosion");
        addTag("chain");
    }

    @Override
    public void apply(Entity player, SpellManager spellManager, PlayerBuild build) {
        addTag("chain_reaction");
    }

    @Override
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return spellManager.hasSpell("Fireball") && spellManager.hasSpell("Lightning Bolt");
    }
}
