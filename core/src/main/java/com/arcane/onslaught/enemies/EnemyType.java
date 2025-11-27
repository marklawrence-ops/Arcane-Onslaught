package com.arcane.onslaught.enemies;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;

/**
 * Base class for different enemy types
 */
public abstract class EnemyType {
    protected String name;
    protected float health;
    protected float speed;
    protected float size;
    protected float xpDrop;
    protected Color color;

    public EnemyType(String name, float health, float speed, float size, float xpDrop, Color color) {
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.size = size;
        this.xpDrop = xpDrop;
        this.color = color;
    }

    public Entity spawn(Engine engine, Vector2 position) {
        Entity enemy = new Entity();

        enemy.add(new PositionComponent(position.x, position.y));
        enemy.add(new VelocityComponent(speed));
        enemy.add(new VisualComponent(size, size, color));
        enemy.add(new HealthComponent(health));

        EnemyComponent ec = new EnemyComponent();
        ec.enemyType = name;
        ec.xpDropped = xpDrop;
        enemy.add(ec);

        enemy.add(new AIComponent());

        // Add special components for this enemy type
        addSpecialComponents(enemy);

        engine.addEntity(enemy);
        return enemy;
    }

    /**
     * Override to add special components for specific enemy types
     */
    protected void addSpecialComponents(Entity enemy) {
        // Default: no special components
    }

    /**
     * Scale this enemy type for difficulty
     */
    public void scale(float multiplier) {
        health *= multiplier;
        speed *= (1f + (multiplier - 1f) * 0.3f); // Speed scales slower
    }

    public String getName() { return name; }
}

// ============================================
// BASIC ZOMBIE - Slow, tanky
// ============================================

class ZombieEnemy extends EnemyType {
    public ZombieEnemy() {
        super(
            "Zombie",
            50f,    // High health
            60f,    // Slow
            28f,    // Medium size
            5f,     // Standard XP
            new Color(0.4f, 0.8f, 0.3f, 1f) // Greenish
        );
    }
}

// ============================================
// IMP - Fast, weak
// ============================================

class ImpEnemy extends EnemyType {
    public ImpEnemy() {
        super(
            "Imp",
            15f,    // Low health
            140f,   // Very fast
            18f,    // Small
            10f,     // Low XP
            new Color(1f, 0.3f, 0.3f, 1f) // Bright red
        );
    }
}

// ============================================
// TANK - Very slow, very tanky
// ============================================

class TankEnemy extends EnemyType {
    public TankEnemy() {
        super(
            "Tank",
            120f,   // Very high health
            40f,    // Very slow
            40f,    // Large
            30f,    // High XP
            new Color(0.5f, 0.5f, 0.5f, 1f) // Gray
        );
    }
}

// ============================================
// RUNNER - Medium speed, medium health
// ============================================

class RunnerEnemy extends EnemyType {
    public RunnerEnemy() {
        super(
            "Runner",
            30f,    // Medium health
            100f,   // Fast
            24f,    // Medium size
            20f,     // Medium XP
            new Color(1f, 0.7f, 0.2f, 1f) // Orange
        );
    }
}

// ============================================
// SWARM - Tiny, weak, spawns in groups
// ============================================

class SwarmEnemy extends EnemyType {
    public SwarmEnemy() {
        super(
            "Swarm",
            8f,     // Very low health
            110f,   // Fast
            14f,    // Tiny
            5f,     // Low XP
            new Color(1f, 1f, 0.3f, 1f) // Yellow
        );
    }
}

// ============================================
// BRUTE - Strong and moderately fast
// ============================================

class BruteEnemy extends EnemyType {
    public BruteEnemy() {
        super(
            "Brute",
            80f,    // High health
            70f,    // Medium-slow
            36f,    // Large
            18f,    // Good XP
            new Color(0.8f, 0.2f, 0.2f, 1f) // Dark red
        );
    }
}

// ============================================
// GHOST - Fast, phases through others
// ============================================

class GhostEnemy extends EnemyType {
    public GhostEnemy() {
        super(
            "Ghost",
            25f,    // Low-medium health
            120f,   // Very fast
            22f,    // Small-medium
            15f,     // Medium XP
            new Color(0.6f, 0.6f, 1f, 0.7f) // Transparent blue
        );
    }
}

// ============================================
// ELITE - Rare, powerful
// ============================================

class EliteEnemy extends EnemyType {
    public EliteEnemy() {
        super(
            "Elite",
            150f,   // Very high health
            80f,    // Medium speed
            44f,    // Very large
            50f,    // Lots of XP
            new Color(1f, 0.2f, 1f, 1f) // Magenta/Purple
        );
    }
}
