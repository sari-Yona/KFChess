package org.kamatech.chess;

import java.awt.image.BufferedImage;

import java.awt.Graphics2D;

/**
 * Graphics - responsible for visual representation of a Piece on screen.
 * Operates in pixel units; displays animations based on the piece's physics
 * state.
 * Separate from Physics logic, can be replaced to change rendering without
 * affecting game logic.
 */
public class Graphics implements Cloneable {
    private final Piece piece;

    /**
     * Create graphics handler for given piece
     */
    public Graphics(Piece piece) {
        this.piece = piece;
    }

    /**
     * Draw the piece at specified location and size
     */
    public void draw(Graphics2D g2d, int x, int y, int width, int height) {
        // Try sprite animation
        BufferedImage sprite = GraphicsFactory.getSpriteForPiece(piece);
        if (sprite != null) {
            g2d.drawImage(sprite, x, y, width, height, null);
        } else {
            // Fallback drawing
            GraphicsFactory.drawPieceFallback(g2d, piece, x, y, width, height);
        }
    }

    @Override
    public Graphics clone() {
        return new Graphics(piece);
    }
}
