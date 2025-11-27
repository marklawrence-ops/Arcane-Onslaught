package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Homing projectile that tracks enemies
 */
public class HomingComponent implements Component {
    public float homingStrength;
    public com.badlogic.ashley.core.Entity target;

    public HomingComponent(float strength) {
        this.homingStrength = strength;
    }
}
