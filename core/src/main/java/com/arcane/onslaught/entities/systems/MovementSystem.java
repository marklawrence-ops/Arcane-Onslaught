package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.utils.Constants;

public class MovementSystem extends IteratingSystem {
    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);

    public MovementSystem() {
        super(Family.all(PositionComponent.class, VelocityComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent pos = pm.get(entity);
        VelocityComponent vel = vm.get(entity);

        pos.position.x += vel.velocity.x * deltaTime;
        pos.position.y += vel.velocity.y * deltaTime;

        if (entity.getComponent(PlayerComponent.class) != null) {
            pos.position.x = MathUtils.clamp(pos.position.x,
                Constants.ARENA_OFFSET_X + Constants.PLAYER_SIZE / 2,
                Constants.ARENA_OFFSET_X + Constants.ARENA_WIDTH - Constants.PLAYER_SIZE / 2);
            pos.position.y = MathUtils.clamp(pos.position.y,
                Constants.ARENA_OFFSET_Y + Constants.PLAYER_SIZE / 2,
                Constants.ARENA_OFFSET_Y + Constants.ARENA_HEIGHT - Constants.PLAYER_SIZE / 2);
        }
    }
}
