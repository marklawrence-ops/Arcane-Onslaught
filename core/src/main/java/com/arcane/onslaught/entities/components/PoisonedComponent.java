package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Applied to enemies that are poisoned
 */
public class PoisonedComponent implements Component {
    public float damagePerSecond;
    public float timeRemaining;
    public float tickTimer = 0;

    public PoisonedComponent(float dps, float duration) {
        this.damagePerSecond = dps;
        this.timeRemaining = duration;
    }
}
