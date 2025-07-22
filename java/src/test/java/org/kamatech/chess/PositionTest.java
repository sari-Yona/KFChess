package org.kamatech.chess;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PositionTest {

    @Test
    void testToStringAndGetters() {
        Position pos = new Position(3, 5);
        assertEquals(3, pos.getRow(), "Row getter should return correct value");
        assertEquals(5, pos.getCol(), "Col getter should return correct value");
        assertEquals("3,5", pos.toString(), "toString should return 'row,col'");
    }

    @Test
    void testFromCoordinates() {
        Position pos = Position.fromCoordinates(7.8, 4.2);
        assertEquals(7, pos.getRow(), "fromCoordinates should floor the X coordinate to row");
        assertEquals(4, pos.getCol(), "fromCoordinates should floor the Y coordinate to col");
    }
}
