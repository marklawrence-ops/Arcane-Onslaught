package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

/**
 * Definition of Poison effect (Applied to Projectile)
 */
public class PoisonComponent implements Component {
    public float damagePerSecond;
    public float duration;

    public PoisonComponent(float dps, float duration) {
        this.damagePerSecond = dps;
        this.duration = duration;
        // Removed timeRemaining - the projectile doesn't need to track this!
    }
}
