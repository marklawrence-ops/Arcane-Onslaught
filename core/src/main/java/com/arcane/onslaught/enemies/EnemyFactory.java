package com.arcane.onslaught.enemies;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.CollisionComponent;

import java.util.HashMap;
import java.util.Map;

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
        // --- NEW: Register Slime ---
        enemyTypes.put("slime", new SlimeEnemy());
    }

    public Entity spawnEnemy(Engine engine, String typeId, Vector2 position, float difficultyMultiplier) {
        EnemyType type = enemyTypes.get(typeId);
        if (type != null) {
            EnemyType scaledType = createScaledInstance(typeId, difficultyMultiplier);
            Entity enemy = scaledType.spawn(engine, position);

            float radius = 12f;
            switch (typeId) {
                case "swarm": radius = 6f; break;
                case "imp": radius = 10f; break;
                case "runner": radius = 10f; break;
                // --- NEW: Slime Hitbox ---
                case "slime": radius = 10f; break;
                case "zombie": radius = 12f; break;
                case "ghost": radius = 12f; break;
                case "brute": radius = 18f; break;
                case "tank": radius = 20f; break;
                case "elite": radius = 22f; break;
            }

            enemy.add(new CollisionComponent(radius, (short)0, (short)0));
            return enemy;
        }
        return null;
    }

    public Entity spawnRandomEnemy(Engine engine, Vector2 position, float difficulty) {
        String enemyType = selectEnemyType(difficulty);
        return spawnEnemy(engine, enemyType, position, difficulty);
    }

    private String selectEnemyType(float difficulty) {
        if (difficulty < 1.5f) {
            // Early Game: Add Slimes
            int choice = MathUtils.random(10);
            if (choice < 4) return "zombie";
            if (choice < 7) return "slime"; // 30% chance for Slime
            if (choice < 9) return "imp";
            return "runner";
        } else if (difficulty < 2.5f) {
            // Mid Game
            int choice = MathUtils.random(12); // Increased range
            if (choice < 3) return "zombie";
            if (choice < 5) return "slime";
            if (choice < 7) return "imp";
            if (choice < 8) return "tank";
            if (choice < 10) return "brute";
            return "swarm";
        } else if (difficulty < 4.0f) {
            // Late Game
            int choice = MathUtils.random(10);
            if (choice < 2) return "zombie";
            if (choice < 3) return "imp";
            if (choice < 4) return "tank";
            if (choice < 6) return "brute";
            if (choice < 8) return "ghost";
            if (choice < 9) return "swarm";
            return "elite";
        } else {
            // End Game
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
            case "slime": type = new SlimeEnemy(); break;
            default: type = new ZombieEnemy();
        }
        type.scale(multiplier);
        return type;
    }

    public void spawnSwarm(Engine engine, Vector2 centerPosition, String enemyType, int count, float difficulty) {
        for (int i = 0; i < count; i++) {
            float angle = (360f / count) * i;
            float radius = 30f;
            Vector2 offset = new Vector2(radius, 0).rotateDeg(angle);
            Vector2 spawnPos = new Vector2(centerPosition).add(offset);
            spawnEnemy(engine, enemyType, spawnPos, difficulty);
        }
    }
}
