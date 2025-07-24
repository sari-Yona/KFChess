package org.kamatech.chess.events;

public class PieceMovedEvent implements Event {
    public final String from;
    public final String to;
    public final String player; // "WHITE" or "BLACK"
    public final String pieceType; // "P", "R", "N", "B", "Q", "K"
    public final long timestamp;
    public final int moveNumber;
    public final String capturedPiece; // null if no capture, otherwise piece type

    public PieceMovedEvent(String from, String to, String player, String pieceType,
            int moveNumber, String capturedPiece) {
        this.from = from;
        this.to = to;
        this.player = player;
        this.pieceType = pieceType;
        this.moveNumber = moveNumber;
        this.capturedPiece = capturedPiece;
        this.timestamp = System.currentTimeMillis();
    }
}
