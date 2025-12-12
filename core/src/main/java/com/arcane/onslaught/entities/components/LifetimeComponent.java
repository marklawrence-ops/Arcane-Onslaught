package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

public class LifetimeComponent implements Component {
    public float timer = 0f;
    public float duration;

    public LifetimeComponent(float duration) {
        this.duration = duration;
        this.timer = 0f;
    }
}
