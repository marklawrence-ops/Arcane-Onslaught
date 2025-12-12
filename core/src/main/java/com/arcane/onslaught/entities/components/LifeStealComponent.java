package com.arcane.onslaught.entities.components;
import com.badlogic.ashley.core.Component;

public class LifeStealComponent implements Component {
    public int amount;
    public LifeStealComponent(int amount) { this.amount = amount; }
}
