package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Makes projectile explode on impact, damaging nearby enemies
 */
public class ExplosiveComponent implements Component {
    public float explosionRadius;
    public float explosionDamage;

    public ExplosiveComponent(float radius, float damage) {
        this.explosionRadius = radius;
        this.explosionDamage = damage;
    }
}
