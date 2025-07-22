package org.kamatech.chess;

public class Physics implements Cloneable {
    private final Piece piece;

    /**
     * Physics handler for a given piece
     */
    public Physics(Piece piece) {
        this.piece = piece;
    }

    /**
     * Update piece state and physics each frame
     */
    public void update() {
        if (piece != null && piece.getState() != null) {
            piece.getState().update();
        }
        // Additional physics (collisions, motion) can be added here
    }

    @Override
    public Physics clone() {
        return new Physics(piece);
    }
}
