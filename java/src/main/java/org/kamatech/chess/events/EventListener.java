package org.kamatech.chess.events;

public interface EventListener<T extends Event> {
    void onEvent(T event);
}