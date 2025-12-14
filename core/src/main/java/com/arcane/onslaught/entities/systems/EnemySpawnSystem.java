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
import com.arcane.onslaught.enemies.EnemyFactory.SpawnPattern; // Import
import com.arcane.onslaught.utils.Constants;

public class EnemySpawnSystem extends EntitySystem {
    private EnemyFactory factory;
    private float spawnTimer = 0;
    private float gameTime = 0;
    private float difficulty = 1.0f;
    private float spawnInterval;

    // --- NEW: Pattern Management ---
    private float patternTimer = 0f;
    private float patternInterval = 20.0f; // Special pattern every 20s
    // -------------------------------

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

        if (isBossActive()) return;

        spawnTimer += deltaTime;
        patternTimer += deltaTime; // Update pattern timer

        updateDifficulty();
        checkBossSpawn();

        // 1. Regular Spawns
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            spawnEnemies();
        }

        // 2. Special Patterns (Vampire Survivors Style)
        if (patternTimer >= patternInterval) {
            patternTimer = 0;
            triggerSpecialWave();
        }
    }

    private void updateDifficulty() {
        // --- UPDATED SCALING ---
        // Early Game (0-5 mins): Linear scaling
        if (gameTime < 300) {
            difficulty = 1.0f + (gameTime / 60f) * 0.2f;
        }
        // Mid Game (5-10 mins): Steeper
        else if (gameTime < 600) {
            difficulty = 2.0f + ((gameTime - 300) / 60f) * 0.5f;
        }
        // Late Game (10+ mins): Exponential "AFK Killer" scaling
        else {
            float lateGameTime = (gameTime - 600) / 60f; // Minutes past 10
            difficulty = 4.5f * (float)Math.pow(1.15, lateGameTime);
        }

        // Cap Spawn Rate
        spawnInterval = Constants.ENEMY_SPAWN_INTERVAL / (1f + (difficulty - 1f) * 0.4f);
        spawnInterval = Math.max(0.15f, spawnInterval); // Allow very fast spawns (0.15s) in late game
    }

    private void triggerSpecialWave() {
        // Find Player Position
        Vector2 playerPos = new Vector2(0, 0);
        Family playerFamily = Family.all(PlayerComponent.class).get();
        ImmutableArray<Entity> players = getEngine().getEntitiesFor(playerFamily);
        if (players.size() > 0) {
            playerPos.set(posMapper.get(players.get(0)).position);
        }

        // Pick Random Pattern
        SpawnPattern pattern = SpawnPattern.values()[MathUtils.random(SpawnPattern.values().length - 1)];
        int count = 10 + (int)(difficulty * 2); // Pattern size scales with difficulty

        // Pick Tougher Enemy Type for Patterns
        String enemyType = "imp";
        if (difficulty > 3.0f) enemyType = "tank";
        if (difficulty > 5.0f) enemyType = "elite";

        factory.spawnPattern(getEngine(), playerPos, pattern, enemyType, count, difficulty);

        // Warning Sound
        SoundManager.getInstance().play("spawn_breach", 0.8f);
        System.out.println(">>> SPECIAL WAVE: " + pattern.name() + " (" + count + " " + enemyType + "s) <<<");
    }

    // ... (isBossActive, checkBossSpawn, triggerBossEvent, spawnEnemies, getRandomEdgePosition remain same) ...
    private boolean isBossActive() {
        Family bossFamily = Family.all(BossComponent.class).get();
        return getEngine().getEntitiesFor(bossFamily).size() > 0;
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
        Vector2 spawnPos = getRandomEdgePosition();
        factory.spawnBoss(getEngine(), spawnPos, level);
        factory.spawnSwarm(getEngine(), spawnPos, "imp", 4, difficulty);
        System.out.println(">>> BOSS WAVE STARTED <<<");
    }

    private void spawnEnemies() {
        Vector2 spawnPos = getRandomEdgePosition();
        float swarmChance = Math.min(0.3f, difficulty * 0.05f);
        if (MathUtils.random() < swarmChance) {
            int count = 3 + MathUtils.random(2);
            factory.spawnSwarm(getEngine(), spawnPos, "swarm", count, difficulty);
        } else {
            factory.spawnRandomEnemy(getEngine(), spawnPos, difficulty);
        }
    }

    private Vector2 getRandomEdgePosition() {
        int edge = MathUtils.random(3);
        float x, y;
        switch (edge) {
            case 0: x = MathUtils.random(Constants.ARENA_OFFSET_X, Constants.ARENA_OFFSET_X + Constants.ARENA_WIDTH); y = Constants.ARENA_OFFSET_Y + Constants.ARENA_HEIGHT; break;
            case 1: x = Constants.ARENA_OFFSET_X + Constants.ARENA_WIDTH; y = MathUtils.random(Constants.ARENA_OFFSET_Y, Constants.ARENA_OFFSET_Y + Constants.ARENA_HEIGHT); break;
            case 2: x = MathUtils.random(Constants.ARENA_OFFSET_X, Constants.ARENA_OFFSET_X + Constants.ARENA_WIDTH); y = Constants.ARENA_OFFSET_Y; break;
            default: x = Constants.ARENA_OFFSET_X; y = MathUtils.random(Constants.ARENA_OFFSET_Y, Constants.ARENA_OFFSET_Y + Constants.ARENA_HEIGHT); break;
        }
        return new Vector2(x, y);
    }
}
