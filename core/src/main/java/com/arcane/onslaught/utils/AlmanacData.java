package com.arcane.onslaught.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlmanacData {

    public enum Category {
        SPELLS, UPGRADES, ENEMIES, BOSSES
    }

    public static class Entry {
        public String name;
        public String description;
        public String lore; // Fluff text

        public Entry(String name, String description, String lore) {
            this.name = name;
            this.description = description;
            this.lore = lore;
        }
    }

    private static Map<Category, List<Entry>> database;

    static {
        database = new HashMap<>();

        // --- SPELLS (Based on UpgradePool "Unlock" classes) ---
        List<Entry> spells = new ArrayList<>();
        spells.add(new Entry("Arcane Missile", "Fires a homing bolt of pure energy.", "A staple of the academy, condensed mana seeking its target."));
        spells.add(new Entry("Fireball", "Launches a burning sphere that pierces enemies.", "Pyromancy in its most volatile form."));
        spells.add(new Entry("Ice Shard", "Shoots a freezing projectile that slows targets.", "Condensed frost from the northern peaks."));
        spells.add(new Entry("Lightning Bolt", " strikes a random enemy with high damage.", "The thunder god's judgment, instantaneous and bright."));
        spells.add(new Entry("Poison Dart", "Fires a quick projectile that applies DOT.", "A subtle weapon favored by assassins of the deep wood."));
        // Kept for legacy if you still use Aura, otherwise can be removed
        spells.add(new Entry("Lightning Aura", "Strikes nearby enemies with periodic bolts.", "Static discharge manifesting as a protective storm."));
        database.put(Category.SPELLS, spells);

        // --- UPGRADES (Based on UpgradePool) ---
        List<Entry> upgrades = new ArrayList<>();

        // Offense
        upgrades.add(new Entry("Might", "Increases base damage.", "Enhancing the potency of one's magical core."));
        upgrades.add(new Entry("Haste", "Increases attack speed.", "Time flows faster for the attuned mind."));
        upgrades.add(new Entry("Critical Hit", "Chance to deal double damage.", "Finding the weak point in the fabric of reality."));
        upgrades.add(new Entry("MultiShot", "Spells fire additional projectiles.", "Fracturing the spell weave to multiply its effect."));
        upgrades.add(new Entry("Piercing", "Projectiles pass through more enemies.", "Hardening the mana structure to survive impact."));
        upgrades.add(new Entry("Projectile Size", "Increases spell area/size.", "Feeding more energy into the manifestation."));
        upgrades.add(new Entry("Explosive Finish", "Enemies explode on death.", "Unstable resonance causing a chain reaction."));

        // Defense
        upgrades.add(new Entry("Vitality", "Increases maximum health.", "Fortifying the body to withstand the arcane strain."));
        upgrades.add(new Entry("Regeneration", "Passively restores health over time.", "Accelerating natural healing processes."));
        upgrades.add(new Entry("Life Steal", "Heals a portion of damage dealt.", "Siphoning the life essence of your foes."));
        upgrades.add(new Entry("Armor", "Reduces incoming damage.", "Spectral plating that dulls enemy claws."));
        upgrades.add(new Entry("Dodge", "Chance to ignore damage.", "Phasing out of reality for a split second."));
        upgrades.add(new Entry("Revive", "Grants an extra life.", "Cheating death, just this once."));

        // Utility
        upgrades.add(new Entry("Greed", "Increases pickup range.", "A magical magnetism for scattered essence."));
        upgrades.add(new Entry("Swiftness", "Increases movement speed.", "Wind-infused footsteps."));
        upgrades.add(new Entry("Wisdom", "Increases experience gained.", "Learning faster from the chaos of battle."));
        upgrades.add(new Entry("Luck", "Improves chances for good events.", "Fate smiles upon the bold."));

        // Synergies
        upgrades.add(new Entry("Fire & Ice", "Synergy: Opposing elements maximize damage.", "Thermodynamic paradox utilizing extreme temperature shock."));
        upgrades.add(new Entry("Lightning & Poison", "Synergy: Paralysis increases toxin effect.", "Nervous system overload."));
        upgrades.add(new Entry("All Elements", "Synergy: Mastery of all forms.", "The pinnacle of elemental theory."));

        database.put(Category.UPGRADES, upgrades);

        // --- ENEMIES (Based on EnemyType.java) ---
        List<Entry> enemies = new ArrayList<>();
        enemies.add(new Entry("Zombie", "Slow, melee attacker.", "A husk reanimated by the void's influence."));
        enemies.add(new Entry("Imp", "Fast, swarming creature.", "Minor demons that rush in hunger."));
        enemies.add(new Entry("Tank", "High health, slow speed.", "A bloated amalgamation of flesh and stone."));
        enemies.add(new Entry("Runner", "Very fast, low health.", "Twisted limbs designed for unnatural speed."));
        enemies.add(new Entry("Swarm", "Tiny, very fast, very weak.", "A collective consciousness driven by hunger."));
        enemies.add(new Entry("Brute", "High damage, aggressive.", "Muscle and rage, barely contained by skin."));
        enemies.add(new Entry("Ghost", "Passes through solid objects.", "A spirit trapped between the void and reality."));
        enemies.add(new Entry("Slime", "Leaves puddles on death.", "Living corruption that stains the earth."));
        enemies.add(new Entry("Elite", "Larger, stronger variant.", "A creature on the verge of becoming a lord."));
        database.put(Category.ENEMIES, enemies);

        // --- BOSSES (Based on BossComponent & AISystem) ---
        List<Entry> bosses = new ArrayList<>();
        bosses.add(new Entry("Void Lord", "The primary avatar of the abyss.", "An ancient entity seeking to consume all light."));

        // Boss Skills
        bosses.add(new Entry("Skill: Gravity Well", "Pulls the player towards the boss.", "Bending the laws of physics to entrap prey."));
        bosses.add(new Entry("Skill: Shockwave", "Massive area damage and knockback.", "The earth itself recoils from its step."));
        bosses.add(new Entry("Skill: Arcane Nova", "Fires projectiles in all directions.", "An unchecked release of raw chaotic energy."));
        bosses.add(new Entry("Skill: Berserk", "Boss enrages, doubling speed.", "Abandoning all defense for pure aggression."));
        bosses.add(new Entry("Skill: Summon Minions", "Calls lesser enemies to aid.", "The void answers its master's call."));
        bosses.add(new Entry("Skill: Dash Attack", "Rapid charge towards the player.", "Momentum capable of shattering stone."));
        bosses.add(new Entry("Skill: Teleport", "Instantly relocates to ambush.", "Stepping through the fold of reality."));

        // Elemental Boss Skills
        bosses.add(new Entry("Skill: Fire Flamethrower", "Cone of continuous fire damage.", "Dragon's breath imitated by corrupted magic."));
        bosses.add(new Entry("Skill: Frost Breath", "Cone of cold that slows.", "The chill of the grave, exhaled."));
        bosses.add(new Entry("Skill: Poison Spit", "Projectile that leaves toxic pools.", "Venomous bile from the inner abyss."));
        bosses.add(new Entry("Skill: Electric Aura", "Radial lightning strikes.", "The air itself crackles with hostile energy."));

        database.put(Category.BOSSES, bosses);
    }

    public static List<Entry> getEntries(Category category) {
        return database.getOrDefault(category, new ArrayList<>());
    }
}
