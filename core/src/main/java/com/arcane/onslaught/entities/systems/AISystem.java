package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.arcane.onslaught.entities.components.*;

public class AISystem extends IteratingSystem {
    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private ComponentMapper<AIComponent> am = ComponentMapper.getFor(AIComponent.class);

    private Vector2 playerPos = new Vector2();

    public AISystem() {
        super(Family.all(AIComponent.class, PositionComponent.class, VelocityComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
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
        VelocityComponent vel = vm.get(entity);
        AIComponent ai = am.get(entity);

        if (ai.type == AIComponent.AIType.CHASE_PLAYER) {
            Vector2 direction = new Vector2(playerPos).sub(pos.position).nor();
            vel.velocity.set(direction).scl(vel.maxSpeed);
        }
    }
}
