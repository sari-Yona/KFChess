package org.kamatech.chess;

import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

public class GraphicsFactoryTest {

    @Test
    void testGetSpriteForPieceReturnsImageWhenSpritesExist() {
        PieceFactory pf = new PieceFactory(new GraphicsFactory(), new PhysicsFactory());
        Piece p = pf.createPiece("PB", 0, 0);
        BufferedImage img = GraphicsFactory.getSpriteForPiece(p);
        assertNotNull(img, "Expected a sprite image when sprites are available");
    }

    @Test
    void testDrawPieceFallbackChangesImage() {
        PieceFactory pf = new PieceFactory(new GraphicsFactory(), new PhysicsFactory());
        Piece p = pf.createPiece("KW", 1, 1);
        int w = 100, h = 100;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        // Fill background white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);
        // Draw fallback
        GraphicsFactory.drawPieceFallback(g2d, p, 0, 0, w, h);
        g2d.dispose();
        // Verify that at least one pixel is not white
        boolean changed = false;
        for (int x = 0; x < w && !changed; x++) {
            for (int y = 0; y < h; y++) {
                if (img.getRGB(x, y) != Color.WHITE.getRGB()) {
                    changed = true;
                    break;
                }
            }
        }
        assertTrue(changed, "Fallback draw should change image pixels from white");
    }

    @Test
    void testDrawHoverEffect() {
        int w = 50, h = 50;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);
        GraphicsFactory.drawHoverEffect(g2d, 10, 10, 20, 20);
        g2d.dispose();
        // The border should draw at (12,12)
        assertNotEquals(Color.WHITE.getRGB(), img.getRGB(12, 12), "Hover effect should draw colored border");
    }

    @Test
    void testDrawSelectionBorder() {
        int w = 50, h = 50;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);
        // Draw for white
        GraphicsFactory.drawSelectionBorder(g2d, 5, 5, 20, 20, true);
        // Draw for black
        GraphicsFactory.drawSelectionBorder(g2d, 15, 15, 20, 20, false);
        g2d.dispose();
        // Check some pixels in both regions are not white
        assertNotEquals(Color.WHITE.getRGB(), img.getRGB(6, 6), "Selection border (white) should draw border");
        assertNotEquals(Color.WHITE.getRGB(), img.getRGB(16, 16), "Selection border (black) should draw border");
    }
}
