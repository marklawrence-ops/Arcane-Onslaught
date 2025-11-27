package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Makes projectile pierce through enemies
 */
public class PierceComponent implements Component {
    public int remainingPierces;
    public int maxPierces;

    public PierceComponent(int maxPierces) {
        this.maxPierces = maxPierces;
        this.remainingPierces = maxPierces;
    }
}
