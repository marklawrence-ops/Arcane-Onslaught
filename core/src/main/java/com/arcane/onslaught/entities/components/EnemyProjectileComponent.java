package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

public class EnemyProjectileComponent implements Component {
    public float damage;

    public EnemyProjectileComponent(float damage) {
        this.damage = damage;
    }
}
