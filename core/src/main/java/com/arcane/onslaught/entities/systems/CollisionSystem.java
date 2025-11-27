package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.events.*;
import com.arcane.onslaught.utils.Constants;

/**
 * Handles collision detection and XP/leveling system
 */
public class CollisionSystem extends EntitySystem {
    private ComponentMapper<PositionComponent> posMapper;
    private ComponentMapper<VisualComponent> visMapper;
    private ComponentMapper<HealthComponent> healthMapper;
    private ComponentMapper<ProjectileComponent> projMapper;
    private ComponentMapper<PlayerComponent> playerMapper;
    private ComponentMapper<EnemyComponent> enemyMapper;
    private ComponentMapper<XPOrbComponent> xpMapper;
    private ComponentMapper<VelocityComponent> velMapper;
    private ComponentMapper<TimerComponent> timerMapper;
    private ComponentMapper<ExplosiveComponent> explosiveMapper;
    private ComponentMapper<ChainComponent> chainMapper;
    private ComponentMapper<PierceComponent> pierceMapper;
    private ComponentMapper<SlowComponent> slowMapper;
    private ComponentMapper<PoisonComponent> poisonMapper;

    private Entity player;

    public CollisionSystem() {
        posMapper = ComponentMapper.getFor(PositionComponent.class);
        visMapper = ComponentMapper.getFor(VisualComponent.class);
        healthMapper = ComponentMapper.getFor(HealthComponent.class);
        projMapper = ComponentMapper.getFor(ProjectileComponent.class);
        playerMapper = ComponentMapper.getFor(PlayerComponent.class);
        enemyMapper = ComponentMapper.getFor(EnemyComponent.class);
        xpMapper = ComponentMapper.getFor(XPOrbComponent.class);
        velMapper = ComponentMapper.getFor(VelocityComponent.class);
        timerMapper = ComponentMapper.getFor(TimerComponent.class);
        explosiveMapper = ComponentMapper.getFor(ExplosiveComponent.class);
        chainMapper = ComponentMapper.getFor(ChainComponent.class);
        pierceMapper = ComponentMapper.getFor(PierceComponent.class);
        slowMapper = ComponentMapper.getFor(SlowComponent.class);
        poisonMapper = ComponentMapper.getFor(PoisonComponent.class);
    }

    @Override
    public void update(float deltaTime) {
        findPlayer();
        if (player == null) return;

        // Don't process collisions if player is dead
        HealthComponent playerHealth = healthMapper.get(player);
        if (playerHealth != null && !playerHealth.isAlive()) {
            return; // Stop all collision processing when dead
        }

        checkProjectileEnemyCollisions();
        checkPlayerEnemyCollisions();
        checkPlayerXPCollisions();
        removeDeadEnemies();
    }

    private void findPlayer() {
        player = null;
        Family playerFamily = Family.all(PlayerComponent.class, PositionComponent.class).get();
        for (Entity e : getEngine().getEntitiesFor(playerFamily)) {
            player = e;
            break;
        }
    }

