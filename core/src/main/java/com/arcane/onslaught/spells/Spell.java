package com.arcane.onslaught.spells;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.upgrades.PlayerBuild;

/**
 * Base class for all spell types
 */
public abstract class Spell {
    protected String name;
    protected float cooldown;
    protected float currentCooldown = 0;
    protected float damage;

    public Spell(String name, float cooldown, float damage) {
        this.name = name;
        this.cooldown = cooldown;
        this.damage = damage;
        this.currentCooldown = 0; // Start ready to cast
    }

    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
            if (currentCooldown < 0) {
                currentCooldown = 0;
            }
        }
    }

    public boolean canCast() {
        return currentCooldown <= 0;
    }

    public void resetCooldown() {
        currentCooldown = cooldown;
        System.out.println(name + " cooldown reset to " + cooldown + "s");
    }

    // Add PlayerBuild parameter
    public abstract void cast(Engine engine, Vector2 playerPos, Vector2 targetPos, PlayerBuild playerBuild);

    public String getName() { return name; }
    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }
    public float getCooldown() { return cooldown; }
    public void setCooldown(float cooldown) {
        this.cooldown = cooldown;
        System.out.println(name + " cooldown changed to " + cooldown + "s");
    }
    public float getCurrentCooldown() { return currentCooldown; }
}
