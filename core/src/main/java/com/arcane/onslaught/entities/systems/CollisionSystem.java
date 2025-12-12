package com.arcane.onslaught.entities.systems;

import com.arcane.onslaught.upgrades.*;
import com.badlogic.ashley.core.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.events.*;
import com.badlogic.gdx.graphics.Texture;
import com.arcane.onslaught.utils.*;

public class CollisionSystem extends EntitySystem {
    private ComponentMapper<PositionComponent> posMapper;
    private ComponentMapper<VisualComponent> visMapper;
    private ComponentMapper<HealthComponent> healthMapper;
    private ComponentMapper<ProjectileComponent> projMapper;
    private ComponentMapper<PlayerComponent> playerMapper;
    private ComponentMapper<EnemyComponent> enemyMapper;
    private ComponentMapper<XPOrbComponent> xpMapper;
    private ComponentMapper<VelocityComponent> velMapper;
    private ComponentMapper<ExplosiveComponent> explosiveMapper;
    private ComponentMapper<ChainComponent> chainMapper;
    private ComponentMapper<PierceComponent> pierceMapper;
    private ComponentMapper<SlowComponent> slowMapper;
    private ComponentMapper<PoisonComponent> poisonMapper;
    private ComponentMapper<HealthOrbComponent> healthOrbMapper;
    private ComponentMapper<CriticalComponent> critMapper;

    private Entity player;
    private PlayerBuild playerBuild;
    private java.util.Random random;
    private Array<Entity> entitiesToRemove;

    public CollisionSystem(PlayerBuild playerBuild) {
        this.playerBuild = playerBuild;
        this.entitiesToRemove = new Array<>();

        posMapper = ComponentMapper.getFor(PositionComponent.class);
        visMapper = ComponentMapper.getFor(VisualComponent.class);
        healthMapper = ComponentMapper.getFor(HealthComponent.class);
        projMapper = ComponentMapper.getFor(ProjectileComponent.class);
        playerMapper = ComponentMapper.getFor(PlayerComponent.class);
        enemyMapper = ComponentMapper.getFor(EnemyComponent.class);
        xpMapper = ComponentMapper.getFor(XPOrbComponent.class);
        velMapper = ComponentMapper.getFor(VelocityComponent.class);
        explosiveMapper = ComponentMapper.getFor(ExplosiveComponent.class);
        chainMapper = ComponentMapper.getFor(ChainComponent.class);
        pierceMapper = ComponentMapper.getFor(PierceComponent.class);
        slowMapper = ComponentMapper.getFor(SlowComponent.class);
        poisonMapper = ComponentMapper.getFor(PoisonComponent.class);
        healthOrbMapper = ComponentMapper.getFor(HealthOrbComponent.class);
        critMapper = ComponentMapper.getFor(CriticalComponent.class);
        random = new java.util.Random();
    }

    @Override
    public void update(float deltaTime) {
        entitiesToRemove.clear();

        findPlayer();
        if (player == null) return;

        HealthComponent playerHealth = healthMapper.get(player);
        if (playerHealth != null && !playerHealth.isAlive()) {
            return;
        }

        checkProjectileEnemyCollisions();
        checkPlayerEnemyCollisions();
        checkPlayerXPCollisions();
        checkPlayerHealthCollisions();
        removeDeadEnemies();

        for (Entity entity : entitiesToRemove) {
            getEngine().removeEntity(entity);
        }
    }

