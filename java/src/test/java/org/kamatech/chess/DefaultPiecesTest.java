package org.kamatech.chess;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultPiecesTest {

    @Test
    void testDefaultPiecesUniquePositions() {
        PieceFactory factory = new PieceFactory(new GraphicsFactory(), new PhysicsFactory());
        Map<String, Piece> pieces = factory.createDefaultPieces();
        assertEquals(16, pieces.size(), "Should have 16 default pieces");
        Set<String> positions = new HashSet<>();
        for (Piece p : pieces.values()) {
            String coord = p.getX() + "," + p.getY();
            assertTrue(positions.add(coord), "Duplicate default piece position: " + coord);
        }
    }
}
