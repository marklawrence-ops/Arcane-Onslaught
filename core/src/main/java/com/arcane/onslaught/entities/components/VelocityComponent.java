package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class VelocityComponent implements Component {
    public Vector2 velocity = new Vector2();
    public float maxSpeed = 0;

    public VelocityComponent() {}

    public VelocityComponent(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
