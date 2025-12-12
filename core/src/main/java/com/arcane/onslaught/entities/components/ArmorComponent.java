package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Armor - reduces incoming damage
 */
public class ArmorComponent implements Component {
    public float damageReduction; // 0.0 to 1.0 (0.15 = 15% reduction)

    public ArmorComponent(float damageReduction) {
        this.damageReduction = damageReduction;
    }
}
