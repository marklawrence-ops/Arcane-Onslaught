package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class VisualComponent implements Component {
    public float width;
    public float height;
    public Color color = Color.WHITE;

    // Animation Support
    public Animation<TextureRegion> animation;
    public float stateTime = 0f;
    public boolean isLooping = true;

    // Fallback for non-animated objects
    public TextureRegion staticRegion;
    public Texture texture;
    public Sprite sprite;
    public boolean useSprite = false;

    // --- NEW: Z-Index for Layering ---
    public int zIndex = 0;

    // Constructor for Animations
    public VisualComponent(float width, float height, Animation<TextureRegion> animation) {
        this.width = width;
        this.height = height;
        this.animation = animation;
        this.stateTime = 0f;
        this.zIndex = 10; // Default for animated entities (Player/Enemies)
    }

    // Constructor for Sprites (Textures)
    public VisualComponent(float width, float height, Texture texture) {
        this(width, height, texture, Color.WHITE);
    }

    public VisualComponent(float width, float height, Texture texture, Color tint) {
        this.width = width;
        this.height = height;

        if (texture == null) {
            this.color = tint != null ? tint : Color.MAGENTA;
            this.useSprite = false;
        } else {
            this.texture = texture;
            this.sprite = new Sprite(texture);
            this.sprite.setSize(width, height);
            if (tint != null) this.sprite.setColor(tint);
            this.useSprite = true;
            this.color = tint != null ? tint : Color.WHITE;
        }
        this.zIndex = 10; // Default layer
    }

    // Constructor for colored rectangles
    public VisualComponent(float width, float height, Color color) {
        this.width = width;
        this.height = height;
        this.color = color;
        this.useSprite = false;
        this.zIndex = 10;
    }
}
