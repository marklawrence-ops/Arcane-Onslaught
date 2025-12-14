package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import java.util.ArrayList;
import java.util.List;

public class BossComponent implements Component {
    public String name;
    public String title;

    // Skill Management
    public float skillTimer = 0f;
    public float skillCooldown = 4.0f;

    // --- NEW: Telegraph State ---
    public boolean isTelegraphing = false;
    public float telegraphTimer = 0f;
    public BossSkill nextSkill = null; // Stores the skill waiting to trigger
    // ----------------------------

    public boolean isCasting = false;
    public float castTimer = 0f;

    public boolean isEnraged = false;
    public float enrageTimer = 0f;

    public List<BossSkill> availableSkills = new ArrayList<>();

    public enum BossSkill {
        SUMMON_MINIONS,
        DASH_ATTACK,
        TELEPORT_AMBUSH,
        ELECTRIC_AURA,
        FROST_BREATH,
        FIRE_FLAMETHROWER,
        POISON_SPIT,
        ARCANE_NOVA,
        GRAVITY_WELL,
        SHOCKWAVE,
        BERSERK
    }

    public BossComponent(String name, String title) {
        this.name = name;
        this.title = title;
    }
}
