package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.UpgradeHelper;

/**
 * Makes XP orbs and health orbs move toward the player when they're nearby
 */
public class XPMagnetSystem extends IteratingSystem {
    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private PlayerBuild playerBuild;

    private Vector2 playerPos = new Vector2();
    private static final float MAGNET_SPEED = 400f;
    private static final float BASE_MAGNET_RANGE = 100f;

    public XPMagnetSystem(PlayerBuild playerBuild) {
        super(Family.all(PositionComponent.class).one(XPOrbComponent.class, HealthOrbComponent.class).get());
        this.playerBuild = playerBuild;
    }

    @Override
    public void update(float deltaTime) {
        // Find player position
        for (Entity entity : getEngine().getEntitiesFor(Family.all(PlayerComponent.class, PositionComponent.class).get())) {
            PositionComponent pos = pm.get(entity);
            playerPos.set(pos.position);
            break;
        }

        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent pos = pm.get(entity);

        float distance = pos.position.dst(playerPos);

        // Apply pickup range multiplier from upgrades
        float magnetRange = BASE_MAGNET_RANGE * UpgradeHelper.getPickupRangeMultiplier(playerBuild);

        // If player is close enough, pull orb toward them
        if (distance < magnetRange) {
            Vector2 direction = new Vector2(playerPos).sub(pos.position).nor();
            pos.position.add(direction.scl(MAGNET_SPEED * deltaTime));
        }
    }
}
