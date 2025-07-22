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
}
