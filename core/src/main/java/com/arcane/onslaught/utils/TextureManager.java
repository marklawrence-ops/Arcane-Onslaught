package com.arcane.onslaught.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages loading and caching of game textures.
 * FIXED: Auto-generates a fallback texture so missing files don't crash the game.
 */
public class TextureManager {
    private static TextureManager instance;
    private Map<String, Texture> textures;
    private Texture fallbackTexture; // The safety net

    private TextureManager() {
        textures = new HashMap<>();
        createFallbackTexture();
    }

    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }

    /**
     * Creates a simple 1x1 white texture to use when a file is missing.
     * This prevents NullPointerExceptions in VisualComponent.
     */
    private void createFallbackTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.MAGENTA); // Hot pink to make it obvious it's missing
        pixmap.fill();
        fallbackTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void loadTextures() {
        System.out.println("Loading textures...");

        // Background & Other Assets
        loadTexture("background", "sprites/background.png");
        loadTexture("magic_circle", "sprites/magic_circle.png");
        loadTexture("teleport_beam", "sprites/teleport_beam.png");
        loadTexture("effect_revive", "sprites/effect_revive.png");
        loadTexture("effect_heal", "sprites/effect_heal.png");
        loadTexture("effect_poison", "sprites/effect_poison.png");
        loadTexture("vfx_steam", "sprites/vfx_steam.png");
        loadTexture("vfx_ice_shatter", "sprites/vfx_ice_shatter.png");
        loadTexture("vfx_muzzle_flash", "sprites/vfx_muzzle_flash.png");

        // Player
        loadTexture("player", "sprites/player/player.png");

        // Orbs
        loadTexture("xp_orb", "sprites/orbs/xp_orb.png");
        loadTexture("health_orb", "sprites/orbs/health_orb.png");

        // Enemies
        loadTexture("zombie", "sprites/enemies/zombie.png");
        loadTexture("imp", "sprites/enemies/imp.png");
        loadTexture("tank", "sprites/enemies/tank.png");
        loadTexture("runner", "sprites/enemies/runner.png");
        loadTexture("swarm", "sprites/enemies/swarm.png");
        loadTexture("brute", "sprites/enemies/brute.png");
        loadTexture("ghost", "sprites/enemies/ghost.png");
        loadTexture("elite", "sprites/enemies/elite.png");
        loadTexture("slime", "sprites/enemies/slime.png");

        // Spells (Add these if you have them, otherwise fallback will be used)
        loadTexture("fireball", "sprites/spells/fireball.png");
        loadTexture("ice_shard", "sprites/spells/ice_shard.png");
        loadTexture("lightning", "sprites/spells/lightning.png");
        loadTexture("poison", "sprites/spells/poison.png");
        loadTexture("magic_bolt", "sprites/spells/magic_bolt.png");
        loadTexture("arcane_missile", "sprites/spells/arcane_missile.png");

        System.out.println("Textures loaded: " + textures.size());
    }

    private void loadTexture(String key, String path) {
        try {
            // Check if file exists before trying to load
            if (Gdx.files.internal(path).exists()) {
                Texture texture = new Texture(Gdx.files.internal(path));
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                textures.put(key, texture);
                System.out.println("✓ Loaded: " + key);
            } else {
                System.err.println("⚠ Texture file missing: " + path);
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to load texture: " + path);
        }
    }

    /**
     * Returns the requested texture, or the fallback if missing.
     * NEVER returns null.
     */
    public Texture getTexture(String key) {
        if (textures.containsKey(key)) {
            return textures.get(key);
        }
        // Return fallback instead of null to prevent crashing
        return fallbackTexture;
    }

    public boolean hasTexture(String key) {
        return textures.containsKey(key);
    }

    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        if (fallbackTexture != null) {
            fallbackTexture.dispose();
        }
    }
}
