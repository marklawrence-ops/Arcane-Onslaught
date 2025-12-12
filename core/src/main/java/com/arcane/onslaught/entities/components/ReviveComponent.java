package com.arcane.onslaught.entities.components;
import com.badlogic.ashley.core.Component;

public class ReviveComponent implements Component {
    public int lives;
    public ReviveComponent(int lives) { this.lives = lives; }
}
