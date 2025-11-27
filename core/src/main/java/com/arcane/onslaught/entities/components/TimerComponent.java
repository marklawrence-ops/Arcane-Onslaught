package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

public class TimerComponent implements Component {
    public float interval;
    public float elapsed = 0;
    public boolean repeat = true;
    public Runnable onComplete;

    public TimerComponent(float interval, boolean repeat) {
        this.interval = interval;
        this.repeat = repeat;
    }

    public boolean update(float delta) {
        elapsed += delta;
        if (elapsed >= interval) {
            if (repeat) {
                elapsed -= interval;
            }
            return true;
        }
        return false;
    }
}
