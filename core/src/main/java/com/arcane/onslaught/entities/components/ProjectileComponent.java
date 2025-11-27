package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

public class ProjectileComponent implements Component {
    public float damage;
    public float lifetime;
    public float timeAlive = 0;
    public String spellType;

    public ProjectileComponent(float damage, float lifetime, String spellType) {
        this.damage = damage;
        this.lifetime = lifetime;
        this.spellType = spellType;
    }
}
