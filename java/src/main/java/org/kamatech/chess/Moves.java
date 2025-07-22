package org.kamatech.chess;

import java.util.List;

public class Moves implements Cloneable {
    private final List<String> allowedMoves;
    private final long cooldown;

    public Moves(List<String> allowedMoves, long cooldown) {
        this.allowedMoves = allowedMoves;
        this.cooldown = cooldown;
    }

    public List<String> getAllowedMoves() {
        return allowedMoves;
    }

    public long getCooldown() {
        return cooldown;
    }

    @Override
    public Moves clone() {
        try {
            return (Moves) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }

    /**
     * Movement logic methods moved from Game.java to separate concerns
     */

    /**
     * Handle pawn movement with special rules
     */
    public static void handlePawnMove(Piece piece, int dx, int dy) {
        double newX = piece.getX() + dx;
        double newY = piece.getY() + dy;

        // Basic bounds check
        if (newX >= 0 && newX < 8 && newY >= 0 && newY < 8) {
            piece.setPosition(newX, newY);
            piece.getState().setState(State.PieceState.REST);
        }
    }

    /**
     * Handle knight movement with L-shaped pattern
     */
    public static void handleKnightMove(Piece piece, int dx, int dy) {
        // Knight moves in L-shape: 2 squares in one direction, 1 in perpendicular
        // For simplicity, just allow the move if it's valid
        double newX = piece.getX() + dx;
        double newY = piece.getY() + dy;

        if (newX >= 0 && newX < 8 && newY >= 0 && newY < 8) {
            piece.setPosition(newX, newY);
            piece.getState().setState(State.PieceState.REST);
        }
    }

    /**
     * Handle sliding movement for pieces like rook, bishop, queen
     */
    public static void slideMove(Piece piece, int dx, int dy) {
        double currentX = piece.getX();
        double currentY = piece.getY();
        double newX = currentX + dx;
        double newY = currentY + dy;

        // Basic bounds check
        if (newX >= 0 && newX < 8 && newY >= 0 && newY < 8) {
            piece.setPosition(newX, newY);
            piece.getState().setState(State.PieceState.REST);
        }
    }

    /**
     * Improved sliding movement with direction handling
     */
    public static void slideMoveBetter(Piece piece, int directionX, int directionY) {
        double currentX = piece.getX();
        double currentY = piece.getY();

        // Move one step in the specified direction
        double newX = currentX + directionX;
        double newY = currentY + directionY;

        // Bounds checking
        if (newX >= 0 && newX < 8 && newY >= 0 && newY < 8) {
            piece.setPosition(newX, newY);
            piece.getState().setState(State.PieceState.REST);
        }
    }

    /**
     * Check if a piece belongs to a specific player
     */
    public static boolean isPieceOwnedByPlayer(Piece piece, Command.Player player) {
        if (piece == null)
            return false;

        switch (player) {
            case WHITE:
                return piece.isWhite();
            case BLACK:
                return !piece.isWhite();
            case SYSTEM:
                return true; // System can control any piece
            default:
                return false;
        }
    }
}
