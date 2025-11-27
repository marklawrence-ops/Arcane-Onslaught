package com.arcane.onslaught.events;

public class AutoCastEvent extends Event {
    private final float x, y;
    private final String spellType;

    public AutoCastEvent(float x, float y, String spellType) {
        this.x = x;
        this.y = y;
        this.spellType = spellType;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public String getSpellType() { return spellType; }
}
