package com.arcane.onslaught.events;

@FunctionalInterface
public interface EventListener<T extends Event> {
    void onEvent(T event);
}
