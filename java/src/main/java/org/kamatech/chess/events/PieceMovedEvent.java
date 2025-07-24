package org.kamatech.chess.events;


public class PieceMovedEvent implements Event {
    public final String from;
    public final String to;

    public PieceMovedEvent(String from, String to) {
        this.from = from;
        this.to = to;
    }
}
