package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.arcane.onslaught.entities.components.*;

public class LifetimeSystem extends IteratingSystem {
    private ComponentMapper<LifetimeComponent> lm = ComponentMapper.getFor(LifetimeComponent.class);
    private ComponentMapper<VisualComponent> vm = ComponentMapper.getFor(VisualComponent.class);

    public LifetimeSystem() {
        super(Family.all(LifetimeComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LifetimeComponent life = lm.get(entity);
        VisualComponent vis = vm.get(entity);

        life.timer += deltaTime;

        // Auto-Fade logic for sprites
        if (vis != null && vis.sprite != null) {
            float alpha = 1.0f - (life.timer / life.duration);
            if (alpha < 0) alpha = 0;
            vis.sprite.setAlpha(alpha);
        }

        // Remove when time is up
        if (life.timer >= life.duration) {
            getEngine().removeEntity(entity);
        }
    }
}
