package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.arcane.onslaught.entities.components.*;

public class ProjectileSystem extends IteratingSystem {
    private ComponentMapper<ProjectileComponent> pm = ComponentMapper.getFor(ProjectileComponent.class);

    public ProjectileSystem() {
        super(Family.all(ProjectileComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ProjectileComponent proj = pm.get(entity);
        proj.timeAlive += deltaTime;

        if (proj.timeAlive >= proj.lifetime) {
            getEngine().removeEntity(entity);
        }
    }
}
