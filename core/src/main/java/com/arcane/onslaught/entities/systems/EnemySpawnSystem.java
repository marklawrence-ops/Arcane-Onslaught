package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.enemies.EnemyFactory;
import com.arcane.onslaught.utils.Constants;

/**
 * Spawns enemies using the factory system with difficulty scaling
 */
public class EnemySpawnSystem extends EntitySystem {
    private EnemyFactory factory;
    private float spawnTimer = 0;
    private float gameTime = 0;
    private float difficulty = 1.0f;
    private float spawnInterval;

    public EnemySpawnSystem(EnemyFactory factory) {
        this.factory = factory;
        this.spawnInterval = Constants.ENEMY_SPAWN_INTERVAL;
    }

    @Override
    public void update(float deltaTime) {
        gameTime += deltaTime;
        spawnTimer += deltaTime;

        // Update difficulty every 30 seconds
        updateDifficulty();

        // Check if it's time to spawn
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            spawnEnemies();
        }
    }

    private void updateDifficulty() {
        // Difficulty increases over time
        // Every 30 seconds: +0.15 difficulty
        difficulty = 1.0f + (gameTime / 30f) * 0.15f;

        // Also spawn faster as difficulty increases
        spawnInterval = Constants.ENEMY_SPAWN_INTERVAL / (1f + (difficulty - 1f) * 0.5f);

        // Cap spawn rate at 0.5 seconds minimum
        spawnInterval = Math.max(0.5f, spawnInterval);

        // Every minute, print difficulty update
        if (((int)gameTime) % 60 == 0 && ((int)gameTime) > 0) {
            System.out.println("═══════════════════════════════════");
            System.out.println("Difficulty increased: " + String.format("%.2f", difficulty) + "x");
            System.out.println("Spawn rate: " + String.format("%.2f", spawnInterval) + "s");
            System.out.println("═══════════════════════════════════");
        }
    }

    private void spawnEnemies() {
        Vector2 spawnPos = getRandomEdgePosition();

        // Chance to spawn swarm (increases with difficulty)
        float swarmChance = Math.min(0.3f, difficulty * 0.05f);

        if (MathUtils.random() < swarmChance) {
            // Spawn swarm of 3-5 enemies
            int count = 3 + MathUtils.random(2);
            factory.spawnSwarm(getEngine(), spawnPos, "swarm", count, difficulty);
        } else {
            // Spawn single random enemy
            factory.spawnRandomEnemy(getEngine(), spawnPos, difficulty);
        }
    }

    private Vector2 getRandomEdgePosition() {
        int edge = MathUtils.random(3);
        float x, y;

        switch (edge) {
            case 0: // Top
                x = MathUtils.random(Constants.ARENA_OFFSET_X,
                    Constants.ARENA_OFFSET_X + Constants.ARENA_WIDTH);
                y = Constants.ARENA_OFFSET_Y + Constants.ARENA_HEIGHT;
                break;

            case 1: // Right
                x = Constants.ARENA_OFFSET_X + Constants.ARENA_WIDTH;
                y = MathUtils.random(Constants.ARENA_OFFSET_Y,
                    Constants.ARENA_OFFSET_Y + Constants.ARENA_HEIGHT);
                break;

            case 2: // Bottom
                x = MathUtils.random(Constants.ARENA_OFFSET_X,
                    Constants.ARENA_OFFSET_X + Constants.ARENA_WIDTH);
                y = Constants.ARENA_OFFSET_Y;
                break;

            default: // Left
                x = Constants.ARENA_OFFSET_X;
                y = MathUtils.random(Constants.ARENA_OFFSET_Y,
                    Constants.ARENA_OFFSET_Y + Constants.ARENA_HEIGHT);
                break;
        }

        return new Vector2(x, y);
    }

    public float getDifficulty() {
        return difficulty;
    }
}
