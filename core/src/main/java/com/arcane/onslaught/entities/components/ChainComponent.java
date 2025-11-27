package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Makes projectile chain to nearby enemies
 */
public class ChainComponent implements Component {
    public int remainingChains;
    public float chainRange;
    public float chainDamage;
    public java.util.Set<com.badlogic.ashley.core.Entity> hitEntities;

    public ChainComponent(int chains, float range, float damage) {
        this.remainingChains = chains;
        this.chainRange = range;
        this.chainDamage = damage;
        this.hitEntities = new java.util.HashSet<>();
    }
}
