package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.input.GameInputProcessor;

public class PlayerInputSystem extends IteratingSystem {
    private ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private GameInputProcessor inputProcessor;

    public PlayerInputSystem(GameInputProcessor inputProcessor) {
        super(Family.all(PlayerComponent.class, VelocityComponent.class).get());
        this.inputProcessor = inputProcessor;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        VelocityComponent vel = vm.get(entity);
        vel.velocity.set(inputProcessor.getInputVector());
    }
}
