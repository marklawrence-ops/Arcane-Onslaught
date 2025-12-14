package com.arcane.onslaught.enemies;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.utils.TextureManager;

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
    protected String textureKey; // NEW: Key for texture lookup

    public EnemyType(String name, float health, float speed, float size, float xpDrop, Color color) {
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.size = size;
        this.xpDrop = xpDrop;
        this.color = color;
        this.textureKey = name.toLowerCase(); // Default: use lowercase name as texture key
    }

    public Entity spawn(Engine engine, Vector2 position) {
        Entity enemy = new Entity();

        enemy.add(new PositionComponent(position.x, position.y));
        enemy.add(new VelocityComponent(speed));

        // NEW: Try to use sprite, fallback to colored square
        TextureManager tm = TextureManager.getInstance();
        if (tm.hasTexture(textureKey)) {
            Texture texture = tm.getTexture(textureKey);
            enemy.add(new VisualComponent(size * 2f, size * 2f, texture, color));
        } else {
            enemy.add(new VisualComponent(size, size, color));
        }

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

// All your enemy classes stay the same, they'll automatically get sprites!

class ZombieEnemy extends EnemyType {
    public ZombieEnemy() {
        super(
            "Zombie",
            50f,
            60f,
            28f,
            5f,
            new Color(0.4f, 0.8f, 0.3f, 1f)
        );
    }
}

class ImpEnemy extends EnemyType {
    public ImpEnemy() {
        super(
            "Imp",
            15f,
            140f,
            18f,
            10f,
            new Color(0.8f, 0.3f, 2f, 1f)
        );
    }
}

class TankEnemy extends EnemyType {
    public TankEnemy() {
        super(
            "Tank",
            120f,
            40f,
            40f,
            30f,
            new Color(0.5f, 0.5f, 0.5f, 1f)
        );
    }
}

class RunnerEnemy extends EnemyType {
    public RunnerEnemy() {
        super(
            "Runner",
            30f,
            100f,
            24f,
            20f,
            new Color(1f, 0.7f, 0.2f, 1f)
        );
    }
}

class SwarmEnemy extends EnemyType {
    public SwarmEnemy() {
        super(
            "Swarm",
            8f,
            110f,
            14f,
            5f,
            new Color(1f, 1f, 0.3f, 1f)
        );
    }
}

class BruteEnemy extends EnemyType {
    public BruteEnemy() {
        super(
            "Brute",
            80f,
            70f,
            36f,
            18f,
            new Color(0.8f, 0.2f, 0.2f, 1f)
        );
    }
}

class GhostEnemy extends EnemyType {
    public GhostEnemy() {
        super(
            "Ghost",
            25f,
            120f,
            22f,
            15f,
            new Color(0.6f, 0.6f, 1f, 0.7f)
        );
    }
}

class EliteEnemy extends EnemyType {
    public EliteEnemy() {
        super(
            "Elite",
            150f,
            80f,
            44f,
            50f,
            new Color(1f, 0.2f, 1f, 1f)
        );
    }
}

class SlimeEnemy extends EnemyType {
    public SlimeEnemy() {
        super(
            "Slime",
            40f,  // Health (Average)
            55f,  // Speed (Slower than zombies)
            24f,  // Size
            8f,   // XP Drop
            new Color(0.2f, 0.9f, 0.6f, 1f) // Bright Slime Green
        );
    }
}
