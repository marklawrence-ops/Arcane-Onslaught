package com.arcane.onslaught.utils;

/**
 * Game-wide constants and configuration values
 */
public class Constants {

    // Screen dimensions
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    // Arena boundaries
    public static final float ARENA_WIDTH = 1200;
    public static final float ARENA_HEIGHT = 680;
    public static final float ARENA_OFFSET_X = (SCREEN_WIDTH - ARENA_WIDTH) / 2;
    public static final float ARENA_OFFSET_Y = (SCREEN_HEIGHT - ARENA_HEIGHT) / 2;

    // Player settings
    public static final float PLAYER_SPEED = 200f;
    public static final float PLAYER_SIZE = 32f;
    public static final float PLAYER_MAX_HEALTH = 100f;

    // Enemy settings
    public static final float ENEMY_SPEED = 80f;
    public static final float ENEMY_SIZE = 24f;
    public static final float ENEMY_SPAWN_INTERVAL = 2f; // seconds

    // Spell settings
    public static final float MAGIC_BOLT_COOLDOWN = 1.5f; // seconds
    public static final float MAGIC_BOLT_SPEED = 300f;
    public static final float MAGIC_BOLT_DAMAGE = 10f;
    public static final float MAGIC_BOLT_SIZE = 16f;

    // XP settings
    public static final float XP_ORB_SIZE = 12f;
    public static final float XP_ORB_COLLECTION_RANGE = 50f;
    public static final float XP_PER_ENEMY = 20f;
    public static final float XP_TO_LEVEL_BASE = 10f;

    // Physics
    public static final float WORLD_TO_BOX = 0.01f;
    public static final float BOX_TO_WORLD = 100f;

    // Collision categories (for Box2D)
    public static final short CATEGORY_PLAYER = 0x0001;
    public static final short CATEGORY_ENEMY = 0x0002;
    public static final short CATEGORY_PROJECTILE = 0x0004;
    public static final short CATEGORY_XP_ORB = 0x0008;

    private Constants() {
        // Prevent instantiation
    }
}
