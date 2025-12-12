package com.arcane.onslaught.upgrades;

import com.arcane.onslaught.spells.Spell;
import java.util.*;

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
        String name = upgrade.getName();
        upgradeStacks.put(name, upgradeStacks.getOrDefault(name, 0) + 1);
        tags.addAll(upgrade.getTags());
        System.out.println("★ Upgrade Applied: " + upgrade.getName() + " ★");
    }

    // --- NEW: Remove a specific upgrade (Used for Revive) ---
    public void removeUpgradeStack(String upgradeName) {
        if (upgradeStacks.containsKey(upgradeName)) {
            int current = upgradeStacks.get(upgradeName);

            if (current > 1) {
                // Decrement count
                upgradeStacks.put(upgradeName, current - 1);
            } else {
                // Remove entirely if it was the last stack
                upgradeStacks.remove(upgradeName);
            }

            // Also remove one instance from the list (for UI/Random logic)
            for (int i = 0; i < upgrades.size(); i++) {
                if (upgrades.get(i).getName().equals(upgradeName)) {
                    upgrades.remove(i);
                    break;
                }
            }

            System.out.println("Consumed Upgrade: " + upgradeName);
        }
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

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public List<Upgrade> getUpgrades() {
        return upgrades;
    }

    public Map<String, Integer> getUpgradeStackMap() {
        return upgradeStacks;
    }

    public void clear() {
        upgrades.clear();
        upgradeStacks.clear();
        tags.clear();
    }
}
