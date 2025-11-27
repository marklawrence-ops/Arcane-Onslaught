package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Slows enemies on hit
 */
public class SlowComponent implements Component {
    public float slowAmount; // 0.0 to 1.0 (0.5 = 50% slow)
    public float slowDuration;

    public SlowComponent(float amount, float duration) {
        this.slowAmount = amount;
        this.slowDuration = duration;
    }
}
