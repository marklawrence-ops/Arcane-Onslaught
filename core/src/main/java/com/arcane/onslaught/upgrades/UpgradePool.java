package com.arcane.onslaught.upgrades;

import com.arcane.onslaught.spells.SpellManager;
import java.util.*;

/**
 * Manages the pool of available upgrades and handles random selection
 */
public class UpgradePool {
    private List<Upgrade> allUpgrades;
    private Random random;

    public UpgradePool() {
        allUpgrades = new ArrayList<>();
        random = new Random();
        registerAllUpgrades();
    }

    private void registerAllUpgrades() {

        // Spells
        allUpgrades.add(new UnlockFireballUpgrade());
        allUpgrades.add(new UnlockArcaneMissilesUpgrade());
        allUpgrades.add(new UnlockIceShardUpgrade());
        allUpgrades.add(new UnlockLightningBoltUpgrade());
        allUpgrades.add(new UnlockPoisonDartUpgrade());

        // Passive - Offense
        allUpgrades.add(new DamageUpgrade());
        allUpgrades.add(new AttackSpeedUpgrade());
        allUpgrades.add(new CriticalHitUpgrade());
        allUpgrades.add(new MultiShotUpgrade());
        allUpgrades.add(new PiercingUpgrade());
        allUpgrades.add(new ProjectileSizeUpgrade());
        allUpgrades.add(new ProjectileSpeedUpgrade());
        allUpgrades.add(new DamageOverTimeUpgrade());
        allUpgrades.add(new ExplosiveFinishUpgrade());

        // Passive - Defense
        allUpgrades.add(new MaxHealthUpgrade());
        allUpgrades.add(new MaxHealthUpgrade2());
        allUpgrades.add(new RegenerationUpgrade());
        allUpgrades.add(new LifeStealUpgrade());
        allUpgrades.add(new HealthDropUpgrade());
        allUpgrades.add(new ArmorUpgrade());
        allUpgrades.add(new DodgeUpgrade());
        allUpgrades.add(new ReviveUpgrade());

        // Passive - Utility
        allUpgrades.add(new SpeedUpgrade());
        allUpgrades.add(new SpeedUpgrade2());
        allUpgrades.add(new PickupRangeUpgrade());
        allUpgrades.add(new PickupRangeUpgrade2());
        allUpgrades.add(new ExperienceGainUpgrade());
        allUpgrades.add(new LuckUpgrade());

        // Spell-Specific
        allUpgrades.add(new FireballSizeUpgrade());
        allUpgrades.add(new LightningChainUpgrade());
        allUpgrades.add(new IceSlowUpgrade());
        allUpgrades.add(new PoisonPowerUpgrade());
        allUpgrades.add(new ArcanePierceUpgrade());

        // Synergies
        allUpgrades.add(new FireAndIceSynergy());
        allUpgrades.add(new LightningPoisonSynergy());
        allUpgrades.add(new ExplosiveChainSynergy());
        allUpgrades.add(new PoisonExplosionSynergy());
        allUpgrades.add(new FrozenExplosionSynergy());
        allUpgrades.add(new AllElementsSynergy());
    }

    /**
     * Get 3 random upgrades that can be offered to the player
     */
    public List<Upgrade> getRandomUpgrades(PlayerBuild build, SpellManager spellManager, int count) {
        List<Upgrade> available = new ArrayList<>();

        // Filter by what can be offered
        for (Upgrade upgrade : allUpgrades) {
            if (upgrade.canOffer(build, spellManager)) {
                available.add(upgrade);
            }
        }

        if (available.isEmpty()) {
            System.out.println("WARNING: No upgrades available!");
            return new ArrayList<>();
        }

        // Weighted random selection based on rarity
        List<Upgrade> selected = new ArrayList<>();
        List<Upgrade> availableCopy = new ArrayList<>(available);

        for (int i = 0; i < count && !availableCopy.isEmpty(); i++) {
            Upgrade chosen = selectWeightedRandom(availableCopy);
            selected.add(chosen);
            availableCopy.remove(chosen); // Don't offer same upgrade twice
        }

        return selected;
    }

    /**
     * Select one upgrade with weighted probability based on rarity
     */
    private Upgrade selectWeightedRandom(List<Upgrade> upgrades) {
        int totalWeight = 0;
        for (Upgrade u : upgrades) {
            totalWeight += getRarityWeight(u.getRarity());
        }

        int roll = random.nextInt(totalWeight);
        int current = 0;

        for (Upgrade u : upgrades) {
            current += getRarityWeight(u.getRarity());
            if (roll < current) {
                return u;
            }
        }

        return upgrades.get(0); // Fallback
    }

    /**
     * Weight for rarity (higher = more common)
     */
    private int getRarityWeight(Upgrade.UpgradeRarity rarity) {
        switch (rarity) {
            case COMMON: return 50;
            case UNCOMMON: return 30;
            case RARE: return 15;
            case EPIC: return 5;
            default: return 1;
        }
    }

    public List<Upgrade> getAllUpgrades() {
        return allUpgrades;
    }
}
