package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

public class EnemyComponent implements Component {
    public String enemyType = "basic";
    public float damage = 10f; // NEW: Default damage
    public float xpDropped = 5f;

    // Default constructor
    public EnemyComponent() {}

    // Constructor for Spawning
    public EnemyComponent(float damage, float xpDropped) {
        this.damage = damage;
        this.xpDropped = xpDropped;
    }
}