    private void checkProjectileEnemyCollisions() {
        Family projFamily = Family.all(ProjectileComponent.class, PositionComponent.class).get();
        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();

        for (Entity proj : getEngine().getEntitiesFor(projFamily)) {
            PositionComponent projPos = posMapper.get(proj);
            VisualComponent projVis = visMapper.get(proj);
            ProjectileComponent projComp = projMapper.get(proj);

            float projRadius = Math.max(projVis.width, projVis.height) / 2f;

            for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
                HealthComponent enemyHealth = healthMapper.get(enemy);
                if (!enemyHealth.isAlive()) continue;

                PositionComponent enemyPos = posMapper.get(enemy);
                VisualComponent enemyVis = visMapper.get(enemy);
                float enemyRadius = Math.max(enemyVis.width, enemyVis.height) / 2f;

                float distance = projPos.position.dst(enemyPos.position);

                if (distance < projRadius + enemyRadius) {
                    enemyHealth.damage(projComp.damage);
                    getEngine().removeEntity(proj);
                    break;
                }
            }
        }
    }

    private void checkPlayerEnemyCollisions() {
        PositionComponent playerPos = posMapper.get(player);
        VisualComponent playerVis = visMapper.get(player);
        HealthComponent playerHealth = healthMapper.get(player);

        float playerRadius = Math.max(playerVis.width, playerVis.height) / 2f;

        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();

        for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
            HealthComponent enemyHealth = healthMapper.get(enemy);
            if (!enemyHealth.isAlive()) continue;

            PositionComponent enemyPos = posMapper.get(enemy);
            VisualComponent enemyVis = visMapper.get(enemy);
            float enemyRadius = Math.max(enemyVis.width, enemyVis.height) / 2f;

            float distance = playerPos.position.dst(enemyPos.position);

            if (distance < playerRadius + enemyRadius) {
                playerHealth.damage(1f); // 1 damage per frame
            }
        }
    }

    private void checkPlayerXPCollisions() {
        PositionComponent playerPos = posMapper.get(player);
        PlayerComponent playerComp = playerMapper.get(player);
        HealthComponent playerHealth = healthMapper.get(player);
        VelocityComponent playerVel = velMapper.get(player);

        Family xpFamily = Family.all(XPOrbComponent.class, PositionComponent.class).get();

        for (Entity orb : getEngine().getEntitiesFor(xpFamily)) {
            PositionComponent orbPos = posMapper.get(orb);
            XPOrbComponent orbComp = xpMapper.get(orb);

            float distance = playerPos.position.dst(orbPos.position);

            if (distance < Constants.XP_ORB_COLLECTION_RANGE) {
                // Collect XP
                playerComp.xp += orbComp.xpValue;

                // Check for level up
                if (playerComp.xp >= playerComp.xpToNextLevel) {
                    levelUp(player, playerComp, playerHealth, playerVel);
                }

                getEngine().removeEntity(orb);
            }
        }
    }

    /**
     * Handle player leveling up with stat increases and better XP curve
     */
    private void levelUp(Entity player, PlayerComponent pc, HealthComponent health, VelocityComponent vel) {
        pc.level++;
        pc.xp -= pc.xpToNextLevel;

        // EXPONENTIAL XP CURVE
        // Formula: XP = base * (level ^ 1.5)
        // Level 1->2: 100 XP
        // Level 2->3: 141 XP
        // Level 3->4: 173 XP
        // Level 4->5: 200 XP
        // Level 10->11: 316 XP
        // Level 20->21: 632 XP
        pc.xpToNextLevel = (float)(Constants.XP_TO_LEVEL_BASE * Math.pow(pc.level, 1.5));

        // STAT INCREASES PER LEVEL
        // Max Health: +5% per level
        health.maxHealth *= 1.05f;
        health.currentHealth = health.maxHealth; // Fully heal on level up

        // Movement Speed: +2% per level (caps at reasonable speed)
        if (vel.maxSpeed < Constants.PLAYER_SPEED * 2f) { // Cap at 2x base speed
            vel.maxSpeed *= 1.02f;
        }

        // Attack Speed: -3% cooldown per level (faster casting)
        TimerComponent timer = player.getComponent(TimerComponent.class);
        if (timer != null && timer.interval > 0.3f) { // Cap at 0.3s minimum cooldown
            timer.interval *= 0.97f; // 3% faster each level
        }

        // Publish level up event
        EventManager.getInstance().publish(new LevelUpEvent(pc.level));

        System.out.println("═══════════════════════════════════");
        System.out.println("★★★ LEVEL UP! ★★★");
        System.out.println("Level: " + pc.level);
        System.out.println("Max HP: " + (int)health.maxHealth + " (FULLY HEALED!)");
        System.out.println("Speed: " + (int)vel.maxSpeed);
        System.out.println("Attack Speed: " + String.format("%.2f", timer != null ? timer.interval : 0) + "s cooldown");
        System.out.println("Next Level: " + (int)pc.xpToNextLevel + " XP");
        System.out.println("═══════════════════════════════════");
    }

    private void removeDeadEnemies() {
        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();

        for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
            HealthComponent health = healthMapper.get(enemy);

            if (!health.isAlive()) {
                PositionComponent pos = posMapper.get(enemy);
                EnemyComponent enemyComp = enemyMapper.get(enemy);

                spawnXPOrb(pos.position, enemyComp.xpDropped);
                getEngine().removeEntity(enemy);
            }
        }
    }

    private void spawnXPOrb(Vector2 position, float xpValue) {
        Entity orb = new Entity();
        orb.add(new PositionComponent(position.x, position.y));
        orb.add(new VisualComponent(Constants.XP_ORB_SIZE, Constants.XP_ORB_SIZE, Color.GREEN));
        orb.add(new XPOrbComponent(xpValue));
        getEngine().addEntity(orb);
    }

    /**
     * Apply spell effects like slow, poison, etc.
     */
    private void applySpellEffects(Entity projectile, Entity enemy) {
        // Apply slow
        SlowComponent slow = slowMapper.get(projectile);
        if (slow != null) {
            VelocityComponent enemyVel = velMapper.get(enemy);
            if (enemyVel != null && enemy.getComponent(SlowedComponent.class) == null) {
                enemy.add(new SlowedComponent(slow.slowAmount, slow.slowDuration, enemyVel.maxSpeed));
            }
        }

        // Apply poison
        PoisonComponent poison = poisonMapper.get(projectile);
        if (poison != null) {
            // Add or refresh poison
            PoisonedComponent poisoned = enemy.getComponent(PoisonedComponent.class);
            if (poisoned == null) {
                enemy.add(new PoisonedComponent(poison.damagePerSecond, poison.duration));
            } else {
                // Refresh duration
                poisoned.timeRemaining = Math.max(poisoned.timeRemaining, poison.duration);
            }
        }
    }

    /**
     * Create explosion that damages nearby enemies
     */
    private void explodeAt(Vector2 position, ExplosiveComponent explosive) {
        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();

        for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
            PositionComponent enemyPos = posMapper.get(enemy);
            HealthComponent enemyHealth = healthMapper.get(enemy);

            if (!enemyHealth.isAlive()) continue;

            float distance = position.dst(enemyPos.position);

            if (distance <= explosive.explosionRadius) {
                // Damage falls off with distance
                float damageFalloff = 1f - (distance / explosive.explosionRadius);
                float finalDamage = explosive.explosionDamage * damageFalloff;
                enemyHealth.damage(finalDamage);
            }
        }
    }

    /**
     * Chain lightning to nearby enemy
     */
    private void chainToNearbyEnemy(Entity sourceProjectile, Entity sourceEnemy) {
        ChainComponent chain = chainMapper.get(sourceProjectile);
        if (chain == null || chain.remainingChains <= 0) return;

        PositionComponent sourcePos = posMapper.get(sourceEnemy);
        Entity nearestEnemy = null;
        float minDist = Float.MAX_VALUE;

        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();

        for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
            if (enemy == sourceEnemy) continue;
            if (chain.hitEntities.contains(enemy)) continue; // Don't chain to same enemy

            HealthComponent health = healthMapper.get(enemy);
            if (!health.isAlive()) continue;

            PositionComponent pos = posMapper.get(enemy);
            float distance = sourcePos.position.dst(pos.position);

            if (distance <= chain.chainRange && distance < minDist) {
                minDist = distance;
                nearestEnemy = enemy;
            }
        }

        // Deal damage to chained enemy
        if (nearestEnemy != null) {
            HealthComponent health = healthMapper.get(nearestEnemy);
            health.damage(chain.chainDamage);
            chain.hitEntities.add(nearestEnemy);
        }
    }
}
