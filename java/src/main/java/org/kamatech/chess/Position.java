package org.kamatech.chess;

/**
 * Represents a position on the chess board in row and column coordinates.
 */
public class Position {
    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return row + "," + col;
    }

    /**
     * Utility to convert from piece coordinates if needed.
     */
    public static Position fromCoordinates(double x, double y) {
        return new Position((int) x, (int) y);
    }
}
