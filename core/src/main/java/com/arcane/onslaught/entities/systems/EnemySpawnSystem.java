package com.arcane.onslaught.entities.systems;

import com.arcane.onslaught.entities.components.BossComponent;
import com.arcane.onslaught.entities.components.PlayerComponent;
import com.arcane.onslaught.entities.components.PositionComponent;
import com.arcane.onslaught.utils.SoundManager;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.enemies.EnemyFactory;
import com.arcane.onslaught.enemies.EnemyFactory.SpawnPattern;
import com.arcane.onslaught.utils.Constants;

public class EnemySpawnSystem extends EntitySystem {
    private EnemyFactory factory;
    private float spawnTimer = 0;
    private float gameTime = 0;
    private float difficulty = 1.0f;
    private float spawnInterval;

    private float patternTimer = 0f;
    private float patternInterval = 20.0f;

    private int lastBossLevel = 0;
    private ComponentMapper<PlayerComponent> playerMapper = ComponentMapper.getFor(PlayerComponent.class);
    private ComponentMapper<PositionComponent> posMapper = ComponentMapper.getFor(PositionComponent.class);

    public EnemySpawnSystem(EnemyFactory factory) {
        this.factory = factory;
        this.spawnInterval = Constants.ENEMY_SPAWN_INTERVAL;
    }

    @Override
    public void update(float deltaTime) {
        gameTime += deltaTime;
        spawnTimer += deltaTime;
        patternTimer += deltaTime;

        updateDifficulty();
        checkBossSpawn();

        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            spawnEnemies();
        }

        if (patternTimer >= patternInterval) {
            patternTimer = 0;
            triggerSpecialWave();
        }
    }

    private void updateDifficulty() {
        if (gameTime < 300) {
            difficulty = 1.0f + (gameTime / 60f) * 0.2f;
        } else if (gameTime < 600) {
            difficulty = 2.0f + ((gameTime - 300) / 60f) * 0.5f;
        } else {
            float lateGameTime = (gameTime - 600) / 60f;
            difficulty = 4.5f * (float)Math.pow(1.15, lateGameTime);
        }
        spawnInterval = Constants.ENEMY_SPAWN_INTERVAL / (1f + (difficulty - 1f) * 0.4f);
        spawnInterval = Math.max(0.15f, spawnInterval);
    }

    private void checkBossSpawn() {
        Family playerFamily = Family.all(PlayerComponent.class).get();
        ImmutableArray<Entity> players = getEngine().getEntitiesFor(playerFamily);
        if (players.size() > 0) {
            Entity player = players.get(0);
            PlayerComponent pc = playerMapper.get(player);

            if (pc.level > 0 && pc.level % 5 == 0 && pc.level > lastBossLevel) {
                lastBossLevel = pc.level;
                triggerBossEvent(pc.level);
            }
        }
    }

    private void triggerBossEvent(int level) {
        SoundManager.getInstance().play("spawn_breach", 1.0f);

        // --- FIX: Ensure Boss spawns FAR away ---
        Vector2 spawnPos = getRandomEdgePosition(200f); // 200px buffer outside screen

        factory.spawnBoss(getEngine(), spawnPos, level);

        if (level >= 40) {
            Vector2 secondPos = new Vector2(spawnPos).add(150, 0);
            factory.spawnBoss(getEngine(), secondPos, level);
            System.out.println("⚠️ DOUBLE BOSS EVENT!");
        }

        factory.spawnSwarm(getEngine(), spawnPos, "imp", 4, difficulty);
        System.out.println(">>> BOSS WAVE STARTED (Level " + level + ") <<<");
    }

    private void triggerSpecialWave() {
        Vector2 playerPos = new Vector2(0, 0);
        Family playerFamily = Family.all(PlayerComponent.class).get();
        ImmutableArray<Entity> players = getEngine().getEntitiesFor(playerFamily);
        if (players.size() > 0) {
            playerPos.set(posMapper.get(players.get(0)).position);
        }

        SpawnPattern pattern = SpawnPattern.values()[MathUtils.random(SpawnPattern.values().length - 1)];
        int count = 10 + (int)(difficulty * 2);

        String enemyType = "imp";
        if (difficulty > 3.0f) enemyType = "tank";
        if (difficulty > 5.0f) enemyType = "elite";

        // --- FIX: Don't spawn patterns ON TOP of player ---
        // Pass a modified position or ensure factory handles logic to not spawn on player
        // For patterns like "Circle", we actually want them around the player, but at a distance.
        // The factory handles the offset, so we pass playerPos directly.
        factory.spawnPattern(getEngine(), playerPos, pattern, enemyType, count, difficulty);

        SoundManager.getInstance().play("spawn_breach", 0.8f);
    }

    private void spawnEnemies() {
        // --- FIX: Standard enemies spawn 50px outside screen ---
        Vector2 spawnPos = getRandomEdgePosition(50f);

        float swarmChance = Math.min(0.3f, difficulty * 0.05f);
        if (MathUtils.random() < swarmChance) {
            int count = 3 + MathUtils.random(2);
            factory.spawnSwarm(getEngine(), spawnPos, "swarm", count, difficulty);
        } else {
            factory.spawnRandomEnemy(getEngine(), spawnPos, difficulty);
        }
    }

    // --- UPDATED: Accepts a buffer distance ---
    private Vector2 getRandomEdgePosition(float buffer) {
        int edge = MathUtils.random(3);
        float x, y;

        float minX = Constants.ARENA_OFFSET_X - buffer;
        float maxX = Constants.ARENA_OFFSET_X + Constants.ARENA_WIDTH + buffer;
        float minY = Constants.ARENA_OFFSET_Y - buffer;
        float maxY = Constants.ARENA_OFFSET_Y + Constants.ARENA_HEIGHT + buffer;

        switch (edge) {
            case 0: // Top
                x = MathUtils.random(minX, maxX);
                y = maxY;
                break;
            case 1: // Right
                x = maxX;
                y = MathUtils.random(minY, maxY);
                break;
            case 2: // Bottom
                x = MathUtils.random(minX, maxX);
                y = minY;
                break;
            default: // Left
                x = minX;
                y = MathUtils.random(minY, maxY);
                break;
        }
        return new Vector2(x, y);
    }

    // Overload for backward compatibility if needed, though we updated calls above
    private Vector2 getRandomEdgePosition() {
        return getRandomEdgePosition(50f);
    }
}
