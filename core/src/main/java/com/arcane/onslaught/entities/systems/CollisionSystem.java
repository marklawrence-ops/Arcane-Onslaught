package com.arcane.onslaught.entities.systems;

import com.arcane.onslaught.upgrades.*;
import com.badlogic.ashley.core.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.events.*;
import com.arcane.onslaught.utils.*;
import java.util.ArrayList;
import java.util.List;

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
    private ComponentMapper<CollisionComponent> colMapper;
    private ComponentMapper<SlowedComponent> slowedMapper;

    private Entity player;
    private PlayerBuild playerBuild;
    private java.util.Random random;

    private Array<Entity> entitiesToRemove;
    private List<ExplosionRequest> explosionQueue;

    private static class ExplosionRequest {
        Vector2 position;
        float radius;
        float damage;
        public ExplosionRequest(Vector2 pos, float r, float d) {
            this.position = new Vector2(pos);
            this.radius = r;
            this.damage = d;
        }
    }

    public CollisionSystem(PlayerBuild playerBuild) {
        this.playerBuild = playerBuild;
        this.entitiesToRemove = new Array<>();
        this.explosionQueue = new ArrayList<>();

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
        colMapper = ComponentMapper.getFor(CollisionComponent.class);
        slowedMapper = ComponentMapper.getFor(SlowedComponent.class);

        random = new java.util.Random();
    }

    private float getEntityRadius(Entity entity) {
        CollisionComponent col = colMapper.get(entity);
        if (col != null) return col.radius;
        VisualComponent vis = visMapper.get(entity);
        if (vis != null) return Math.min(vis.width, vis.height) / 2.5f;
        return 10f;
    }

    @Override
    public void update(float deltaTime) {
        entitiesToRemove.clear();
        explosionQueue.clear();

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

        processExplosions();

        for (Entity entity : entitiesToRemove) {
            getEngine().removeEntity(entity);
        }
    }

    private void queueRemoval(Entity entity) {
        if (!entitiesToRemove.contains(entity, true)) entitiesToRemove.add(entity);
    }

    private void findPlayer() {
        player = null;
        Family playerFamily = Family.all(PlayerComponent.class, PositionComponent.class).get();
        for (Entity e : getEngine().getEntitiesFor(playerFamily)) {
            player = e;
            break;
        }
    }

    private void queueExplosion(Vector2 position, ExplosiveComponent explosive) {
        explosionQueue.add(new ExplosionRequest(position, explosive.explosionRadius, explosive.explosionDamage));
    }

    private void processExplosions() {
        if (explosionQueue.isEmpty()) return;

        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();

        for (ExplosionRequest req : explosionQueue) {
            spawnExplosionVisual(req.position, req.radius);

            for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
                if (entitiesToRemove.contains(enemy, true)) continue;

                PositionComponent enemyPos = posMapper.get(enemy);
                HealthComponent enemyHealth = healthMapper.get(enemy);
                if (!enemyHealth.isAlive()) continue;

                float enemyRadius = getEntityRadius(enemy);
                float distance = req.position.dst(enemyPos.position) - enemyRadius;

                if (distance <= req.radius) {
                    float falloff = 1f - (Math.max(0, distance) / req.radius);
                    float damageMultiplier = 0.3f + (0.7f * falloff);
                    float finalDamage = req.damage * damageMultiplier;

                    // --- FIXED: SYNERGY VFX (SHATTER) ---
                    if (playerBuild.hasTag("shatter")) {
                        SlowedComponent slowed = slowedMapper.get(enemy);
                        // Check slowAmount (Corrected Field)
                        if (slowed != null && slowed.slowAmount > 0.5f) {
                            spawnVisualEffect(enemyPos.position, "vfx_ice_shatter", 60f, 0.4f);
                            finalDamage *= 2.0f;
                        }
                    }
                    // ------------------------------------

                    enemyHealth.damage(finalDamage);
                    spawnDamageIndicator(enemyPos.position, finalDamage, false);
                }
            }
        }
    }

    private void spawnExplosionVisual(Vector2 position, float radius) {
        Entity explosionVisual = new Entity();
        explosionVisual.add(new PositionComponent(position.x, position.y));
        float size = radius * 2.5f;

        TextureManager tm = TextureManager.getInstance();
        if (tm.hasTexture("fireball")) {
            explosionVisual.add(new VisualComponent(size, size, tm.getTexture("fireball"), new Color(1f, 0.4f, 0.1f, 0.6f)));
        } else {
            explosionVisual.add(new VisualComponent(size, size, Color.ORANGE));
        }

        explosionVisual.add(new LifetimeComponent(0.25f));
        SoundManager.getInstance().play("explosion", 0.8f); // Lower pitch for boom
        getEngine().addEntity(explosionVisual);

        if (playerBuild.hasTag("toxic_cloud")) {
            spawnVisualEffect(position, "effect_poison", radius * 2.5f, 2.0f);
        }
    }

    private void spawnVisualEffect(Vector2 position, String textureName, float size, float duration) {
        Entity effect = new Entity();
        effect.add(new PositionComponent(position.x, position.y));

        TextureManager tm = TextureManager.getInstance();
        if (tm.hasTexture(textureName)) {
            effect.add(new VisualComponent(size, size, tm.getTexture(textureName)));
        } else {
            effect.add(new VisualComponent(size, size, Color.WHITE));
        }

        effect.add(new LifetimeComponent(duration));

        if (textureName.equals("effect_heal") || textureName.equals("vfx_steam")) {
            VelocityComponent vel = new VelocityComponent(0);
            vel.velocity.set(0, 50f);
            effect.add(vel);
        }

        getEngine().addEntity(effect);
    }

    private void explodeAt(Vector2 position, ExplosiveComponent explosive) {
        queueExplosion(position, explosive);
    }

    private void checkProjectileEnemyCollisions() {
        Family projFamily = Family.all(ProjectileComponent.class, PositionComponent.class).get();
        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();
        for (Entity proj : getEngine().getEntitiesFor(projFamily)) {
            if (entitiesToRemove.contains(proj, true)) continue;
            PositionComponent projPos = posMapper.get(proj);
            ProjectileComponent projComp = projMapper.get(proj);
            float projRadius = getEntityRadius(proj);
            for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
                if (entitiesToRemove.contains(enemy, true)) continue;
                HealthComponent enemyHealth = healthMapper.get(enemy);
                if (!enemyHealth.isAlive()) continue;
                PositionComponent enemyPos = posMapper.get(enemy);
                float enemyRadius = getEntityRadius(enemy);
                if (projPos.position.dst(enemyPos.position) < projRadius + enemyRadius) {
                    handleHit(proj, enemy, projComp, projPos);
                    PierceComponent pierce = pierceMapper.get(proj);
                    if (pierce != null && pierce.remainingPierces > 0) { pierce.remainingPierces--; }
                    else { queueRemoval(proj); break; }
                }
            }
        }
    }

    private void handleHit(Entity proj, Entity enemy, ProjectileComponent projComp, PositionComponent projPos) {
        HealthComponent enemyHealth = healthMapper.get(enemy);
        PositionComponent enemyPos = posMapper.get(enemy);

        // --- FIXED: SYNERGY CHECKS (Using spellType) ---
        ProjectileComponent pc = projMapper.get(proj);
        if (pc != null) {
            // Thermal Shock
            if ("fireball".equals(pc.spellType) && playerBuild.hasTag("thermal_shock")) {
                if (slowedMapper.get(enemy) != null) {
                    spawnVisualEffect(enemyPos.position, "vfx_steam", 50f, 0.6f);
                }
            }
            // Electrocution (Use "lightning" because SpellCastSystem usually sets it to this)
            // Check your LightningBoltSpell.java to see what string it passes. Usually "lightning" or "lightning_bolt"
            if (pc.spellType.contains("lightning") && playerBuild.hasTag("electrocution")) {
                spawnVisualEffect(enemyPos.position, "effect_poison", 40f, 0.4f);
            }
        }
        // -----------------------------------------------

        boolean isCritical = false;
        float finalDamage = projComp.damage;
        CriticalComponent crit = critMapper.get(proj);
        if (crit != null && random.nextFloat() < crit.critChance) {
            isCritical = true;
            finalDamage *= crit.critMultiplier;
        }
        enemyHealth.damage(finalDamage);
        SoundManager.getInstance().play("hit", 1.2f); // Higher pitch for hits
        spawnDamageIndicator(enemyPos.position, finalDamage, isCritical);
        applySpellEffects(proj, enemy);
        ExplosiveComponent explosive = explosiveMapper.get(proj);
        if (explosive != null) { explodeAt(enemyPos.position, explosive); }
        ChainComponent chain = chainMapper.get(proj);
        if (chain != null) { chain.hitEntities.add(enemy); chainToNearbyEnemy(proj, enemy); }
    }

    // ... (Collision Checks for Player, XP, Health - UNCHANGED) ...
    private void checkPlayerEnemyCollisions() {
        PositionComponent playerPos = posMapper.get(player);
        HealthComponent playerHealth = healthMapper.get(player);
        float playerRadius = getEntityRadius(player);
        Family enemyFamily = Family.all(EnemyComponent.class, PositionComponent.class, HealthComponent.class).get();
        for (Entity enemy : getEngine().getEntitiesFor(enemyFamily)) {
            if (entitiesToRemove.contains(enemy, true)) continue;
            HealthComponent enemyHealth = healthMapper.get(enemy);
            if (!enemyHealth.isAlive()) continue;
            PositionComponent enemyPos = posMapper.get(enemy);
            float enemyRadius = getEntityRadius(enemy);
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
                SoundManager.getInstance().play("pickup", 1.0f);
                if (playerComp.xp >= playerComp.xpToNextLevel) levelUp(player, playerComp, playerHealth, playerVel);
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
                        spawnVisualEffect(posMapper.get(player).position, "effect_heal", 40f, 0.5f);
                    }
                }

                if (random.nextFloat() < 0.05f * UpgradeHelper.getHealthDropMultiplier(playerBuild)) spawnHealthOrb(pos.position, 10f);
                if (UpgradeHelper.shouldExplodeOnDeath(playerBuild)) {
                    ExplosiveComponent deathExplosion = new ExplosiveComponent(50f, 40f);
                    queueExplosion(pos.position, deathExplosion);
                }
                queueRemoval(enemy);
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

    private void spawnXPOrb(Vector2 position, float xpValue) {
        Entity orb = new Entity();
        orb.add(new PositionComponent(position.x, position.y));
        TextureManager tm = TextureManager.getInstance();
        if (tm.hasTexture("xp_orb")) orb.add(new VisualComponent(Constants.XP_ORB_SIZE * 3f, Constants.XP_ORB_SIZE * 3f, tm.getTexture("xp_orb")));
        else orb.add(new VisualComponent(Constants.XP_ORB_SIZE, Constants.XP_ORB_SIZE, Color.GREEN));
        orb.add(new XPOrbComponent(xpValue));
        getEngine().addEntity(orb);
    }

    private void spawnHealthOrb(Vector2 position, float healAmount) {
        Entity orb = new Entity();
        orb.add(new PositionComponent(position.x, position.y));
        TextureManager tm = TextureManager.getInstance();
        if (tm.hasTexture("health_orb")) orb.add(new VisualComponent(Constants.XP_ORB_SIZE * 3f, Constants.XP_ORB_SIZE * 3f, tm.getTexture("health_orb")));
        else orb.add(new VisualComponent(Constants.XP_ORB_SIZE, Constants.XP_ORB_SIZE, Color.RED));
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
            if (poisoned == null) enemy.add(new PoisonedComponent(poison.damagePerSecond, poison.duration));
            else poisoned.timeRemaining = Math.max(poisoned.timeRemaining, poison.duration);
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
        if (Float.isNaN(position.x) || Float.isNaN(position.y) || Float.isInfinite(position.x) || Float.isInfinite(position.y)) return;
        Entity indicator = new Entity();
        indicator.add(new PositionComponent(position.x, position.y + 20f));
        indicator.add(new DamageIndicatorComponent(damage, isCritical));
        VelocityComponent vel = new VelocityComponent(0);
        vel.velocity.set(0, 50f);
        indicator.add(vel);
        getEngine().addEntity(indicator);
    }
}
