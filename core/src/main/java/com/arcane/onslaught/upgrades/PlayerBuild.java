package com.arcane.onslaught.upgrades;

import com.arcane.onslaught.spells.Spell;
import java.util.*;

/**
 * Tracks the player's current build - all upgrades and stats
 */
public class PlayerBuild {
    private List<Upgrade> upgrades;
    private Map<String, Integer> upgradeStacks;
    private Set<String> tags;

    public PlayerBuild() {
        upgrades = new ArrayList<>();
        upgradeStacks = new HashMap<>();
        tags = new HashSet<>();
    }

    public void addUpgrade(Upgrade upgrade) {
        upgrades.add(upgrade);

        // Track stacks
        String name = upgrade.getName();
        upgradeStacks.put(name, upgradeStacks.getOrDefault(name, 0) + 1);

        // Add tags
        tags.addAll(upgrade.getTags());

        System.out.println("★ Upgrade Applied: " + upgrade.getName() + " ★");
    }

    public boolean hasUpgrade(String upgradeName) {
        return upgradeStacks.containsKey(upgradeName);
    }

    public int getUpgradeStacks(String upgradeName) {
        return upgradeStacks.getOrDefault(upgradeName, 0);
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public List<Upgrade> getUpgrades() {
        return upgrades;
    }

    public void clear() {
        upgrades.clear();
        upgradeStacks.clear();
        tags.clear();
    }
}