    private void queueRemoval(Entity entity) {
        if (!entitiesToRemove.contains(entity, true)) {
            entitiesToRemove.add(entity);
        }
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
            if (entitiesToRemove.contains(proj, true)) continue;

            PositionComponent projPos = posMapper.get(proj);
            VisualComponent projVis = visMapper.get(proj);
            ProjectileComponent projComp = projMapper.get(proj);

            float projRadius = Math.max(projVis.width, projVis.height) / 2f;

            for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
                if (entitiesToRemove.contains(enemy, true)) continue;

                HealthComponent enemyHealth = healthMapper.get(enemy);
                if (!enemyHealth.isAlive()) continue;

                PositionComponent enemyPos = posMapper.get(enemy);
                VisualComponent enemyVis = visMapper.get(enemy);
                float enemyRadius = Math.max(enemyVis.width, enemyVis.height) / 2f;

                if (projPos.position.dst(enemyPos.position) < projRadius + enemyRadius) {
                    handleHit(proj, enemy, projComp, projPos);

                    PierceComponent pierce = pierceMapper.get(proj);
                    if (pierce != null && pierce.remainingPierces > 0) {
                        pierce.remainingPierces--;
                    } else {
                        queueRemoval(proj);
                        break;
                    }
                }
            }
        }
    }

    private void handleHit(Entity proj, Entity enemy, ProjectileComponent projComp, PositionComponent projPos) {
        HealthComponent enemyHealth = healthMapper.get(enemy);
        PositionComponent enemyPos = posMapper.get(enemy);

        boolean isCritical = false;
        float finalDamage = projComp.damage;

        CriticalComponent crit = critMapper.get(proj);
        if (crit != null && random.nextFloat() < crit.critChance) {
            isCritical = true;
            finalDamage *= crit.critMultiplier;
        }

        enemyHealth.damage(finalDamage);

        // Spawn visual indicator for the direct hit
        spawnDamageIndicator(enemyPos.position, finalDamage, isCritical);

        applySpellEffects(proj, enemy);

        ExplosiveComponent explosive = explosiveMapper.get(proj);
        if (explosive != null) {
            // Pass the hit location (usually enemy pos or projectile pos)
            explodeAt(enemyPos.position, explosive);
        }

        ChainComponent chain = chainMapper.get(proj);
        if (chain != null) {
            chain.hitEntities.add(enemy);
            chainToNearbyEnemy(proj, enemy);
        }
    }

    private void explodeAt(Vector2 position, ExplosiveComponent explosive) {
        // 1. SPAWN VISUAL
        Entity explosionVisual = new Entity();
        explosionVisual.add(new PositionComponent(position.x, position.y));

        float size = explosive.explosionRadius * 2.5f; // Visual needs to be slightly larger than radius to look right

        TextureManager tm = TextureManager.getInstance();
        if (tm.hasTexture("fireball")) {
            // Reuse fireball texture, tinted orange/red
            explosionVisual.add(new VisualComponent(size, size, tm.getTexture("fireball"), new Color(1f, 0.4f, 0.1f, 0.6f)));
        } else {
            explosionVisual.add(new VisualComponent(size, size, Color.ORANGE));
        }

        // Add a fader so it disappears
        DamageIndicatorComponent fade = new DamageIndicatorComponent(0, false);
        fade.lifetime = 0.25f; // Duration of explosion visual
        fade.color = new Color(1f, 0.5f, 0f, 0.8f);
        explosionVisual.add(fade);

        getEngine().addEntity(explosionVisual);

        // 2. DO DAMAGE TO NEARBY ENEMIES
        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();
        for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
            // Don't damage already dead/queued enemies
            if (entitiesToRemove.contains(enemy, true)) continue;

            PositionComponent enemyPos = posMapper.get(enemy);
            HealthComponent enemyHealth = healthMapper.get(enemy);
            if (!enemyHealth.isAlive()) continue;

            float distance = position.dst(enemyPos.position);

            if (distance <= explosive.explosionRadius) {
                // Modified Falloff: Minimum 30% damage at max range
                float falloff = 1f - (distance / explosive.explosionRadius); // 1.0 at center, 0.0 at edge
                float damageMultiplier = 0.3f + (0.7f * falloff);

                float finalDamage = explosive.explosionDamage * damageMultiplier;
                enemyHealth.damage(finalDamage);

                spawnDamageIndicator(enemyPos.position, finalDamage, false);
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
            if (entitiesToRemove.contains(enemy, true)) continue;

            HealthComponent enemyHealth = healthMapper.get(enemy);
            if (!enemyHealth.isAlive()) continue;

            PositionComponent enemyPos = posMapper.get(enemy);
            VisualComponent enemyVis = visMapper.get(enemy);
            float enemyRadius = Math.max(enemyVis.width, enemyVis.height) / 2f;

            if (playerPos.position.dst(enemyPos.position) < playerRadius + enemyRadius) {
                playerHealth.damage(1f, player);
            }
        }
    }

    private void checkPlayerXPCollisions() {
        PositionComponent playerPos = posMapper.get(player);
        PlayerComponent playerComp = playerMapper.get(player);
        HealthComponent playerHealth = healthMapper.get(player);
        VelocityComponent playerVel = velMapper.get(player);

        float pickupRange = Constants.XP_ORB_COLLECTION_RANGE * UpgradeHelper.getPickupRangeMultiplier(playerBuild);
        float xpMultiplier = UpgradeHelper.getXPMultiplier(playerBuild);

        Family xpFamily = Family.all(XPOrbComponent.class, PositionComponent.class).get();

        for (Entity orb : getEngine().getEntitiesFor(xpFamily)) {
            if (entitiesToRemove.contains(orb, true)) continue;

            PositionComponent orbPos = posMapper.get(orb);
            XPOrbComponent orbComp = xpMapper.get(orb);

            if (playerPos.position.dst(orbPos.position) < pickupRange) {
                playerComp.xp += orbComp.xpValue * xpMultiplier;
                if (playerComp.xp >= playerComp.xpToNextLevel) {
                    levelUp(player, playerComp, playerHealth, playerVel);
                }
                queueRemoval(orb);
            }
        }
    }

    private void checkPlayerHealthCollisions() {
        PositionComponent playerPos = posMapper.get(player);
        HealthComponent playerHealth = healthMapper.get(player);
        Family healthFamily = Family.all(HealthOrbComponent.class, PositionComponent.class).get();

        for (Entity orb : getEngine().getEntitiesFor(healthFamily)) {
            if (entitiesToRemove.contains(orb, true)) continue;

            PositionComponent orbPos = posMapper.get(orb);
            HealthOrbComponent orbComp = healthOrbMapper.get(orb);
            if (playerPos.position.dst(orbPos.position) < Constants.XP_ORB_COLLECTION_RANGE) {
                float healAmount = Math.min(orbComp.healAmount, playerHealth.maxHealth - playerHealth.currentHealth);
                playerHealth.currentHealth += healAmount;
                queueRemoval(orb);
            }
        }
    }

    private void levelUp(Entity player, PlayerComponent pc, HealthComponent health, VelocityComponent vel) {
        pc.level++;
        pc.xp -= pc.xpToNextLevel;
        pc.xpToNextLevel = (float)(Constants.XP_TO_LEVEL_BASE * Math.pow(pc.level, 1.5));
        health.maxHealth *= 1.05f;

        if (vel.maxSpeed < Constants.PLAYER_SPEED * 2f) vel.maxSpeed *= 1.02f;

        TimerComponent timer = player.getComponent(TimerComponent.class);
        if (timer != null && timer.interval > 0.3f) timer.interval *= 0.97f;

        EventManager.getInstance().publish(new LevelUpEvent(pc.level));
    }

    private void removeDeadEnemies() {
        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();

        for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
            if (entitiesToRemove.contains(enemy, true)) continue;

            HealthComponent health = healthMapper.get(enemy);
            if (!health.isAlive()) {
                PositionComponent pos = posMapper.get(enemy);
                EnemyComponent enemyComp = enemyMapper.get(enemy);

                spawnXPOrb(pos.position, enemyComp.xpDropped);

                if (UpgradeHelper.hasLifeSteal(playerBuild) && player != null) {
                    HealthComponent playerHealth = healthMapper.get(player);
                    if (playerHealth != null) {
                        playerHealth.currentHealth = Math.min(playerHealth.currentHealth + 1f, playerHealth.maxHealth);
                    }
                }

                if (random.nextFloat() < 0.05f * UpgradeHelper.getHealthDropMultiplier(playerBuild)) {
                    spawnHealthOrb(pos.position, 10f);
                }

                if (UpgradeHelper.shouldExplodeOnDeath(playerBuild)) {
                    ExplosiveComponent deathExplosion = new ExplosiveComponent(50f, 40f);
                    explodeAt(pos.position, deathExplosion);
                }

                queueRemoval(enemy);
            }
        }
    }

    private void spawnXPOrb(Vector2 position, float xpValue) {
        Entity orb = new Entity();
        orb.add(new PositionComponent(position.x, position.y));
        TextureManager tm = TextureManager.getInstance();
        if (tm.hasTexture("xp_orb")) {
            orb.add(new VisualComponent(Constants.XP_ORB_SIZE * 3f, Constants.XP_ORB_SIZE * 3f, tm.getTexture("xp_orb")));
        } else {
            orb.add(new VisualComponent(Constants.XP_ORB_SIZE, Constants.XP_ORB_SIZE, Color.GREEN));
        }
        orb.add(new XPOrbComponent(xpValue));
        getEngine().addEntity(orb);
    }

    private void spawnHealthOrb(Vector2 position, float healAmount) {
        Entity orb = new Entity();
        orb.add(new PositionComponent(position.x, position.y));
        TextureManager tm = TextureManager.getInstance();
        if (tm.hasTexture("health_orb")) {
            orb.add(new VisualComponent(Constants.XP_ORB_SIZE * 3f, Constants.XP_ORB_SIZE * 3f, tm.getTexture("health_orb")));
        } else {
            orb.add(new VisualComponent(Constants.XP_ORB_SIZE, Constants.XP_ORB_SIZE, Color.RED));
        }
        orb.add(new HealthOrbComponent(healAmount));
        getEngine().addEntity(orb);
    }

    private void applySpellEffects(Entity projectile, Entity enemy) {
        SlowComponent slow = slowMapper.get(projectile);
        if (slow != null) {
            VelocityComponent enemyVel = velMapper.get(enemy);
            if (enemyVel != null && enemy.getComponent(SlowedComponent.class) == null) {
                enemy.add(new SlowedComponent(slow.slowAmount, slow.slowDuration, enemyVel.maxSpeed));
            }
        }

        PoisonComponent poison = poisonMapper.get(projectile);
        if (poison != null) {
            PoisonedComponent poisoned = enemy.getComponent(PoisonedComponent.class);
            if (poisoned == null) {
                enemy.add(new PoisonedComponent(poison.damagePerSecond, poison.duration));
            } else {
                poisoned.timeRemaining = Math.max(poisoned.timeRemaining, poison.duration);
            }
        }
    }

    private void chainToNearbyEnemy(Entity sourceProjectile, Entity sourceEnemy) {
        ChainComponent chain = chainMapper.get(sourceProjectile);
        if (chain == null || chain.remainingChains <= 0) return;

        PositionComponent sourcePos = posMapper.get(sourceEnemy);
        Entity nearestEnemy = null;
        float minDist = Float.MAX_VALUE;

        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();
        for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
            if (enemy == sourceEnemy) continue;
            if (chain.hitEntities.contains(enemy)) continue;
            if (entitiesToRemove.contains(enemy, true)) continue;

            HealthComponent health = healthMapper.get(enemy);
            if (!health.isAlive()) continue;

            PositionComponent pos = posMapper.get(enemy);
            float distance = sourcePos.position.dst(pos.position);
            if (distance <= chain.chainRange && distance < minDist) {
                minDist = distance;
                nearestEnemy = enemy;
            }
        }

        if (nearestEnemy != null) {
            HealthComponent health = healthMapper.get(nearestEnemy);
            health.damage(chain.chainDamage);
            chain.hitEntities.add(nearestEnemy);
            chain.remainingChains--;
            PositionComponent targetPos = posMapper.get(nearestEnemy);
            spawnDamageIndicator(targetPos.position, chain.chainDamage, false);
        }
    }

    private void spawnDamageIndicator(Vector2 position, float damage, boolean isCritical) {
        if (Float.isNaN(position.x) || Float.isNaN(position.y) ||
            Float.isInfinite(position.x) || Float.isInfinite(position.y)) {
            System.err.println("WARNING: Attempted to spawn damage indicator at invalid position: " + position);
            return;
        }

        Entity indicator = new Entity();
        indicator.add(new PositionComponent(position.x, position.y + 20f));
        indicator.add(new DamageIndicatorComponent(damage, isCritical));
        VelocityComponent vel = new VelocityComponent(0);
        vel.velocity.set(0, 50f);
        indicator.add(vel);
        getEngine().addEntity(indicator);
    }
}
