package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

public class SpawningComponent implements Component {
    public float timer;
    public float duration;

    public SpawningComponent(float duration) {
        this.duration = duration;
        this.timer = 0;
    }
}
