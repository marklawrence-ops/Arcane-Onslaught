package com.arcane.onslaught.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, Sound> sounds;
    private Map<String, Music> musicTracks;

    // Volume Settings (0.0 to 1.0)
    // Defaults set lower to prevent ear blasting
    private float masterVolume = 0.3f;
    private float sfxVolume = 1.0f;
    private float musicVolume = 0.8f;

    private boolean isMuted = false;
    private Music currentMusic;
    private String currentMusicKey;

    private SoundManager() {
        sounds = new HashMap<>();
        musicTracks = new HashMap<>();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void loadSounds() {
        // SFX
        loadSound("cast", "sounds/sfx_cast.wav");
        loadSound("hit", "sounds/sfx_hit.wav");
        loadSound("explosion", "sounds/sfx_explosion.wav");
        loadSound("levelup", "sounds/sfx_levelup.wav");
        loadSound("pickup", "sounds/sfx_pickup.wav");
        loadSound("ui_click", "sounds/sfx_ui_click.wav");
        loadSound("ui_hover", "sounds/sfx_ui_hover.wav");
        loadSound("fanfare", "sounds/sfx_fanfare.wav");
        loadSound("spawn_breach", "sounds/sfx_spawn_breach.wav");
        loadSound("gameover", "sounds/sfx_gameover.wav");

        // MUSIC
        loadMusic("abyss", "sounds/bgm_abyss.wav");
    }

    private void loadSound(String key, String path) {
        if (Gdx.files.internal(path).exists()) {
            sounds.put(key, Gdx.audio.newSound(Gdx.files.internal(path)));
        }
    }

    private void loadMusic(String key, String path) {
        if (Gdx.files.internal(path).exists()) {
            musicTracks.put(key, Gdx.audio.newMusic(Gdx.files.internal(path)));
        }
    }

    // --- SFX METHODS ---
    public void play(String key) { play(key, 1.0f); }

    public void play(String key, float pitch) {
        if (isMuted) return;
        Sound sound = sounds.get(key);
        if (sound != null) {
            // Volume = Master * SFX
            float finalVol = masterVolume * sfxVolume;
            if (finalVol > 0) {
                long id = sound.play(finalVol);
                sound.setPitch(id, pitch);
            }
        }
    }

    // --- MUSIC METHODS ---
    public void playMusic(String key) {
        if (isMuted) return;

        // Don't restart if already playing
        if (currentMusicKey != null && currentMusicKey.equals(key) && currentMusic != null && currentMusic.isPlaying()) {
            return;
        }

        stopMusic();

        Music music = musicTracks.get(key);
        if (music != null) {
            currentMusic = music;
            currentMusicKey = key;
            currentMusic.setLooping(true);
            updateMusicVolume(); // Apply current volume
            currentMusic.play();
        }
    }

    public void stopMusic() {
        if (currentMusic != null) currentMusic.stop();
    }

    private void updateMusicVolume() {
        if (currentMusic != null) {
            currentMusic.setVolume(masterVolume * musicVolume);
        }
    }

    // --- VOLUME CONTROLS ---
    public void setMasterVolume(float vol) {
        this.masterVolume = Math.max(0, Math.min(1, vol));
        updateMusicVolume();
    }

    public void setSFXVolume(float vol) {
        this.sfxVolume = Math.max(0, Math.min(1, vol));
    }

    public void setMusicVolume(float vol) {
        this.musicVolume = Math.max(0, Math.min(1, vol));
        updateMusicVolume();
    }

    public float getMasterVolume() { return masterVolume; }
    public float getSFXVolume() { return sfxVolume; }
    public float getMusicVolume() { return musicVolume; }

    public void dispose() {
        for (Sound sound : sounds.values()) sound.dispose();
        for (Music music : musicTracks.values()) music.dispose();
        sounds.clear();
        musicTracks.clear();
    }
}
