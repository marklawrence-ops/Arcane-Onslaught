package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;

public class VisualComponent implements Component {
    public float width;
    public float height;
    public Color color;

    public VisualComponent(float width, float height, Color color) {
        this.width = width;
        this.height = height;
        this.color = color;
    }
}
