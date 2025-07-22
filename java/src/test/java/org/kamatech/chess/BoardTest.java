package org.kamatech.chess;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    void testMetersPixelsConversion() {
        Img mockImg = new Img(true);
        Board board = new Board(10, 20, 2, 4, 5, 5, mockImg);
        // meters to pixels
        assertEquals(10, board.metersToPixelsX(2), "metersToPixelsX should convert correctly");
        assertEquals(10, board.metersToPixelsY(2), "metersToPixelsY should convert correctly");
        // pixels to meters
        assertEquals(2.0, board.pixelsToMetersX(10), 0.0001, "pixelsToMetersX should convert correctly");
        assertEquals(2.0, board.pixelsToMetersY(10), 0.0001, "pixelsToMetersY should convert correctly");
    }

    @Test
    void testIsValidPosition() {
        Img mockImg = new Img(true);
        // widthCells*cellWidthMeters = 5*4 = 20, same for height
        Board board = new Board(10, 20, 2, 4, 5, 5, mockImg);
        assertTrue(board.isValidPosition(0, 0), "Position at origin should be valid");
        // Within X bound (<20) and Y bound (<10)
        assertTrue(board.isValidPosition(19.9, 9.9), "Position within bounds should be valid");
        assertFalse(board.isValidPosition(-1, 5), "Negative X should be invalid");
        // Y equal to max (heightCells * cellHeightMeters = 5*2 = 10)
        assertFalse(board.isValidPosition(5, 10), "Y equal to max should be invalid");
    }
}
