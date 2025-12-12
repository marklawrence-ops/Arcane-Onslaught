package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.utils.TextureManager;
import com.arcane.onslaught.utils.Constants;

public class PlayerSpawnSystem extends IteratingSystem {
    private ComponentMapper<SpawningComponent> sm = ComponentMapper.getFor(SpawningComponent.class);
    private ComponentMapper<VisualComponent> vm = ComponentMapper.getFor(VisualComponent.class);
    private ComponentMapper<VelocityComponent> velm = ComponentMapper.getFor(VelocityComponent.class);

    private Entity beamEntity;
    private Entity portalEntity;
    private boolean effectsSpawned = false;

    private static final float MAX_BEAM_WIDTH = 600f;
    private static final float MAX_PORTAL_SIZE = 140f;

    public PlayerSpawnSystem() {
        super(Family.all(PlayerComponent.class, SpawningComponent.class).get());
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        SpawningComponent spawn = sm.get(player);
        VisualComponent playerVis = vm.get(player);
        VelocityComponent playerVel = velm.get(player);

        // 1. Initialize Effects
        if (!effectsSpawned) {
            spawnVisuals(player);
            effectsSpawned = true;
            if (playerVis != null && playerVis.sprite != null) {
                playerVis.sprite.setAlpha(0f);
            }
        }

        // 2. Lock Player
        if (playerVel != null) {
            playerVel.velocity.setZero();
        }

        // 3. Animation Progress
        spawn.timer += deltaTime;
        float progress = Math.min(1f, spawn.timer / spawn.duration);

        // --- ANIMATE VISUALS ---

        // A. BEAM
        if (beamEntity != null) {
            VisualComponent beamVis = beamEntity.getComponent(VisualComponent.class);
            if (beamVis != null) {
                float beamWidthProgress;
                if (progress < 0.2f) {
                    beamWidthProgress = Interpolation.pow2Out.apply(progress / 0.2f);
                } else {
                    beamWidthProgress = 1f - Interpolation.pow2In.apply((progress - 0.2f) / 0.8f);
                }
                beamVis.width = MAX_BEAM_WIDTH * beamWidthProgress;

                if (progress > 0.8f && beamVis.sprite != null) {
                    beamVis.sprite.setAlpha(1f - (progress - 0.8f) / 0.2f);
                }
            }
        }

        // B. PORTAL (The Magic Circle)
        if (portalEntity != null) {
            VisualComponent portalVis = portalEntity.getComponent(VisualComponent.class);
            RotationComponent rot = portalEntity.getComponent(RotationComponent.class);

            if (portalVis != null) {
                // Grow logic
                float scale = Interpolation.swingOut.apply(Math.min(1f, progress * 1.5f));
                portalVis.width = MAX_PORTAL_SIZE * scale;
                portalVis.height = MAX_PORTAL_SIZE * scale;

                // --- FIX: Update Origin ---
                // We must update the sprite size and origin HERE so it rotates around the center, not the corner
                if (portalVis.sprite != null) {
                    portalVis.sprite.setSize(portalVis.width, portalVis.height);
                    portalVis.sprite.setOriginCenter();
                }
            }

            // Spin Logic
            if (rot != null) {
                // Decays from 300 to 0.90
                float targetSpeed = 0.90f;
                rot.degreesPerSecond = (300f * (1f - progress)) + targetSpeed;
            }
        }

        // C. PLAYER
        if (playerVis != null && playerVis.sprite != null) {
            if (progress > 0.3f) {
                float playerProgress = (progress - 0.3f) / 0.7f;
                playerVis.sprite.setAlpha(MathUtils.clamp(playerProgress * 2f, 0f, 1f));

                float scale = Interpolation.swingOut.apply(MathUtils.clamp(playerProgress, 0f, 1f));
                float baseSize = Constants.PLAYER_SIZE * 2f;
                playerVis.width = baseSize * scale;
                playerVis.height = baseSize * scale;
            }
        }

        // 4. CLEANUP
        if (spawn.timer >= spawn.duration) {
            player.remove(SpawningComponent.class);

            if (playerVis != null) {
                float normalSize = Constants.PLAYER_SIZE * 2f;
                playerVis.width = normalSize;
                playerVis.height = normalSize;
                if (playerVis.sprite != null) {
                    playerVis.sprite.setAlpha(1f);
                    playerVis.sprite.setScale(1f);
                }
            }

            if (beamEntity != null) getEngine().removeEntity(beamEntity);

            // --- FIX: Portal Idle State ---
            if (portalEntity != null) {
                RotationComponent rot = portalEntity.getComponent(RotationComponent.class);
                if (rot != null) {
                    rot.degreesPerSecond = 0.90f;
                }
                VisualComponent pVis = portalEntity.getComponent(VisualComponent.class);
                if (pVis != null) {
                    pVis.width = MAX_PORTAL_SIZE;
                    pVis.height = MAX_PORTAL_SIZE;
                    if (pVis.sprite != null) {
                        pVis.sprite.setAlpha(0.8f);
                        // Ensure origin is perfectly centered for the idle spin
                        pVis.sprite.setSize(MAX_PORTAL_SIZE, MAX_PORTAL_SIZE);
                        pVis.sprite.setOriginCenter();
                    }
                }
            }
        }
    }

// Inside PlayerSpawnSystem.java -> spawnVisuals()

    private void spawnVisuals(Entity player) {
        PositionComponent playerPos = player.getComponent(PositionComponent.class);
        TextureManager tm = TextureManager.getInstance();

        // 1. Portal (Bottom Layer)
        portalEntity = new Entity();
        portalEntity.add(new PositionComponent(playerPos.position.x, playerPos.position.y));

        if (tm.hasTexture("magic_circle")) {
            VisualComponent vis = new VisualComponent(0f, 0f, tm.getTexture("magic_circle"));
            vis.sprite.setOriginCenter();
            vis.zIndex = 0; // --- FLOOR LAYER ---
            portalEntity.add(vis);
        } else {
            portalEntity.add(new VisualComponent(0f, 0f, Color.CYAN));
        }

        portalEntity.add(new RotationComponent(360f));
        getEngine().addEntity(portalEntity);

        // 2. Beam (Behind Player but above Portal)
        beamEntity = new Entity();
        beamEntity.add(new PositionComponent(playerPos.position.x, playerPos.position.y + 250f));

        if (tm.hasTexture("teleport_beam")) {
            VisualComponent vis = new VisualComponent(0f, 600f, tm.getTexture("teleport_beam"));
            vis.zIndex = 5; // --- BEHIND PLAYER (10) ---
            beamEntity.add(vis);
        } else {
            beamEntity.add(new VisualComponent(0f, 600f, Color.WHITE));
        }

        getEngine().addEntity(beamEntity);
    }
}
