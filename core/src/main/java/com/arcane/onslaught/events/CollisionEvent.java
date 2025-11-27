package com.arcane.onslaught.events;

public class CollisionEvent extends Event {
    public enum CollisionType { PROJECTILE_ENEMY, PLAYER_ENEMY, PLAYER_XP_ORB }

    private final CollisionType type;
    private final Object entityA;
    private final Object entityB;

    public CollisionEvent(CollisionType type, Object entityA, Object entityB) {
        this.type = type;
        this.entityA = entityA;
        this.entityB = entityB;
    }

    public CollisionType getType() { return type; }
    public Object getEntityA() { return entityA; }
    public Object getEntityB() { return entityB; }
}
