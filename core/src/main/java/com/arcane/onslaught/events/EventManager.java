package com.arcane.onslaught.events;

import java.util.*;

public class EventManager {
    private static EventManager instance;
    private Map<Class<? extends Event>, List<EventListener>> listeners;

    private EventManager() {
        listeners = new HashMap<>();
    }

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public <T extends Event> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<EventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        List<EventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener listener : new ArrayList<>(eventListeners)) {
                if (!event.isHandled()) {
                    listener.onEvent(event);
                }
            }
        }
    }

    public void clear() {
        listeners.clear();
    }
}
