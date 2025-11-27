package com.arcane.onslaught.events;

public class EnemySpawnEvent extends Event {
    private final int count;
    private final String enemyType;

    public EnemySpawnEvent(int count, String enemyType) {
        this.count = count;
        this.enemyType = enemyType;
    }

    public int getCount() { return count; }
    public String getEnemyType() { return enemyType; }
}
