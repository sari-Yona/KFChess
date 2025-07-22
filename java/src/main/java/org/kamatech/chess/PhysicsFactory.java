package org.kamatech.chess;

import org.kamatech.chess.api.IPhysicsFactory;

public class PhysicsFactory implements IPhysicsFactory {
    /**
     * Create a Physics handler for a specific piece.
     */
    public Physics createPhysics(Piece piece) {
        return new Physics(piece);
    }

    /**
     * @deprecated Legacy method, cannot infer Piece context.
     */
    @Deprecated
    @Override
    public Physics createPhysics(String pieceType, Moves moves) {
        // Legacy support: produce no-op physics
        return new Physics(null);
    }
}
