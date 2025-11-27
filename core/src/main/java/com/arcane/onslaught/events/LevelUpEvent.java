package com.arcane.onslaught.events;

public class LevelUpEvent extends Event {
    private final int newLevel;

    public LevelUpEvent(int newLevel) {
        this.newLevel = newLevel;
    }

    public int getNewLevel() { return newLevel; }
}
