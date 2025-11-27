package com.arcane.onslaught.events;

import java.util.HashSet;
import java.util.Set;

public class PlayerInputEvent extends Event {
    public enum Direction { UP, DOWN, LEFT, RIGHT, NONE }
    private final Set<Direction> directions;

    public PlayerInputEvent(Set<Direction> directions) {
        this.directions = new HashSet<>(directions);
    }

    public Set<Direction> getDirections() {
        return directions;
    }

    public boolean isMoving() {
        return !directions.isEmpty() && !directions.contains(Direction.NONE);
    }
}
