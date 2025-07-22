package org.kamatech.chess.api;

import org.kamatech.chess.Piece;
import java.util.Map;

public interface IPieceFactory {
    Map<String, Piece> createPiecesFromBoardCsv() throws Exception;

    Map<String, Piece> createDefaultPieces();

    Piece createPiece(String pieceType, int x, int y);

    Piece createPieceWithCooldown(String pieceCode, int x, int y, long cooldownMs);
}
