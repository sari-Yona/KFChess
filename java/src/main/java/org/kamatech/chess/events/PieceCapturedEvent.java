package org.kamatech.chess.events;

public class PieceCapturedEvent implements Event {
    public final String pieceType; // "Pawn", "Knight", "Queen" וכו'

    public PieceCapturedEvent(String pieceType) {
        this.pieceType = pieceType;
    }
}
