package com.arcane.onslaught.spells;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all spells the player currently has equipped
 */
public class SpellManager {
    private List<Spell> activeSpells;

    public SpellManager() {
        activeSpells = new ArrayList<>();
        // Start with basic magic bolt
        activeSpells.add(new MagicBoltSpell());
    }

    public void addSpell(Spell spell) {
        activeSpells.add(spell);
        System.out.println("★ New Spell Unlocked: " + spell.getName() + " ★");
    }

    public void removeSpell(Spell spell) {
        activeSpells.remove(spell);
    }

    public List<Spell> getActiveSpells() {
        return activeSpells;
    }

    public void updateAllSpells(float delta) {
        for (Spell spell : activeSpells) {
            spell.update(delta);
        }
    }

    public boolean hasSpell(String spellName) {
        return activeSpells.stream().anyMatch(s -> s.getName().equals(spellName));
    }

    public void clear() {
        activeSpells.clear();
    }
}
