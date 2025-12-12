package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Regeneration - heals over time
 */
public class RegenerationComponent implements Component {
    public float healAmount;
    public float healInterval;
    public float timeSinceLastHeal = 0;

    public RegenerationComponent(float healAmount, float healInterval) {
        this.healAmount = healAmount;
        this.healInterval = healInterval;
    }
}
