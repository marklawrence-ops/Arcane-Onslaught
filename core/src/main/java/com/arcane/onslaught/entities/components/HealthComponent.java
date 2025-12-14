package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class HealthComponent implements Component {
    public float currentHealth;
    public float maxHealth;

    // --- NEW: This was missing! ---
    public float hitFlashTimer = 0f;
    // ------------------------------

    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public void damage(float amount) {
        currentHealth = Math.max(0, currentHealth - amount);

        // --- NEW: Trigger the red flash ---
        this.hitFlashTimer = 0.15f;
    }

    // NEW: Damage with armor consideration
    public void damage(float amount, Entity owner) {
        if (owner != null) {
            ArmorComponent armor = owner.getComponent(ArmorComponent.class);
            if (armor != null) {
                float reducedAmount = amount * (1f - armor.damageReduction);
                System.out.println("🛡️ Armor reduced damage from " + (int)amount + " to " + (int)reducedAmount);
                amount = reducedAmount;
            }
        }
        currentHealth = Math.max(0, currentHealth - amount);

        // --- NEW: Trigger the red flash here too ---
        this.hitFlashTimer = 0.15f;
    }

    public void heal(float amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
}
