package com.arcane.onslaught.enemies;

import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.entities.components.BossComponent.BossSkill;
import com.arcane.onslaught.utils.TextureManager;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnemyFactory {
    private Map<String, EnemyType> enemyTypes;
    public enum BossArchetype { TITAN, BERSERKER, SPEEDSTER, TANK }

    private static final List<BossSkill> ELEMENTAL_SKILLS = Arrays.asList(
        BossSkill.ELECTRIC_AURA,
        BossSkill.FROST_BREATH,
        BossSkill.FIRE_FLAMETHROWER,
        BossSkill.POISON_SPIT,
        BossSkill.ARCANE_NOVA
    );
    private static final List<BossSkill> UTILITY_SKILLS = Arrays.asList(
        BossSkill.SUMMON_MINIONS,
        BossSkill.DASH_ATTACK,
        BossSkill.TELEPORT_AMBUSH,
        BossSkill.GRAVITY_WELL, // New
        BossSkill.SHOCKWAVE,    // New
        BossSkill.BERSERK       // New
    );

    public enum SpawnPattern {
        WALL_HORIZONTAL,
        WALL_VERTICAL,
        CIRCLE_ENCIRCLEMENT,
        TRIANGLE_WEDGE
    }

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
        enemyTypes.put("slime", new SlimeEnemy());
    }

    public void spawnBoss(Engine engine, Vector2 position, int playerLevel) {
        BossArchetype randomType = BossArchetype.values()[MathUtils.random(BossArchetype.values().length - 1)];
        spawnBoss(engine, position, playerLevel, randomType);
    }

    public void spawnBoss(Engine engine, Vector2 position, int playerLevel, BossArchetype archetype) {
        Entity boss = new Entity();

        BossSkill elementalSkill = ELEMENTAL_SKILLS.get(MathUtils.random(ELEMENTAL_SKILLS.size() - 1));
        BossSkill utilitySkill = UTILITY_SKILLS.get(MathUtils.random(UTILITY_SKILLS.size() - 1));

        String bossTitle = archetype.name() + " (" + elementalSkill.name() + ")";

        float baseHealth = 1200f + (playerLevel * 120f);
        float baseSpeed = 55f;
        float baseDamage = 25f;
        float size = 90f;

        // --- NEW: Level 10+ Difficulty Spike ---
        if (playerLevel >= 10) {
            baseHealth *= 2.0f; // Massive Health Buff
            baseDamage *= 2.0f; // Massive Damage Buff
            System.out.println("⚠️ BOSS ENRAGED! Stats Doubled.");
        }
        // ---------------------------------------

        switch (archetype) {
            case TITAN: baseHealth *= 2.0f; baseSpeed *= 0.6f; size *= 1.3f; break;
            case BERSERKER: baseHealth *= 0.8f; baseDamage *= 1.8f; baseSpeed *= 1.3f; break;
            case SPEEDSTER: baseHealth *= 0.7f; baseSpeed *= 1.6f; break;
            case TANK: baseHealth *= 1.5f; baseSpeed *= 0.5f; break;
        }

        TextureManager tm = TextureManager.getInstance();
        String textureName = "boss_void";
        Color tint = Color.WHITE;

        switch (elementalSkill) {
            case FIRE_FLAMETHROWER: textureName = "boss_fire"; tint = new Color(1f, 0.4f, 0.4f, 1f); break;
            case FROST_BREATH: textureName = "boss_frost"; tint = new Color(0.4f, 0.8f, 1f, 1f); break;
            case POISON_SPIT: textureName = "boss_poison"; tint = new Color(0.4f, 1f, 0.4f, 1f); break;
            case ELECTRIC_AURA: textureName = "boss_electric"; tint = new Color(1f, 1f, 0.4f, 1f); break;
            case ARCANE_NOVA: textureName = "boss_arcane"; tint = new Color(0.8f, 0.4f, 1f, 1f); break;
        }

        VisualComponent vis;
        if (tm.hasTexture(textureName)) {
            vis = new VisualComponent(size, size, tm.getTexture(textureName));
            vis.color = Color.WHITE;
        } else if (tm.hasTexture("boss_void")) {
            vis = new VisualComponent(size, size, tm.getTexture("boss_void"));
            vis.color = tint;
        } else {
            vis = new VisualComponent(size, size, tint);
        }
        vis.isFadingIn = true;
        vis.fadeInDuration = 2.6f;
        boss.add(vis);

        boss.add(new PositionComponent(position.x, position.y));
        boss.add(new VelocityComponent(baseSpeed));
        boss.add(new HealthComponent(baseHealth));
        boss.add(new EnemyComponent(baseDamage, 1500f));
        boss.add(new CollisionComponent(size / 2.5f, (short)0, (short)0));
        boss.add(new AIComponent());

        BossComponent bossComp = new BossComponent("Void Lord", bossTitle);
        bossComp.availableSkills.add(elementalSkill);
        bossComp.availableSkills.add(utilitySkill);
        boss.add(bossComp);

        engine.addEntity(boss);
        System.out.println("⚠️ BOSS SPAWNED: " + bossTitle);
    }

    // ... (Keep the rest of the file: spawnEnemy, spawnRandomEnemy, spawnPattern, etc. exactly the same) ...
    public Entity spawnEnemy(Engine engine, String typeId, Vector2 position, float difficultyMultiplier) {
        EnemyType type = enemyTypes.get(typeId);
        if (type != null) {
            EnemyType scaledType = createScaledInstance(typeId, difficultyMultiplier);
            Entity enemy = scaledType.spawn(engine, position);

            VisualComponent vis = enemy.getComponent(VisualComponent.class);
            if (vis != null) {
                vis.isFadingIn = true;
                vis.fadeInDuration = 1.6f;
            }

            float radius = 12f;
            switch (typeId) {
                case "swarm": radius = 6f; break;
                case "imp": radius = 10f; break;
                case "slime": radius = 10f; break;
                case "tank": radius = 20f; break;
                case "brute": radius = 18f; break;
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
            int choice = MathUtils.random(10);
            if (choice < 4) return "zombie";
            if (choice < 7) return "slime";
            if (choice < 9) return "imp";
            return "runner";
        } else if (difficulty < 2.5f) {
            int choice = MathUtils.random(12);
            if (choice < 3) return "zombie";
            if (choice < 5) return "slime";
            if (choice < 7) return "imp";
            if (choice < 8) return "tank";
            if (choice < 10) return "brute";
            return "swarm";
        } else if (difficulty < 4.0f) {
            int choice = MathUtils.random(10);
            if (choice < 2) return "zombie";
            if (choice < 3) return "imp";
            if (choice < 4) return "tank";
            if (choice < 6) return "brute";
            if (choice < 8) return "ghost";
            if (choice < 9) return "swarm";
            return "elite";
        } else {
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

    public void spawnPattern(Engine engine, Vector2 playerPos, SpawnPattern pattern, String enemyType, int count, float difficulty) {
        float spacing = 40f;
        switch (pattern) {
            case WALL_HORIZONTAL:
                float startX = playerPos.x - (count * spacing) / 2;
                // Spawn strictly above or below the visible area relative to player
                float yPos = playerPos.y + (MathUtils.randomBoolean() ? 800f : -800f);
                for (int i = 0; i < count; i++) {
                    spawnEnemy(engine, enemyType, new Vector2(startX + i * spacing, yPos), difficulty);
                }
                break;
            case WALL_VERTICAL:
                float startY = playerPos.y - (count * spacing) / 2;
                // Spawn strictly left or right
                float xPos = playerPos.x + (MathUtils.randomBoolean() ? 1000f : -1000f);
                for (int i = 0; i < count; i++) {
                    spawnEnemy(engine, enemyType, new Vector2(xPos, startY + i * spacing), difficulty);
                }
                break;
            case CIRCLE_ENCIRCLEMENT:
                // Ensure radius is large enough to be off-screen or at least not instant death
                float radius = 700f;
                for (int i = 0; i < count; i++) {
                    float angle = (360f / count) * i;
                    Vector2 offset = new Vector2(radius, 0).rotateDeg(angle);
                    spawnEnemy(engine, enemyType, new Vector2(playerPos).add(offset), difficulty);
                }
                break;
            case TRIANGLE_WEDGE:
                // Ensure the wedge origin is far away (min 600 units)
                Vector2 spawnOffset = new Vector2(MathUtils.randomBoolean() ? 1 : -1, 0)
                    .setToRandomDirection()
                    .scl(MathUtils.random(700f, 900f));

                Vector2 spawnOrigin = new Vector2(playerPos).add(spawnOffset);

                for (int i = 0; i < count; i++) {
                    float rowOffset = (i % 3) * spacing;
                    float colOffset = (i / 3) * spacing;
                    spawnEnemy(engine, enemyType, new Vector2(spawnOrigin.x + rowOffset, spawnOrigin.y + colOffset), difficulty);
                }
                break;
        }
    }

    public void spawnSwarm(Engine engine, Vector2 centerPosition, String enemyType, int count, float difficulty) {
        spawnPattern(engine, centerPosition, SpawnPattern.CIRCLE_ENCIRCLEMENT, enemyType, count, difficulty);
    }
}
