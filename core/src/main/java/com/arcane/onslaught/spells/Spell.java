package com.arcane.onslaught.spells;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.entities.components.CriticalComponent;

/**
 * Base class for all spell types
 */
public abstract class Spell {
    protected String name;
    protected float cooldown;
    protected float currentCooldown = 0;
    protected float damage;

    private static final ComponentMapper<CriticalComponent> critMapper = ComponentMapper.getFor(CriticalComponent.class);

    public Spell(String name, float cooldown, float damage) {
        this.name = name;
        this.cooldown = cooldown;
        this.damage = damage;
        this.currentCooldown = 0;
    }

    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
            if (currentCooldown < 0) currentCooldown = 0;
        }
    }

    public boolean canCast() { return currentCooldown <= 0; }

    public void resetCooldown() {
        currentCooldown = cooldown;
    }

    // --- UPDATED SIGNATURE ---
    public abstract void cast(Engine engine, Entity caster, Vector2 playerPos, Vector2 targetPos, PlayerBuild playerBuild);

    /**
     * calculates damage based on the caster's CriticalComponent stats
     */
    protected float calculateDamage(Entity caster) {
        float finalDamage = this.damage;
        CriticalComponent crit = critMapper.get(caster);

        if (crit != null) {
            if (Math.random() < crit.critChance) {
                finalDamage *= crit.critMultiplier;
                // Optional: You can add a "Crit!" floating text event here later
            }
        }
        return finalDamage;
    }

    public String getName() { return name; }
    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }
    public float getCooldown() { return cooldown; }
    public void setCooldown(float cooldown) { this.cooldown = cooldown; }
    public float getCurrentCooldown() { return currentCooldown; }
}
