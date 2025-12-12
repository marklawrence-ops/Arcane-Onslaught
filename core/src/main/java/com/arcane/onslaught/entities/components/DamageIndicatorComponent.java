package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;

public class DamageIndicatorComponent implements Component {
    public float damage;
    public boolean isCritical;
    public float timeAlive;
    public float lifetime;
    public Color color;

    public DamageIndicatorComponent(float damage, boolean isCritical) {
        this.damage = damage;
        this.isCritical = isCritical;
        this.timeAlive = 0f;

        // --- IMPORTANT: Ensure this is NOT zero ---
        this.lifetime = 1.0f;

        if (isCritical) {
            this.color = new Color(1f, 0.2f, 0.2f, 1f); // Red
            this.lifetime = 1.5f; // Longer life for crits
        } else {
            this.color = new Color(1f, 1f, 1f, 1f); // White
        }
    }
}
