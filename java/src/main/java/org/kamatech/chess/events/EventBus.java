package org.kamatech.chess.events;

import java.util.*;

public class EventBus {
    private final Map<Class<? extends Event>, List<EventListener<? extends Event>>> listeners = new HashMap<>();

    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
        System.out.println("DEBUG: EventBus - Subscribed " + listener.getClass().getSimpleName() + " to "
                + eventType.getSimpleName());
    }

    public <T extends Event> void publish(T event) {
        List<EventListener<? extends Event>> registered = listeners.getOrDefault(event.getClass(), List.of());
        System.out.println("DEBUG: EventBus - Publishing " + event.getClass().getSimpleName() + " to "
                + registered.size() + " listeners");
        for (EventListener<?> listener : registered) {
            @SuppressWarnings("unchecked")
            EventListener<T> typedListener = (EventListener<T>) listener;
            System.out.println("DEBUG: EventBus - Calling onEvent on " + listener.getClass().getSimpleName());
            typedListener.onEvent(event);
        }
    }
}