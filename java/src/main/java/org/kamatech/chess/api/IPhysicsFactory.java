package org.kamatech.chess.api;

import org.kamatech.chess.Moves;
import org.kamatech.chess.Physics;

public interface IPhysicsFactory {
    Physics createPhysics(String pieceType, Moves moves);
}
