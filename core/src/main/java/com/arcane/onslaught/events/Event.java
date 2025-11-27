package com.arcane.onslaught.events;

/**
 * Base Event class - all game events extend this
 */
public abstract class Event {
    private boolean handled = false;

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
