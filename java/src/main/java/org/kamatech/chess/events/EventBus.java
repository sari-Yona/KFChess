package org.kamatech.chess.events;

import java.util.*;

public class EventBus {
    private final Map<Class<? extends Event>, List<EventListener<? extends Event>>> listeners = new HashMap<>();

    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public <T extends Event> void publish(T event) {
        List<EventListener<? extends Event>> registered = listeners.getOrDefault(event.getClass(), List.of());
        for (EventListener<?> listener : registered) {
            @SuppressWarnings("unchecked")
            EventListener<T> typedListener = (EventListener<T>) listener;
            typedListener.onEvent(event);
        }
    }
}