package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class AIComponent implements Component {
    public enum AIType { CHASE_PLAYER, WANDER, STATIONARY }

    public AIType type = AIType.CHASE_PLAYER;
    public Vector2 targetPosition = new Vector2();
}
