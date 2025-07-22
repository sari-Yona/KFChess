package org.kamatech.chess;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class PieceFactoryTest {

    @Test
    void testCreateDefaultPiecesSize() {
        // Arrange: create factory with real implementations
        PieceFactory factory = new PieceFactory(new GraphicsFactory(), new PhysicsFactory());

        // Act: generate default pieces
        Map<String, Piece> pieces = factory.createDefaultPieces();

        // Assert: should have 16 pieces and contain kings of both colors
        assertNotNull(pieces, "Default pieces map should not be null");
        assertEquals(16, pieces.size(), "Default pieces count should be 16");
        assertTrue(pieces.containsKey("KW"), "Should contain white king (KW)");
        assertTrue(pieces.containsKey("KB"), "Should contain black king (KB)");
    }

    @Test
    void testCreatePiecePosition() {
        // Arrange
        PieceFactory factory = new PieceFactory(new GraphicsFactory(), new PhysicsFactory());

        // Act
        Piece p = factory.createPiece("RW", 2, 5);

        // Assert
        assertNotNull(p, "Piece should not be null");
        assertEquals("RW", p.getId(), "Piece ID mismatch");
        assertEquals(2.0, p.getX(), "X position mismatch");
        assertEquals(5.0, p.getY(), "Y position mismatch");
        assertTrue(p.isWhite(), "Piece RW should be white");
    }

    @Test
    void testCreatePieceWithCooldown() {
        // Arrange
        PieceFactory factory = new PieceFactory(new GraphicsFactory(), new PhysicsFactory());
        long cooldown = 500L;

        // Act
        Piece p = factory.createPieceWithCooldown("PB", 0, 1, cooldown);

        // Assert
        assertNotNull(p, "Piece should not be null");
        assertEquals("PB", p.getId(), "Piece ID mismatch");
        assertEquals(0.0, p.getX(), "X position mismatch");
        assertEquals(1.0, p.getY(), "Y position mismatch");
        assertFalse(p.isWhite(), "Piece PB should be black");
        Moves moves = p.getState().getMoves();
        assertEquals(cooldown, moves.getCooldown(), "Cooldown mismatch");
        assertFalse(moves.getAllowedMoves().isEmpty(), "Allowed moves should not be empty");
    }
}
