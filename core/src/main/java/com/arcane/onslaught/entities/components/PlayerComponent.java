package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

public class PlayerComponent implements Component {
    public float xp = 0;
    public float xpToNextLevel = 100;
    public int level = 1;
}
