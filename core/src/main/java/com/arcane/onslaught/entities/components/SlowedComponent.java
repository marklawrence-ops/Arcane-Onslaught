package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Applied to enemies that are slowed
 */
public class SlowedComponent implements Component {
    public float slowAmount;
    public float duration;
    public float originalSpeed;

    public SlowedComponent(float amount, float duration, float originalSpeed) {
        this.slowAmount = amount;
        this.duration = duration;
        this.originalSpeed = originalSpeed;
    }
}
