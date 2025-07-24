package org.kamatech.chess.events;

public class GameEndedEvent implements Event {
    public final String winner;

    public GameEndedEvent(String winner) {
        this.winner = winner;
    }
}