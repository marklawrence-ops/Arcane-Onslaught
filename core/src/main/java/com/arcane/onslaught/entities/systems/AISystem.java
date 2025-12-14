package com.arcane.onslaught.entities.systems;

import com.arcane.onslaught.enemies.EnemyFactory;
import com.arcane.onslaught.entities.components.BossComponent.BossSkill;
import com.arcane.onslaught.utils.SoundManager;
import com.arcane.onslaught.utils.TextureManager;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;

public class AISystem extends IteratingSystem {
    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private ComponentMapper<AIComponent> am = ComponentMapper.getFor(AIComponent.class);
    private ComponentMapper<BossComponent> bm = ComponentMapper.getFor(BossComponent.class);
    private ComponentMapper<VisualComponent> vism = ComponentMapper.getFor(VisualComponent.class); // Needed for flashing

    private Vector2 playerPos = new Vector2();
    private EnemyFactory enemyFactory;

    public AISystem(EnemyFactory factory) {
        super(Family.all(AIComponent.class, PositionComponent.class, VelocityComponent.class).get());
        this.enemyFactory = factory;
    }

    @Override
    public void update(float deltaTime) {
        Family playerFamily = Family.all(PlayerComponent.class, PositionComponent.class).get();
        for (Entity entity : getEngine().getEntitiesFor(playerFamily)) {
            PositionComponent pos = pm.get(entity);
            playerPos.set(pos.position);
            break;
        }
        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BossComponent boss = bm.get(entity);

        if (boss != null) {
            processBossBehavior(entity, boss, deltaTime);
        } else {
            // Standard Enemy Logic
            PositionComponent pos = pm.get(entity);
            VelocityComponent vel = vm.get(entity);
            AIComponent ai = am.get(entity);

            if (ai.type == AIComponent.AIType.CHASE_PLAYER) {
                Vector2 direction = new Vector2(playerPos).sub(pos.position).nor();
                vel.velocity.set(direction).scl(vel.maxSpeed);
            }
        }
    }

    private void processBossBehavior(Entity entity, BossComponent boss, float deltaTime) {
        PositionComponent pos = pm.get(entity);
        VelocityComponent vel = vm.get(entity);
        VisualComponent vis = vism.get(entity);

        // --- STATE 1: CASTING (Skill is happening) ---
        if (boss.isCasting) {
            boss.castTimer -= deltaTime;
            vel.velocity.setZero(); // Rooted while casting
            if (boss.castTimer <= 0) {
                boss.isCasting = false;
                vis.color = Color.WHITE; // Reset color
            }
            return;
        }

        // --- STATE 2: TELEGRAPHING (Warning Phase) ---
        if (boss.isTelegraphing) {
            boss.telegraphTimer -= deltaTime;
            vel.velocity.setZero(); // Rooted while warning

            // Visual Warning: Flash colors based on skill type
            float flashSpeed = 15f;
            float alpha = (MathUtils.sin(boss.telegraphTimer * flashSpeed) + 1) / 2f;

            if (boss.nextSkill == BossSkill.DASH_ATTACK) {
                // Flash RED for physical attacks
                vis.color = new Color(1f, alpha, alpha, 1f);
            } else if (boss.nextSkill == BossSkill.SUMMON_MINIONS) {
                // Flash PURPLE for summoning
                vis.color = new Color(0.8f, 0.2f, 0.8f, 1f);
            } else if (boss.nextSkill == BossSkill.TELEPORT_AMBUSH) {
                // Fade out/Flash CYAN for teleport
                vis.color = new Color(0.2f, 1f, 1f, alpha);
            } else {
                // Flash YELLOW for elemental casts
                vis.color = new Color(1f, 1f, alpha, 1f);
            }

            // Time's up! Execute the skill
            if (boss.telegraphTimer <= 0) {
                boss.isTelegraphing = false;
                executeSkill(entity, boss, boss.nextSkill, pos.position);
                vis.color = Color.WHITE; // Reset visual
                boss.skillTimer = boss.skillCooldown; // Start Cooldown
            }
            return;
        }

        // --- STATE 3: COOLDOWN / CHASING ---
        if (boss.skillTimer > 0) {
            boss.skillTimer -= deltaTime;
        }

        // Chase Player
        Vector2 direction = new Vector2(playerPos).sub(pos.position).nor();
        vel.velocity.set(direction).scl(vel.maxSpeed);

        // --- TRIGGER NEW SKILL ---
        if (boss.skillTimer <= 0 && !boss.availableSkills.isEmpty()) {
            // 1. Pick Skill
            BossSkill skill = boss.availableSkills.get(MathUtils.random(boss.availableSkills.size() - 1));
            boss.nextSkill = skill;

            // 2. Start Telegraph Phase
            boss.isTelegraphing = true;

            // Set telegraph duration based on skill danger level
            switch (skill) {
                case DASH_ATTACK: boss.telegraphTimer = 1.0f; break; // Long warning for dash
                case TELEPORT_AMBUSH: boss.telegraphTimer = 0.8f; break;
                case SUMMON_MINIONS: boss.telegraphTimer = 1.5f; break; // Very long warning
                default: boss.telegraphTimer = 0.6f; break; // Quick warning for spells
            }
        }
    }

