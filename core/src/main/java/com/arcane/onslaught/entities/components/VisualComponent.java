package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class VisualComponent implements Component {
    public float width;
    public float height;
    public Color color;

    // Sprite support
    public Texture texture;
    public Sprite sprite;
    public boolean useSprite = false;

    // Constructor for colored rectangles
    public VisualComponent(float width, float height, Color color) {
        this.width = width;
        this.height = height;
        this.color = color;
        this.useSprite = false;
    }

    // Constructor for sprites
    public VisualComponent(float width, float height, Texture texture) {
        this.width = width;
        this.height = height;

        // Safety check for null texture
        if (texture == null) {
            System.err.println("ERROR: Attempted to create VisualComponent with NULL texture. Using Color fallback.");
            this.color = Color.MAGENTA;
            this.useSprite = false;
        } else {
            this.texture = texture;
            this.sprite = new Sprite(texture);
            this.sprite.setSize(width, height);
            this.useSprite = true;
            this.color = Color.WHITE;
        }
    }

    // Constructor for sprites with custom color tint
    public VisualComponent(float width, float height, Texture texture, Color tint) {
        this.width = width;
        this.height = height;

        if (texture == null) {
            System.err.println("ERROR: Attempted to create VisualComponent with NULL texture. Using Color fallback.");
            this.color = tint != null ? tint : Color.MAGENTA;
            this.useSprite = false;
        } else {
            this.texture = texture;
            this.sprite = new Sprite(texture);
            this.sprite.setSize(width, height);
            this.sprite.setColor(tint);
            this.useSprite = true;
            this.color = tint;
        }
    }
}
