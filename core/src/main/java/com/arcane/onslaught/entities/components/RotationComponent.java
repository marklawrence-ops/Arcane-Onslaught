package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

public class RotationComponent implements Component {
    public float degreesPerSecond;

    public RotationComponent(float degreesPerSecond) {
        this.degreesPerSecond = degreesPerSecond;
    }
}
