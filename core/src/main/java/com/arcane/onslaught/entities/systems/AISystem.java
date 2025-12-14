package com.arcane.onslaught.entities.systems;

import com.arcane.onslaught.enemies.EnemyFactory;
import com.arcane.onslaught.entities.components.BossComponent.BossSkill;
import com.arcane.onslaught.utils.SoundManager;
import com.arcane.onslaught.utils.TextureManager;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;

public class AISystem extends IteratingSystem {
    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private ComponentMapper<AIComponent> am = ComponentMapper.getFor(AIComponent.class);
    private ComponentMapper<BossComponent> bm = ComponentMapper.getFor(BossComponent.class);
    private ComponentMapper<VisualComponent> vism = ComponentMapper.getFor(VisualComponent.class);
    private ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);

    private Entity playerEntity; // Store player reference
    private EnemyFactory enemyFactory;

    public AISystem(EnemyFactory factory) {
        super(Family.all(AIComponent.class, PositionComponent.class, VelocityComponent.class).get());
        this.enemyFactory = factory;
    }

    @Override
    public void update(float deltaTime) {
        // Cache player entity
        Family playerFamily = Family.all(PlayerComponent.class, PositionComponent.class).get();
        ImmutableArray<Entity> players = getEngine().getEntitiesFor(playerFamily);
        if (players.size() > 0) {
            playerEntity = players.get(0);
        } else {
            playerEntity = null;
        }

        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BossComponent boss = bm.get(entity);

        if (boss != null) {
            processBossBehavior(entity, boss, deltaTime);
        } else {
            // Standard Enemy Behavior
            if (playerEntity == null) return;
            PositionComponent pos = pm.get(entity);
            VelocityComponent vel = vm.get(entity);
            AIComponent ai = am.get(entity);
            PositionComponent playerPos = pm.get(playerEntity);

            if (ai.type == AIComponent.AIType.CHASE_PLAYER) {
                Vector2 direction = new Vector2(playerPos.position).sub(pos.position).nor();
                vel.velocity.set(direction).scl(vel.maxSpeed);
            }
        }
    }

    private void processBossBehavior(Entity entity, BossComponent boss, float deltaTime) {
        if (playerEntity == null) return;

        PositionComponent pos = pm.get(entity);
        VelocityComponent vel = vm.get(entity);
        VisualComponent vis = vism.get(entity);
        PositionComponent playerPos = pm.get(playerEntity);

        // --- ENRAGE (BERSERK) BUFF ---
        if (boss.isEnraged) {
            boss.enrageTimer -= deltaTime;
            if (boss.enrageTimer <= 0) {
                boss.isEnraged = false;
                vis.color = Color.WHITE; // Reset color
                vel.maxSpeed /= 2f; // Reset speed
            } else {
                // Ensure visual feedback remains during enrage
                if (boss.enrageTimer % 0.2f < 0.1f) vis.color = Color.RED;
                else vis.color = Color.ORANGE;
            }
        }

        // --- STATE 1: CASTING ---
        if (boss.isCasting) {
            boss.castTimer -= deltaTime;

            // Gravity Well Logic (Pull Player)
            if (boss.nextSkill == BossSkill.GRAVITY_WELL && playerEntity != null) {
                PositionComponent pPos = pm.get(playerEntity);
                Vector2 pullDir = new Vector2(pos.position).sub(pPos.position).nor();
                // Move player towards boss
                pPos.position.add(pullDir.scl(300f * deltaTime));
            }

            vel.velocity.setZero();
            if (boss.castTimer <= 0) {
                boss.isCasting = false;
                if (!boss.isEnraged) vis.color = Color.WHITE;
            }
            return;
        }

        // --- STATE 2: TELEGRAPHING ---
        if (boss.isTelegraphing) {
            boss.telegraphTimer -= deltaTime;
            vel.velocity.setZero();

            // Flash Visuals
            float flashSpeed = 15f;
            float alpha = (MathUtils.sin(boss.telegraphTimer * flashSpeed) + 1) / 2f;

            if (boss.nextSkill == BossSkill.SHOCKWAVE || boss.nextSkill == BossSkill.BERSERK) {
                vis.color = new Color(1f, alpha, 0f, 1f); // Orange Flash
            } else if (boss.nextSkill == BossSkill.GRAVITY_WELL) {
                vis.color = new Color(0.1f, 0.1f, 0.1f, alpha); // Dark Flash
            } else {
                vis.color = new Color(1f, 1f, alpha, 1f); // Standard Flash
            }

            if (boss.telegraphTimer <= 0) {
                boss.isTelegraphing = false;
                executeSkill(entity, boss, boss.nextSkill, pos.position);
                if (!boss.isEnraged) vis.color = Color.WHITE;
                boss.skillTimer = boss.skillCooldown;
            }
            return;
        }

        // --- STATE 3: COOLDOWN / CHASE ---
        if (boss.skillTimer > 0) boss.skillTimer -= deltaTime;

        Vector2 direction = new Vector2(playerPos.position).sub(pos.position).nor();
        vel.velocity.set(direction).scl(vel.maxSpeed);

        if (boss.skillTimer <= 0 && !boss.availableSkills.isEmpty()) {
            BossSkill skill = boss.availableSkills.get(MathUtils.random(boss.availableSkills.size() - 1));
            boss.nextSkill = skill;
            boss.isTelegraphing = true;

            switch (skill) {
                case SHOCKWAVE: boss.telegraphTimer = 1.0f; break; // Big warning
                case GRAVITY_WELL: boss.telegraphTimer = 0.8f; break;
                case BERSERK: boss.telegraphTimer = 0.5f; break; // Quick warn
                case SUMMON_MINIONS: boss.telegraphTimer = 1.5f; break;
                default: boss.telegraphTimer = 0.6f; break;
            }
        }
    }

    private void executeSkill(Entity bossEntity, BossComponent boss, BossSkill skill, Vector2 bossPos) {
        System.out.println("Boss Casting: " + skill);
        PositionComponent playerPosComp = pm.get(playerEntity);
        Vector2 playerPosVec = playerPosComp.position;

        switch (skill) {
            case SHOCKWAVE:
                // Damage + Knockback if close
                float dist = bossPos.dst(playerPosVec);
                if (dist < 300f) { // 300 pixel range
                    HealthComponent ph = hm.get(playerEntity);
                    ph.damage(30f); // Big damage
                    SoundManager.getInstance().play("explosion", 1.0f);

                    // Knockback
                    Vector2 knockback = new Vector2(playerPosVec).sub(bossPos).nor().scl(200f);
                    playerPosVec.add(knockback);
                }
                boss.isCasting = true;
                boss.castTimer = 0.5f;
                break;

            case GRAVITY_WELL:
                // Effect happens during 'isCasting' state update loop
                SoundManager.getInstance().play("teleport", 0.8f);
                boss.isCasting = true;
                boss.castTimer = 2.0f; // Pull for 2 seconds
                break;

            case BERSERK:
                boss.isEnraged = true;
                boss.enrageTimer = 5.0f; // Lasts 5 seconds
                VelocityComponent vel = vm.get(bossEntity);
                vel.maxSpeed *= 2.0f; // Double speed
                SoundManager.getInstance().play("spawn_breach", 1.0f);
                break;

            case SUMMON_MINIONS:
                enemyFactory.spawnSwarm(getEngine(), bossPos, "imp", 3, 1.5f);
                boss.isCasting = true;
                boss.castTimer = 1.0f;
                SoundManager.getInstance().play("spawn_breach", 1.0f);
                break;

            case DASH_ATTACK:
                VelocityComponent v = vm.get(bossEntity);
                Vector2 dashDir = new Vector2(playerPosVec).sub(bossPos).nor();
                v.velocity.set(dashDir).scl(v.maxSpeed * 4f);
                boss.isCasting = true;
                boss.castTimer = 0.5f;
                break;

            case TELEPORT_AMBUSH:
                Vector2 offset = new Vector2(MathUtils.random(-100, 100), MathUtils.random(-100, 100));
                Vector2 telePos = new Vector2(playerPosVec).add(offset);
                PositionComponent pmPos = pm.get(bossEntity);
                pmPos.position.set(telePos);
                SoundManager.getInstance().play("teleport", 1.0f);
                break;

            case ARCANE_NOVA:
                int bullets = 12;
                for (int i = 0; i < bullets; i++) {
                    spawnBossProjectile(bossPos, (360f / bullets) * i, "arcane_missile", Color.PURPLE);
                }
                boss.isCasting = true;
                boss.castTimer = 1.0f;
                break;

            case FIRE_FLAMETHROWER:
                float baseAngle = new Vector2(playerPosVec).sub(bossPos).angleDeg();
                for (int i = -2; i <= 2; i++) {
                    spawnBossProjectile(bossPos, baseAngle + (i * 10), "fireball", Color.ORANGE);
                }
                boss.isCasting = true;
                boss.castTimer = 1.0f;
                break;

            case FROST_BREATH:
                float frostAngle = new Vector2(playerPosVec).sub(bossPos).angleDeg();
                for (int i = -3; i <= 3; i++) {
                    Entity proj = spawnBossProjectile(bossPos, frostAngle + (i * 8), "ice_shard", Color.CYAN);
                    proj.add(new SlowComponent(0.5f, 2.0f));
                }
                boss.isCasting = true;
                boss.castTimer = 1.0f;
                break;

            case POISON_SPIT:
                Entity proj = spawnBossProjectile(bossPos, new Vector2(playerPosVec).sub(bossPos).angleDeg(), "poison", Color.GREEN);
                proj.add(new PoisonComponent(10f, 5.0f));
                boss.isCasting = true;
                boss.castTimer = 0.5f;
                break;

            case ELECTRIC_AURA:
                for (int i = 0; i < 8; i++) {
                    spawnBossProjectile(bossPos, (360f / 8) * i, "lightning", Color.YELLOW);
                }
                boss.isCasting = true;
                boss.castTimer = 1.0f;
                break;
        }
    }

    private Entity spawnBossProjectile(Vector2 startPos, float angleDeg, String textureName, Color tint) {
        Entity proj = new Entity();
        proj.add(new PositionComponent(startPos.x, startPos.y));
        Vector2 dir = new Vector2(1, 0).setAngleDeg(angleDeg);
        VelocityComponent vel = new VelocityComponent(300f);
        vel.velocity.set(dir).scl(300f);
        proj.add(vel);
        TextureManager tm = TextureManager.getInstance();
        if (tm.hasTexture(textureName)) {
            proj.add(new VisualComponent(20f, 20f, tm.getTexture(textureName)));
        } else {
            VisualComponent vis = new VisualComponent(20f, 20f, tint);
            proj.add(vis);
        }
        proj.add(new EnemyProjectileComponent(15f));
        proj.add(new LifetimeComponent(4.0f));
        proj.add(new CollisionComponent(10f, (short)0, (short)0));
        getEngine().addEntity(proj);
        return proj;
    }
}
