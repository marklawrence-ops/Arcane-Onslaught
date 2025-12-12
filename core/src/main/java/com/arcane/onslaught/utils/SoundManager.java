package com.arcane.onslaught.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, Sound> sounds;
    private boolean isMuted = false;
    private float volume = 0.5f; // Master volume

    private SoundManager() {
        sounds = new HashMap<>();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void loadSounds() {
        load("cast", "sounds/sfx_cast.wav");
        load("hit", "sounds/sfx_hit.wav");
        load("explosion", "sounds/sfx_explosion.wav");
        load("levelup", "sounds/sfx_levelup.wav");
        load("pickup", "sounds/sfx_pickup.wav");
    }

    private void load(String key, String path) {
        if (Gdx.files.internal(path).exists()) {
            sounds.put(key, Gdx.audio.newSound(Gdx.files.internal(path)));
        } else {
            System.out.println("Warning: Sound file not found: " + path);
        }
    }

    public void play(String key) {
        play(key, 1.0f);
    }

    public void play(String key, float pitch) {
        if (isMuted) return;
        Sound sound = sounds.get(key);
        if (sound != null) {
            // Play with volume and random slight pitch variation for realism
            float finalPitch = pitch * (0.95f + (float)Math.random() * 0.1f);
            sound.play(volume, finalPitch, 0);
        }
    }

    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
    }
}
