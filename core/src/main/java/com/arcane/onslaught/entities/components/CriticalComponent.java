package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Critical hit component
 */
public class CriticalComponent implements Component {
    public float critChance; // 0.0 to 1.0 (0.1 = 10% chance)
    public float critMultiplier; // 2.0 = double damage

    public CriticalComponent(float critChance, float critMultiplier) {
        this.critChance = critChance;
        this.critMultiplier = critMultiplier;
    }
}
