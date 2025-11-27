package com.arcane.onslaught.enemies;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and managing enemy spawns
 */
public class EnemyFactory {
    private Map<String, EnemyType> enemyTypes;

    public EnemyFactory() {
        enemyTypes = new HashMap<>();
        registerEnemyTypes();
    }

    private void registerEnemyTypes() {
        enemyTypes.put("zombie", new ZombieEnemy());
        enemyTypes.put("imp", new ImpEnemy());
        enemyTypes.put("tank", new TankEnemy());
        enemyTypes.put("runner", new RunnerEnemy());
        enemyTypes.put("swarm", new SwarmEnemy());
        enemyTypes.put("brute", new BruteEnemy());
        enemyTypes.put("ghost", new GhostEnemy());
        enemyTypes.put("elite", new EliteEnemy());
    }

    /**
     * Spawn a specific enemy type
     */
    public Entity spawnEnemy(Engine engine, String typeId, Vector2 position, float difficultyMultiplier) {
        EnemyType type = enemyTypes.get(typeId);
        if (type != null) {
            // Create a new instance and scale it
            EnemyType scaledType = createScaledInstance(typeId, difficultyMultiplier);
            return scaledType.spawn(engine, position);
        }
        return null;
    }

    /**
     * Spawn a random enemy based on difficulty
     */
    public Entity spawnRandomEnemy(Engine engine, Vector2 position, float difficulty) {
        String enemyType = selectEnemyType(difficulty);
        return spawnEnemy(engine, enemyType, position, difficulty);
    }

    /**
     * Select enemy type based on difficulty progression
     */
    private String selectEnemyType(float difficulty) {
        // Difficulty-based spawn pools
        if (difficulty < 1.5f) {
            // Early game: Zombies, Imps, Runners
            int choice = MathUtils.random(10);
            if (choice < 5) return "zombie";
            if (choice < 8) return "imp";
            return "runner";

        } else if (difficulty < 2.5f) {
            // Mid-game: Add Swarms, Brutes, Tanks
            int choice = MathUtils.random(10);
            if (choice < 3) return "zombie";
            if (choice < 5) return "imp";
            if (choice < 6) return "tank";
            if (choice < 8) return "brute";
            return "swarm";

        } else if (difficulty < 4.0f) {
            // Late game: Add Ghosts, Elites
            int choice = MathUtils.random(10);
            if (choice < 2) return "zombie";
            if (choice < 3) return "imp";
            if (choice < 4) return "tank";
            if (choice < 6) return "brute";
            if (choice < 8) return "ghost";
            if (choice < 9) return "swarm";
            return "elite";

        } else {
            // End game: All enemy types, more elites
            int choice = MathUtils.random(10);
            if (choice < 1) return "zombie";
            if (choice < 2) return "imp";
            if (choice < 3) return "tank";
            if (choice < 5) return "brute";
            if (choice < 7) return "ghost";
            if (choice < 8) return "swarm";
            return "elite";
        }
    }

    /**
     * Create a new scaled instance of an enemy type
     */
    private EnemyType createScaledInstance(String typeId, float multiplier) {
        EnemyType type;

        switch (typeId) {
            case "zombie": type = new ZombieEnemy(); break;
            case "imp": type = new ImpEnemy(); break;
            case "tank": type = new TankEnemy(); break;
            case "runner": type = new RunnerEnemy(); break;
            case "swarm": type = new SwarmEnemy(); break;
            case "brute": type = new BruteEnemy(); break;
            case "ghost": type = new GhostEnemy(); break;
            case "elite": type = new EliteEnemy(); break;
            default: type = new ZombieEnemy();
        }

        type.scale(multiplier);
        return type;
    }

    /**
     * Spawn multiple enemies at once (for swarms)
     */
    public void spawnSwarm(Engine engine, Vector2 centerPosition, String enemyType, int count, float difficulty) {
        for (int i = 0; i < count; i++) {
            // Spawn in a circle around center
            float angle = (360f / count) * i;
            float radius = 30f;

            Vector2 offset = new Vector2(radius, 0).rotateDeg(angle);
            Vector2 spawnPos = new Vector2(centerPosition).add(offset);

            spawnEnemy(engine, enemyType, spawnPos, difficulty);
        }
    }
}
