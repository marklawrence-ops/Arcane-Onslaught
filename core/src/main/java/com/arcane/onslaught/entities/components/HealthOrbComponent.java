package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Health orb that heals the player
 */
public class HealthOrbComponent implements Component {
    public float healAmount;

    public HealthOrbComponent(float healAmount) {
        this.healAmount = healAmount;
    }
}
