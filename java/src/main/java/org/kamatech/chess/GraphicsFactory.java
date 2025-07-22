package org.kamatech.chess;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

import org.kamatech.chess.api.IGraphicsFactory;
public class GraphicsFactory implements IGraphicsFactory {
    // Sprite cache and timing for animations (moved from Game.java)
    private static final Map<String, List<BufferedImage>> spriteCache = new HashMap<>();
    private static final Map<String, Long> stateEnterTime = new HashMap<>();

    /**
     * Create a Graphics handler for a Piece instance.
     */
    public Graphics createGraphics(Piece piece) {
        return new Graphics(piece);
    }

    /**
     * @deprecated Legacy method, cannot infer Piece context.
     */
    @Deprecated
    public Graphics createGraphics(String pieceType, String state) {
        // Legacy support: produce no-op graphics
        return new Graphics(null);
    }

    /**
     * Get current sprite frame for a piece based on its state and elapsed time
     * This method was moved from Game.java to separate graphics concerns
     */
    public static BufferedImage getSpriteForPiece(Piece piece) {
        String pieceId = piece.getId();
        // Determine folder name based on state
        State.PieceState ps = piece.getState().getCurrentState();
        String stateName = mapStateToSpriteName(ps);
        String cacheKey = pieceId + "_" + stateName;

        List<BufferedImage> frames = spriteCache.get(cacheKey);
        if (frames == null) {
            frames = loadSprites(pieceId, stateName);
            spriteCache.put(cacheKey, frames);
            stateEnterTime.put(cacheKey, System.currentTimeMillis());
        }

        if (frames.isEmpty()) {
            return null;
        }

        long elapsed = System.currentTimeMillis() - stateEnterTime.get(cacheKey);
        long stateDuration;
        if (piece.getState().getCurrentState() == State.PieceState.REST) {
            stateDuration = 10000; // STANDARD_COOLDOWN_MS
        } else {
            stateDuration = 16; // UPDATE_INTERVAL_MS
        }
        double frameDuration = (double) stateDuration / frames.size();
        int index = (int) ((elapsed / frameDuration) % frames.size());
        return frames.get(index);
    }

    /**
     * Map piece state to sprite folder name
     */
    private static String mapStateToSpriteName(State.PieceState state) {
        switch (state) {
            case REST:
                return "long_rest";
            case SHORT_REST:
                return "short_rest";
            case EXHAUST:
                return "exhaust";
            default:
                return state.name().toLowerCase();
        }
    }

