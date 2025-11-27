package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

public class CollisionComponent implements Component {
    public float radius;
    public short categoryBits;
    public short maskBits;

    public CollisionComponent(float radius, short categoryBits, short maskBits) {
        this.radius = radius;
        this.categoryBits = categoryBits;
        this.maskBits = maskBits;
    }
}
