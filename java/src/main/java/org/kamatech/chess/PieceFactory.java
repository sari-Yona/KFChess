package org.kamatech.chess;
import org.kamatech.chess.api.IPieceFactory;
import org.kamatech.chess.api.IGraphicsFactory;
import org.kamatech.chess.api.IPhysicsFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class PieceFactory implements IPieceFactory {
   private final IGraphicsFactory graphicsFactory;
   private final IPhysicsFactory physicsFactory;
    private Map<String, Piece> pieceTemplates;

   public PieceFactory(IGraphicsFactory graphicsFactory, IPhysicsFactory physicsFactory) {
      this.graphicsFactory = graphicsFactory;
      this.physicsFactory = physicsFactory;
        this.pieceTemplates = new HashMap<>();

        // Initialize basic piece templates
        initializePieceTemplates();
    }

    /**
     * Initialize piece templates from real piece directories
     */
    private void initializePieceTemplates() {
        String basePath = "c:\\הנדסאים\\CTD25\\pieces\\";

        // List of actual piece directories in the pieces folder
        String[] pieceDirectories = { "PB", "PW", "RB", "RW", "NB", "NW", "BB", "BW", "QB", "QW", "KB", "KW" };

        for (String pieceCode : pieceDirectories) {
            try {
                // Load moves from the piece's moves.txt file
                Moves moves = loadMovesFromFile(basePath + pieceCode + "\\moves.txt");

                // Create physics with real moves data
                Physics physics = physicsFactory.createPhysics(pieceCode, moves);

                // Load graphics configuration from piece directory
                String configPath = basePath + pieceCode + "\\states\\idle\\config.json";
                Graphics graphics = graphicsFactory.createGraphics(pieceCode, configPath);

                // Create state with moves
                State state = new State(moves, graphics, physics);

                // Determine if piece is white or black
                boolean isWhite = pieceCode.endsWith("W");

                // Create piece template
                Piece piece = new Piece(pieceCode, state, 0, 0, isWhite);
                pieceTemplates.put(pieceCode, piece);

                System.out.println("Loaded real piece template: " + pieceCode);

            } catch (Exception e) {
                System.err.println("Error loading piece " + pieceCode + ": " + e.getMessage());
                // Create fallback piece
                createFallbackPiece(pieceCode);
            }
        }

        System.out.println("Initialized " + pieceTemplates.size() + " piece templates from real data");
    }

    /**
     * Load moves from a moves.txt file
     */
    private Moves loadMovesFromFile(String movesFilePath) {
        List<String> movesList = new ArrayList<>();

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(movesFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) { // Skip comments
                    movesList.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load moves from " + movesFilePath + ": " + e.getMessage());
            // Use default moves based on piece type
            String pieceType = extractPieceType(movesFilePath);
            movesList = getDefaultMovesForPieceType(pieceType);
        }

        return new Moves(movesList, 1000); // 1 second cooldown
    }

    /**
     * Extract piece type from file path
     */
    private String extractPieceType(String filePath) {
        // Extract piece code from path like "pieces\PB\moves.txt"
        String[] parts = filePath.split("\\\\");
        for (String part : parts) {
            if (part.length() == 2 && (part.endsWith("B") || part.endsWith("W"))) {
                return part.substring(0, 1); // Return just the piece type (P, R, N, etc.)
            }
        }
        return "P"; // Default to pawn
    }

    /**
     * Create a fallback piece when real data loading fails
     */
    private void createFallbackPiece(String pieceCode) {
        List<String> moves = getDefaultMovesForPieceType(pieceCode.substring(0, 1));
        Moves movesObj = new Moves(moves, 1000);

        Physics physics = physicsFactory.createPhysics(pieceCode, movesObj);
        Graphics graphics = graphicsFactory.createGraphics(pieceCode, "");
        State state = new State(movesObj, graphics, physics);

        boolean isWhite = pieceCode.endsWith("W");
        Piece piece = new Piece(pieceCode, state, 0, 0, isWhite);

        pieceTemplates.put(pieceCode, piece);
        System.out.println("Created fallback piece: " + pieceCode);
    }

    /**
     * Get default moves for a piece type
     */
    private List<String> getDefaultMovesForPieceType(String type) {
        List<String> moves = new ArrayList<>();

        switch (type) {
            case "P": // Pawn
                moves.add("0,1");
                moves.add("0,2:first_move");
                break;
            case "R": // Rook
                for (int i = 1; i <= 7; i++) {
                    moves.add(i + ",0");
                    moves.add("-" + i + ",0");
                    moves.add("0," + i);
                    moves.add("0,-" + i);
                }
                break;
            case "N": // Knight
                moves.add("2,1");
                moves.add("2,-1");
                moves.add("-2,1");
                moves.add("-2,-1");
                moves.add("1,2");
                moves.add("1,-2");
                moves.add("-1,2");
                moves.add("-1,-2");
                break;
            case "B": // Bishop
                for (int i = 1; i <= 7; i++) {
                    moves.add(i + "," + i);
                    moves.add("-" + i + "," + i);
                    moves.add(i + ",-" + i);
                    moves.add("-" + i + ",-" + i);
                }
                break;
            case "Q": // Queen - combines rook and bishop
                for (int i = 1; i <= 7; i++) {
                    moves.add(i + ",0");
                    moves.add("-" + i + ",0");
                    moves.add("0," + i);
                    moves.add("0,-" + i);
                    moves.add(i + "," + i);
                    moves.add("-" + i + "," + i);
                    moves.add(i + ",-" + i);
                    moves.add("-" + i + ",-" + i);
                }
                break;
            case "K": // King
                moves.add("1,0");
                moves.add("-1,0");
                moves.add("0,1");
                moves.add("0,-1");
                moves.add("1,1");
                moves.add("-1,1");
                moves.add("1,-1");
                moves.add("-1,-1");
                break;
            default:
                moves.add("1,0"); // Default move
        }

        return moves;
    }

    public Piece createPiece(String pieceType, int x, int y) {
        // Try to get template
        Piece template = pieceTemplates.get(pieceType);
        if (template != null) {
            // Clone the template and set position
            try {
                Piece piece = new Piece(template.getId(), template.getState().clone(), x, y, template.isWhite());
                return piece;
            } catch (Exception e) {
                System.err.println("Error cloning piece template: " + e.getMessage());
            }
        }

        // Create basic piece if no template found
        List<String> defaultMoves = new ArrayList<>();
        defaultMoves.add("1,0");
        Moves moves = new Moves(defaultMoves, 1000);

        Physics physics = physicsFactory.createPhysics(pieceType, moves);
        Graphics graphics = graphicsFactory.createGraphics(pieceType, "");
        State state = new State(moves, graphics, physics);

        boolean isWhite = pieceType.endsWith("W");
        return new Piece(pieceType, state, x, y, isWhite);
    }

    /**
     * Create piece with custom cooldown based on game mode
     */
    public Piece createPieceWithCooldown(String pieceCode, int x, int y, long cooldownMs) {
        try {
            // Load moves for this piece type if available
            Moves moves = loadMovesWithCooldown(pieceCode, cooldownMs);

            // Create physics using PhysicsFactory
            Physics physics = physicsFactory.createPhysics(pieceCode, moves);

            // Create graphics using GraphicsFactory
            Graphics graphics = graphicsFactory.createGraphics(pieceCode, "");

            // Create state with piece-specific moves
            State state = new State(moves, graphics, physics);

            // Determine if piece is white or black
            boolean isWhite = pieceCode.endsWith("W");

            // Create piece
            Piece piece = new Piece(pieceCode, state, x, y, isWhite);

            return piece;

        } catch (Exception e) {
            System.err.println("Error creating piece " + pieceCode + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Load moves for a piece type from moves.txt, using custom cooldown
     */
    private Moves loadMovesWithCooldown(String pieceCode, long cooldownMs) {
        List<String> movesList = new ArrayList<>();
        String movesPath = "c:\\הנדסאים\\CTD25\\pieces\\" + pieceCode + "\\moves.txt";
        java.io.File movesFile = new java.io.File(movesPath);
        if (movesFile.exists()) {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(movesFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        movesList.add(line.trim());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading moves for " + pieceCode + ": " + e.getMessage());
            }
        } else {
            // default single-step move
            movesList.add("1,0");
        }
        return new Moves(movesList, cooldownMs);
    }

    /**
     * Create pieces from board.csv file
     */
    public Map<String, Piece> createPiecesFromBoardCsv() {
        Map<String, Piece> pieces = new HashMap<>();
        try {
            // Read board.csv file
            String boardCsvPath = "c:\\הנדסאים\\CTD25\\pieces\\board.csv";
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(boardCsvPath));

            int row = 0;
            String line;
            while ((line = reader.readLine()) != null && row < 8) {
                String[] cols = line.split(",");
                for (int col = 0; col < Math.min(cols.length, 8); col++) {
                    String pieceId = cols[col].trim();
                    if (!pieceId.isEmpty()) {
                        // Create piece with proper position using factory
                        Piece piece = createPiece(pieceId, col, row);
                        if (piece != null) {
                            pieces.put(pieceId + "_" + row + "_" + col, piece); // Unique ID for duplicate pieces
                            System.out.println("Created piece: " + pieceId + " at (" + col + ", " + row + ")");
                        }
                    }
                }
                row++;
            }
            reader.close();

            System.out.println("Loaded " + pieces.size() + " pieces from board.csv");

        } catch (Exception e) {
            System.err.println("Could not load from board.csv, creating default pieces: " + e.getMessage());
            pieces.putAll(createDefaultPieces());
        }
        return pieces;
    }

    /**
     * Create default pieces if board.csv loading fails
     */
    public Map<String, Piece> createDefaultPieces() {
        Map<String, Piece> pieces = new HashMap<>();

        // Create some test pieces
        String[] whiteIds = { "KW", "QW", "RW1", "RW2", "BW1", "BW2", "NW1", "NW2" };
        String[] blackIds = { "KB", "QB", "RB1", "RB2", "BB1", "BB2", "NB1", "NB2" };

        for (int i = 0; i < whiteIds.length; i++) {
            Piece whitePiece = createPiece(whiteIds[i], i, 7);
            if (whitePiece != null) {
                pieces.put(whiteIds[i], whitePiece);
            }

            Piece blackPiece = createPiece(blackIds[i], i, 0);
            if (blackPiece != null) {
                pieces.put(blackIds[i], blackPiece);
            }
        }

        return pieces;
    }
}
