package org.kamatech.chess;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.kamatech.chess.api.IPieceFactory;

public class IPieceFactoryTest {

    private final IPieceFactory factory = new PieceFactory(new GraphicsFactory(), new PhysicsFactory());

    @Test
    void testCreateDefaultPiecesNonNullAndSize() {
        Map<String, Piece> pieces = factory.createDefaultPieces();
        assertNotNull(pieces, "Default pieces should not be null");
        assertEquals(16, pieces.size(), "Expected 16 default pieces");
        assertTrue(pieces.containsKey("KW"), "Should contain white king");
        assertTrue(pieces.containsKey("KB"), "Should contain black king");
    }

    @Test
    void testCreatePieceAndProperties() {
        Piece w = factory.createPiece("QW", 3, 4);
        assertNotNull(w, "Queen white should be created");
        assertEquals("QW", w.getId());
        assertTrue(w.isWhite());
        assertEquals(3.0, w.getX());
        assertEquals(4.0, w.getY());
        // Default cooldown from template
        long defaultCd = factory.createDefaultPieces().get("QW").getState().getMoves().getCooldown();
        assertEquals(defaultCd, w.getState().getMoves().getCooldown());
    }

    @Test
    void testCreatePieceWithCooldownOverride() {
        long cd = 1234L;
        Piece p = factory.createPieceWithCooldown("QB", 3, 3, cd);
        assertNotNull(p, "Piece should not be null");
        assertEquals("QB", p.getId());
        assertFalse(p.isWhite());
        assertEquals(cd, p.getState().getMoves().getCooldown(), "Cooldown override failed");
    }

    @Test
    void testCreatePiecesFromBoardCsvLoadsAllPieces() throws Exception {
        Map<String, Piece> pieces = factory.createPiecesFromBoardCsv();
        assertNotNull(pieces, "Pieces map should not be null");
        // board.csv defines 32 pieces (16 white + 16 black)
        assertEquals(32, pieces.size(), "Expected 32 pieces from board.csv");
        // sample key format: <pieceId>_<row>_<col>
        assertTrue(pieces.containsKey("RB_0_0"), "Should contain black rook at (0,0)");
        assertTrue(pieces.containsKey("PW_6_7"), "Should contain white pawn at (7,6)");
    }
}
