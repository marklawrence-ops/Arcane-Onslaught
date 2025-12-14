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
    private int currentPlayerLevel = 1; // --- NEW: Track level for scaling ---
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

        // 1. Get current player level every frame
        fetchPlayerLevel();

        // 2. Update Difficulty & Spawn Rate
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

    // --- NEW: Helper to get level ---
    private void fetchPlayerLevel() {
        Family playerFamily = Family.all(PlayerComponent.class).get();
        ImmutableArray<Entity> players = getEngine().getEntitiesFor(playerFamily);
        if (players.size() > 0) {
            currentPlayerLevel = playerMapper.get(players.get(0)).level;
        }
    }

    private void updateDifficulty() {
        // --- 1. Base Time Scaling (Existing) ---
        if (gameTime < 300) {
            difficulty = 1.0f + (gameTime / 60f) * 0.2f;
        } else if (gameTime < 600) {
            difficulty = 2.0f + ((gameTime - 300) / 60f) * 0.5f;
        } else {
            float lateGameTime = (gameTime - 600) / 60f;
            difficulty = 4.5f * (float)Math.pow(1.15, lateGameTime);
        }

        // Calculate base interval from time
        spawnInterval = Constants.ENEMY_SPAWN_INTERVAL / (1f + (difficulty - 1f) * 0.4f);

        // --- 2. NEW: Player Level Scaling (Level 30+) ---
        if (currentPlayerLevel >= 20) {
            // Calculate how many levels past 30 we are
            int levelsPast = currentPlayerLevel - 30;

            // Decrease spawn interval by 5% for every level past 30
            // Example: Level 40 = 1.5x spawn rate
            float levelMultiplier = 1.0f + (levelsPast * 0.05f);

            spawnInterval = spawnInterval / levelMultiplier;
        }
        // ------------------------------------------------

        // Cap minimum interval to prevent crashes (0.1s is insane speed)
        spawnInterval = Math.max(0.10f, spawnInterval);
    }

    private void checkBossSpawn() {
        // (Logic simplified since we fetch level in update() now)
        if (currentPlayerLevel > 0 && currentPlayerLevel % 5 == 0 && currentPlayerLevel > lastBossLevel) {
            lastBossLevel = currentPlayerLevel;
            triggerBossEvent(currentPlayerLevel);
        }
    }

    private void triggerBossEvent(int level) {
        SoundManager.getInstance().play("spawn_breach", 1.0f);

        Vector2 spawnPos = getRandomEdgePosition(200f);

        // 1. Primary Boss
        factory.spawnBoss(getEngine(), spawnPos, level);

        // 2. Secondary Boss (Level 40+)
        if (level >= 30) {
            Vector2 secondPos = new Vector2(spawnPos).add(150, 0);
            factory.spawnBoss(getEngine(), secondPos, level);
            System.out.println("⚠️ DOUBLE BOSS EVENT!");
        }

        // 3. Tertiary Boss (Level 60+)
        if (level >= 60) {
            Vector2 thirdPos = new Vector2(spawnPos).add(-150, 0);
            factory.spawnBoss(getEngine(), thirdPos, level);
            System.out.println("⚠️ TRIPLE BOSS EVENT!");
        }

        factory.spawnSwarm(getEngine(), spawnPos, "imp", 4, difficulty);
        System.out.println(">>> BOSS WAVE STARTED (Level " + level + ") <<<");
    }

    // ... (triggerSpecialWave, spawnEnemies, getRandomEdgePosition remain unchanged) ...
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
        factory.spawnPattern(getEngine(), playerPos, pattern, enemyType, count, difficulty);
        SoundManager.getInstance().play("spawn_breach", 0.8f);
    }

    private void spawnEnemies() {
        Vector2 spawnPos = getRandomEdgePosition(50f);
        float swarmChance = Math.min(0.3f, difficulty * 0.05f);
        if (MathUtils.random() < swarmChance) {
            int count = 3 + MathUtils.random(2);
            factory.spawnSwarm(getEngine(), spawnPos, "swarm", count, difficulty);
        } else {
            factory.spawnRandomEnemy(getEngine(), spawnPos, difficulty);
        }
    }

    private Vector2 getRandomEdgePosition(float buffer) {
        int edge = MathUtils.random(3);
        float x, y;
        float minX = Constants.ARENA_OFFSET_X - buffer;
        float maxX = Constants.ARENA_OFFSET_X + Constants.ARENA_WIDTH + buffer;
        float minY = Constants.ARENA_OFFSET_Y - buffer;
        float maxY = Constants.ARENA_OFFSET_Y + Constants.ARENA_HEIGHT + buffer;
        switch (edge) {
            case 0: x = MathUtils.random(minX, maxX); y = maxY; break;
            case 1: x = maxX; y = MathUtils.random(minY, maxY); break;
            case 2: x = MathUtils.random(minX, maxX); y = minY; break;
            default: x = minX; y = MathUtils.random(minY, maxY); break;
        }
        return new Vector2(x, y);
    }

    private Vector2 getRandomEdgePosition() {
        return getRandomEdgePosition(50f);
    }
}
