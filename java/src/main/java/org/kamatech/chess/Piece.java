package org.kamatech.chess;

/**
 * Represents a chess piece with position, state, and game logic
 */
public class Piece implements Cloneable {
    private final String id;
    private State state;
    private double positionX; // Position in meters
    private double positionY; // Position in meters
    private long lastMoveTime;
    private boolean isWhite;

    public Piece(String id, State state) {
        this.id = id;
        this.state = state;
        this.positionX = 0.0;
        this.positionY = 0.0;
        this.lastMoveTime = 0;
        this.isWhite = id.endsWith("W"); // Simple heuristic: pieces ending with W are white
    }

    public Piece(String id, State state, double x, double y, boolean isWhite) {
        this.id = id;
        this.state = state;
        this.positionX = x;
        this.positionY = y;
        this.lastMoveTime = 0;
        this.isWhite = isWhite;
    }

    public String getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    /* ----------- Position Methods ----------- */
    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    // Convenience methods for shorter syntax
    public double getX() {
        return positionX;
    }

    public double getY() {
        return positionY;
    }

    public void setPosition(double x, double y) {
        this.positionX = x;
        this.positionY = y;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setWhite(boolean isWhite) {
        this.isWhite = isWhite;
    }

    /* ----------- Game Logic Methods ----------- */
    public boolean canMove() {
        return state.canPerformAction();
    }

    public boolean attemptMove(double newX, double newY) {
        if (!canMove()) {
            return false;
        }

        // Check if move is valid according to piece's allowed moves
        // This would be expanded based on piece type logic
        setPosition(newX, newY);
        state.setState(State.PieceState.MOVE);
        lastMoveTime = System.currentTimeMillis();
        return true;
    }

    public boolean attemptJump(double newX, double newY) {
        if (!canMove()) {
            return false;
        }

        setPosition(newX, newY);
        state.setState(State.PieceState.JUMP);
        lastMoveTime = System.currentTimeMillis();
        return true;
    }

    public void update() {
        state.update();
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    @Override
    public Piece clone() {
        try {
            Piece cloned = (Piece) super.clone();
            cloned.state = state.clone();
            // Note: primitive fields (positionX, positionY, etc.) are automatically copied
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
}
