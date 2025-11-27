package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Damage over time effect
 */
public class PoisonComponent implements Component {
    public float damagePerSecond;
    public float duration;
    public float timeRemaining;

    public PoisonComponent(float dps, float duration) {
        this.damagePerSecond = dps;
        this.duration = duration;
        this.timeRemaining = duration;
    }
}