    private void executeSkill(Entity bossEntity, BossComponent boss, BossSkill skill, Vector2 bossPos) {
        System.out.println("Boss Executing: " + skill);

        switch (skill) {
            case SUMMON_MINIONS:
                enemyFactory.spawnSwarm(getEngine(), bossPos, "imp", 10, 1.5f);
                boss.isCasting = true;
                boss.castTimer = 0.5f; // Brief pause after summon
                SoundManager.getInstance().play("spawn_breach", 1.0f);
                break;

            case DASH_ATTACK:
                VelocityComponent vel = vm.get(bossEntity);
                Vector2 dashDir = new Vector2(playerPos).sub(bossPos).nor();
                vel.velocity.set(dashDir).scl(vel.maxSpeed * 5f); // Super fast dash
                boss.isCasting = true;
                boss.castTimer = 0.6f; // Dash duration
                break;

            case TELEPORT_AMBUSH:
                Vector2 offset = new Vector2(MathUtils.random(-150, 150), MathUtils.random(-150, 150));
                Vector2 teleportPos = new Vector2(playerPos).add(offset);
                teleportPos.x = MathUtils.clamp(teleportPos.x, 50, 1870);
                teleportPos.y = MathUtils.clamp(teleportPos.y, 50, 1030);

                PositionComponent pmPos = pm.get(bossEntity);
                pmPos.position.set(teleportPos);
                SoundManager.getInstance().play("teleport", 1.0f);
                boss.isCasting = true;
                boss.castTimer = 0.5f; // Recovery time
                break;

            // ... (Elemental skills remain largely the same, just triggered here) ...
            case ARCANE_NOVA:
                int bullets = 50;
                for (int i = 0; i < bullets; i++) {
                    spawnBossProjectile(bossPos, (360f / bullets) * i, "arcane_missile", Color.PURPLE);
                }
                boss.isCasting = true;
                boss.castTimer = 1.0f;
                break;

            case FIRE_FLAMETHROWER:
                Vector2 toPlayer = new Vector2(playerPos).sub(bossPos);
                float baseAngle = toPlayer.angleDeg();
                for (int i = -2; i <= 2; i++) {
                    spawnBossProjectile(bossPos, baseAngle + (i * 10), "fireball", Color.ORANGE);
                }
                boss.isCasting = true;
                boss.castTimer = 0.4f;
                break;

            case FROST_BREATH:
                float frostAngle = new Vector2(playerPos).sub(bossPos).angleDeg();
                for (int i = -3; i <= 3; i++) {
                    Entity proj = spawnBossProjectile(bossPos, frostAngle + (i * 8), "ice_shard", Color.CYAN);
                    proj.add(new SlowComponent(0.5f, 2.0f));
                }
                boss.isCasting = true;
                boss.castTimer = 0.5f;
                break;

            case POISON_SPIT:
                Entity proj = spawnBossProjectile(bossPos, new Vector2(playerPos).sub(bossPos).angleDeg(), "poison", Color.GREEN);
                proj.add(new PoisonComponent(10f, 5.0f));
                boss.isCasting = true;
                boss.castTimer = 0.3f;
                break;

            case ELECTRIC_AURA:
                for (int i = 0; i < 8; i++) {
                    spawnBossProjectile(bossPos, (360f / 8) * i, "lightning", Color.YELLOW);
                }
                boss.isCasting = true;
                boss.castTimer = 0.1f;
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
