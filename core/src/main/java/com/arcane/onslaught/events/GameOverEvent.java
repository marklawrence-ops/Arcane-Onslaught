package com.arcane.onslaught.events;

public class GameOverEvent extends Event {
    private final float survivalTime;
    private final int finalLevel;

    public GameOverEvent(float survivalTime, int finalLevel) {
        this.survivalTime = survivalTime;
        this.finalLevel = finalLevel;
    }

    public float getSurvivalTime() { return survivalTime; }
    public int getFinalLevel() { return finalLevel; }
}
