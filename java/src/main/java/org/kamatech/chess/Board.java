package org.kamatech.chess;

/**
 * Represents the chess board with its dimensions in both pixels and meters.
 */
public class Board implements Cloneable {
    private final int cellHeightPixels;
    private final int cellWidthPixels;
    private final int cellHeightMeters;
    private final int cellWidthMeters;
    private final int widthCells;
    private final int heightCells;
    private final Img image;

    public Board(int cellHeightPixels, int cellWidthPixels,
            int cellHeightMeters, int cellWidthMeters,
            int widthCells, int heightCells, Img image) {
        this.cellHeightPixels = cellHeightPixels;
        this.cellWidthPixels = cellWidthPixels;
        this.cellHeightMeters = cellHeightMeters;
        this.cellWidthMeters = cellWidthMeters;
        this.widthCells = widthCells;
        this.heightCells = heightCells;
        this.image = image;
    }

    public Img getImage() {
        return image;
    }

    @Override
    public Board clone() {
        // Create a new Board with a cloned image
        return new Board(
                cellHeightPixels,
                cellWidthPixels,
                cellHeightMeters,
                cellWidthMeters,
                widthCells,
                heightCells,
                image.clone());
    }

    // Getters
    public int getCellHeightPixels() {
        return cellHeightPixels;
    }

    public int getCellWidthPixels() {
        return cellWidthPixels;
    }

    public int getCellHeightMeters() {
        return cellHeightMeters;
    }

    public int getCellWidthMeters() {
        return cellWidthMeters;
    }

    public int getWidthCells() {
        return widthCells;
    }

    public int getHeightCells() {
        return heightCells;
    }

    /* ----------- Conversion Methods ----------- */
    /**
     * Convert meters to pixels in X direction
     */
    public int metersToPixelsX(double meters) {
        return (int) Math.round(meters * cellWidthPixels / cellWidthMeters);
    }

    /**
     * Convert meters to pixels in Y direction
     */
    public int metersToPixelsY(double meters) {
        return (int) Math.round(meters * cellHeightPixels / cellHeightMeters);
    }

    /**
     * Convert pixels to meters in X direction
     */
    public double pixelsToMetersX(int pixels) {
        return (double) pixels * cellWidthMeters / cellWidthPixels;
    }

    /**
     * Convert pixels to meters in Y direction
     */
    public double pixelsToMetersY(int pixels) {
        return (double) pixels * cellHeightMeters / cellHeightPixels;
    }

    /**
     * Check if position in meters is within board bounds
     */
    public boolean isValidPosition(double metersX, double metersY) {
        return metersX >= 0 && metersX < (widthCells * cellWidthMeters) &&
                metersY >= 0 && metersY < (heightCells * cellHeightMeters);
    }
}
