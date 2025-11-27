package com.arcane.onslaught.upgrades;

import com.badlogic.ashley.core.Entity;
import com.arcane.onslaught.spells.SpellManager;
import java.util.*;

/**
 * Base class for all upgrades
 */
public abstract class Upgrade {
    protected String name;
    protected String description;
    protected UpgradeRarity rarity;
    protected List<String> tags;

    public enum UpgradeRarity {
        COMMON,    // 50% chance
        UNCOMMON,  // 30% chance
        RARE,      // 15% chance
        EPIC       // 5% chance
    }

    public Upgrade(String name, String description, UpgradeRarity rarity) {
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.tags = new ArrayList<>();
    }

    /**
     * Apply this upgrade to the player
     */
    public abstract void apply(Entity player, SpellManager spellManager, PlayerBuild build);

    /**
     * Check if this upgrade can be offered (prerequisites, etc.)
     */
    public boolean canOffer(PlayerBuild build, SpellManager spellManager) {
        return true;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public UpgradeRarity getRarity() { return rarity; }
    public List<String> getTags() { return tags; }

    protected void addTag(String tag) {
        tags.add(tag);
    }
}