    /**
     * Load all sprite frames for a given piece state
     * This method was moved from Game.java to separate graphics concerns
     */
    private static List<BufferedImage> loadSprites(String pieceId, String stateFolder) {
        List<BufferedImage> frames = new ArrayList<>();
        String basePath = "c:\\הנדסאים\\CTD25\\pieces"
                + File.separator + pieceId
                + File.separator + "states"
                + File.separator + stateFolder
                + File.separator + "sprites";
        File dir = new File(basePath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null) {
                Arrays.sort(files);
                for (File f : files) {
                    try {
                        frames.add(ImageIO.read(f));
                    } catch (IOException e) {
                        // skip invalid frame
                    }
                }
            }
        }
        return frames;
    }

    /**
     * Draw a piece with fallback graphics if sprite loading fails
     * This method was moved from Game.java to separate graphics concerns
     */
    public static void drawPieceFallback(Graphics2D g2d, Piece piece, int x, int y, int cellWidth, int cellHeight) {
        String pieceId = piece.getId();
        Color pieceColor = piece.isWhite() ? Color.WHITE : Color.BLACK;

        // Set piece color based on type
        if (pieceId.startsWith("K"))
            pieceColor = piece.isWhite() ? Color.YELLOW : Color.ORANGE;
        else if (pieceId.startsWith("Q"))
            pieceColor = piece.isWhite() ? Color.PINK : Color.MAGENTA;
        else if (pieceId.startsWith("R"))
            pieceColor = piece.isWhite() ? Color.CYAN : Color.BLUE;
        else if (pieceId.startsWith("B"))
            pieceColor = piece.isWhite() ? Color.GREEN : Color.DARK_GRAY;
        else if (pieceId.startsWith("N"))
            pieceColor = piece.isWhite() ? Color.LIGHT_GRAY : Color.GRAY;
        else if (pieceId.startsWith("P"))
            pieceColor = piece.isWhite() ? Color.WHITE : Color.BLACK;

        g2d.setColor(pieceColor);
        g2d.fillOval(x + 10, y + 10, cellWidth - 20, cellHeight - 20);

        // Draw piece border
        g2d.setColor(Color.RED);
        g2d.drawOval(x + 10, y + 10, cellWidth - 20, cellHeight - 20);

        // Draw piece ID
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String displayId = pieceId.length() > 2 ? pieceId.substring(0, 2) : pieceId;
        g2d.drawString(displayId, x + cellWidth / 2 - 10, y + cellHeight / 2 + 5);
    }

    /**
     * Draw hover effects for a piece
     * This method was moved from Game.java to separate graphics concerns
     */
    public static void drawHoverEffect(Graphics2D g2d, int x, int y, int cellWidth, int cellHeight) {
        // Draw THICK ORANGE BORDER around the piece (over the sprite)
        g2d.setColor(new Color(255, 140, 0)); // Orange color
        g2d.setStroke(new BasicStroke(8.0f)); // Very thick border
        g2d.drawRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4);

        // Draw SECOND border for even more visibility
        g2d.setColor(new Color(255, 200, 0)); // Bright yellow-orange
        g2d.setStroke(new BasicStroke(4.0f));
        g2d.drawRect(x + 6, y + 6, cellWidth - 12, cellHeight - 12);

        // Reset stroke
        g2d.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Draw selection border for a piece
     * This method was moved from Game.java to separate graphics concerns
     */
    public static void drawSelectionBorder(Graphics2D g2d, int x, int y, int cellWidth, int cellHeight,
            boolean isWhite) {
        if (isWhite) {
            g2d.setColor(Color.CYAN);
            g2d.setStroke(new BasicStroke(6));
            g2d.drawRect(x + 1, y + 1, cellWidth - 2, cellHeight - 2);
        } else {
            g2d.setColor(new Color(255, 100, 0)); // Different orange for selection
            g2d.setStroke(new BasicStroke(6));
            g2d.drawRect(x + 3, y + 3, cellWidth - 6, cellHeight - 6);
        }
        g2d.setStroke(new BasicStroke(1)); // Reset stroke
    }

    /**
     * Draw the complete board with all pieces
     * This method was moved from Game.java to separate graphics concerns
     */
    public static void drawGameBoard(Graphics2D g2d, Board board, Map<String, Piece> pieces,
            String hoveredPieceWhite, String hoveredPieceBlack,
            String selectedPieceWhite, String selectedPieceBlack,
            int panelWidth, int panelHeight) {
        // Draw board background
        g2d.drawImage(board.getImage().getImage(), 0, 0, panelWidth, panelHeight, null);

        // Calculate cell dimensions
        int cellWidth = panelWidth / board.getWidthCells();
        int cellHeight = panelHeight / board.getHeightCells();

        // Draw debug info
        drawDebugInfo(g2d, hoveredPieceWhite, hoveredPieceBlack);

        // Draw all pieces
        drawAllPieces(g2d, pieces, cellWidth, cellHeight,
                hoveredPieceWhite, hoveredPieceBlack,
                selectedPieceWhite, selectedPieceBlack);
    }

    /**
     * Draw debug information on screen
     */
    private static void drawDebugInfo(Graphics2D g2d, String hoveredPieceWhite, String hoveredPieceBlack) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("White hover: " + hoveredPieceWhite, 10, 30);
        g2d.drawString("Black hover: " + hoveredPieceBlack, 10, 50);
        g2d.drawString("Press Q/E/Z/C to test white hover", 10, 70);
        g2d.drawString("Press U/O/J/L to test black hover", 10, 90);
    }

    /**
     * Draw all pieces with their sprites, hover effects, and selection borders
     */
    private static void drawAllPieces(Graphics2D g2d, Map<String, Piece> pieces, int cellWidth, int cellHeight,
            String hoveredPieceWhite, String hoveredPieceBlack,
            String selectedPieceWhite, String selectedPieceBlack) {
        // FIRST: Draw all pieces with sprites and hover effects
        for (Map.Entry<String, Piece> entry : pieces.entrySet()) {
            String key = entry.getKey();
            Piece piece = entry.getValue();

            // Calculate piece position in pixels
            int x = (int) (piece.getX() * cellWidth);
            int y = (int) (piece.getY() * cellHeight);

            // Check if this piece is hovered (but not if it's already selected)
            boolean isHovered = (key.equals(hoveredPieceWhite) && !key.equals(selectedPieceWhite))
                    || (key.equals(hoveredPieceBlack) && !key.equals(selectedPieceBlack));

            // Draw sprite
            BufferedImage spriteImage = getSpriteForPiece(piece);
            if (spriteImage != null) {
                // Draw real sprite image first
                g2d.drawImage(spriteImage, x + 5, y + 5, cellWidth - 10, cellHeight - 10, null);

                // THEN draw hover border OVER the sprite for maximum visibility
                if (isHovered) {
                    drawHoverEffect(g2d, x, y, cellWidth, cellHeight);
                }
            } else {
                // Fallback to colored circles if sprite loading fails
                drawPieceFallback(g2d, piece, x, y, cellWidth, cellHeight);
            }

            // Draw remaining rest time for any state with non-zero cooldown
            drawRemainingTime(g2d, piece, x, y, cellWidth, cellHeight);
        }

        // SECOND: Draw selection borders
        drawSelectionBorders(g2d, pieces, cellWidth, cellHeight, selectedPieceWhite, selectedPieceBlack);
    }

    /**
     * Draw remaining time for pieces in cooldown states
     */
    private static void drawRemainingTime(Graphics2D g2d, Piece piece, int x, int y, int cellWidth, int cellHeight) {
        long remMs = piece.getState().getRemainingStateTime();
        if (remMs > 0) {
            String remText = String.format("%.1f", remMs / 1000.0);
            g2d.setColor(new Color(0, 0, 255, 200));
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString(remText, x + cellWidth / 2 - 10, y + cellHeight / 2 + 5);
        }
    }

    /**
     * Draw selection borders for selected pieces
     */
    private static void drawSelectionBorders(Graphics2D g2d, Map<String, Piece> pieces, int cellWidth, int cellHeight,
            String selectedPieceWhite, String selectedPieceBlack) {
        for (Map.Entry<String, Piece> entry : pieces.entrySet()) {
            String key = entry.getKey();
            Piece piece = entry.getValue();
            int x = (int) (piece.getX() * cellWidth);
            int y = (int) (piece.getY() * cellHeight);

            // Draw selection borders
            if (key.equals(selectedPieceWhite)) {
                drawSelectionBorder(g2d, x, y, cellWidth, cellHeight, true);
            }
            if (key.equals(selectedPieceBlack)) {
                drawSelectionBorder(g2d, x, y, cellWidth, cellHeight, false);
            }
        }
    }
}
