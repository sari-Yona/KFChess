package org.kamatech.chess;

import javax.swing.*;
import java.awt.*;
import org.kamatech.chess.api.IPieceFactory;
import org.kamatech.chess.api.IGraphicsFactory;
import org.kamatech.chess.api.IPhysicsFactory;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Main game class that orchestrates all game components
 * Manages board state, command processing, physics, and game flow
 */
public class Game {
    private static final boolean DEBUG = false; // Set to true for debug output

    private final Board board;
    private final Graphics graphics;
    private final Physics physics;
    private final GameLogger logger;
    private final Map<String, Piece> pieces;
    private final IPieceFactory pieceFactory;
    private final IGraphicsFactory graphicsFactory;
    private final IPhysicsFactory physicsFactory;
    private final JFrame frame;
    private boolean running;
    private long lastUpdateTime;
    private final Set<Integer> pressedKeys;
    private String selectedPieceWhite; // Selected piece for white player
    private String selectedPieceBlack; // Selected piece for black player
    private String hoveredPieceWhite; // Piece currently being hovered by white player
    private String hoveredPieceBlack; // Piece currently being hovered by black player
    private boolean whiteInMovementMode = false; // Whether white player is in movement mode
    private boolean blackInMovementMode = false; // Whether black player is in movement mode
    // Pending moves storage
    private int whitePendingDx = 0;
    private int whitePendingDy = 0;
    private int blackPendingDx = 0;
    private int blackPendingDy = 0;

    private static final long UPDATE_INTERVAL_MS = 33; // ~30 FPS for slower updates

    public Game(Board board, IPieceFactory pieceFactory, IGraphicsFactory graphicsFactory,
            IPhysicsFactory physicsFactory) {
        this.board = board;
        this.pieces = new HashMap<>();
        this.pieceFactory = pieceFactory;
        this.graphicsFactory = graphicsFactory;
        this.physicsFactory = physicsFactory;
        this.graphics = graphicsFactory.createGraphics("", "");
        this.physics = physicsFactory.createPhysics("", null);
        this.logger = new GameLogger();
        this.running = false;
        this.lastUpdateTime = System.currentTimeMillis();
        this.pressedKeys = new HashSet<>();
        this.selectedPieceWhite = null;
        this.selectedPieceBlack = null;
        this.hoveredPieceWhite = null;
        this.hoveredPieceBlack = null;

        // Create and setup the window
        this.frame = new JFrame("Chess Game");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(800, 800);
        this.frame.setLocationRelativeTo(null);
        this.frame.addKeyListener(new InputHandler(this));
        this.frame.setFocusable(true);

        this.frame.add(new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Use GraphicsFactory to draw everything
                GraphicsFactory.drawGameBoard(g2d, board, pieces,
                        hoveredPieceWhite, hoveredPieceBlack,
                        selectedPieceWhite, selectedPieceBlack,
                        getWidth(), getHeight());
            }
        });

        initializeGame();
    }

    /**
     * Initialize the game by loading pieces and setting up the board
     */
    private void initializeGame() {
        try {
            // Use PieceFactory to create pieces with real configurations
            loadPiecesFromBoardCsv();

            logger.logCommand(Command.createGameControl("GAME_INITIALIZED"));

            // Initialize hover to first piece of each player
            List<String> whitePieces = Command.getPlayerPieces(Command.Player.WHITE, pieces);
            List<String> blackPieces = Command.getPlayerPieces(Command.Player.BLACK, pieces);
            if (!whitePieces.isEmpty()) {
                hoveredPieceWhite = whitePieces.get(0);
            }
            if (!blackPieces.isEmpty()) {
                hoveredPieceBlack = blackPieces.get(0);
            }

        } catch (Exception e) {
            // Fall back to default pieces
            pieces.putAll(pieceFactory.createDefaultPieces());
        }
    }

    /**
     * Load pieces from board.csv using PieceFactory
     */
    private void loadPiecesFromBoardCsv() throws Exception {
        String boardCsvPath = "c:\\הנדסאים\\CTD25\\pieces\\board.csv";
        File boardFile = new File(boardCsvPath);

        if (!boardFile.exists()) {
            pieces.putAll(pieceFactory.createDefaultPieces());
            return;
        }

        try {
            pieces.putAll(pieceFactory.createPiecesFromBoardCsv());
        } catch (Exception e) {
            System.err.println("Error loading from board.csv: " + e.getMessage());
            pieces.putAll(pieceFactory.createDefaultPieces());
        }
    }

    /**
     * Start the game loop
     */
    public void startGame() {
        frame.setVisible(true);
        running = true;
        logger.logCommand(Command.createGameControl("GAME_STARTED"));

        // Auto-select first piece for each player
        autoSelectFirstPieces();

        // Start game loop in separate thread
        Thread gameLoop = new Thread(this::gameLoop);
        gameLoop.setDaemon(true);
        gameLoop.start();
    }

    /**
     * Auto-select the first piece for each player
     */
    private void autoSelectFirstPieces() {
        // Find first white piece (contains W in the name)
        for (Map.Entry<String, Piece> entry : pieces.entrySet()) {
            String pieceId = entry.getKey();
            if (pieceId.contains("W")) {
                selectedPieceWhite = pieceId;
                entry.getValue().getState().setState(State.PieceState.IDLE);
                break;
            }
        }

        // Find first black piece (contains B in the name)
        for (Map.Entry<String, Piece> entry : pieces.entrySet()) {
            String pieceId = entry.getKey();
            if (pieceId.contains("B")) {
                selectedPieceBlack = pieceId;
                entry.getValue().getState().setState(State.PieceState.IDLE);
                break;
            }
        }
    }

    /**
     * Stop the game
     */
    public void stopGame() {
        running = false;
        logger.logCommand(Command.createGameControl("GAME_STOPPED"));
        logger.saveLogs();
        logger.printGameStats();
    }

    /**
     * Main game loop
     */
    private void gameLoop() {
        while (running) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastUpdateTime;

            if (deltaTime >= UPDATE_INTERVAL_MS) {
                update(deltaTime);
                render();
                lastUpdateTime = currentTime;
            }

            try {
                Thread.sleep(1); // Prevent 100% CPU usage
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Update game state
     */
    private void update(long deltaTimeMs) {
        // Update all piece states and animations
        for (Piece piece : pieces.values()) {
            State state = piece.getState();
            if (state != null) {
                state.update(); // Call without parameters for now
            }
        }

        // Check for game end conditions
        checkGameEndConditions();
    }

    /**
     * Render current frame
     */
    private void render() {
        // Just request a repaint of the frame
        frame.repaint();
    }

    /**
     * Check for game end conditions
     */
    private void checkGameEndConditions() {
        // Check if any king is captured - look for pieces that start with "KW" or "KB"
        boolean whiteKingExists = pieces.keySet().stream()
                .anyMatch(key -> key.startsWith("KW"));
        boolean blackKingExists = pieces.keySet().stream()
                .anyMatch(key -> key.startsWith("KB"));

        if (!whiteKingExists) {
            endGame(Command.Player.BLACK, "Black wins - White king captured!");
        } else if (!blackKingExists) {
            endGame(Command.Player.WHITE, "White wins - Black king captured!");
        }
    }

    /**
     * End the game with a winner
     */
    private void endGame(Command.Player winner, String reason) {
        running = false;
        logger.logCommand(Command.createGameControl("GAME_ENDED: " + reason));
        logger.saveLogs();
        // Display game over animation dialog
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame,
                reason + "\nWinner: " + winner,
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * Raw KeyListener entry point: preserves existing logic before full migration
     */
    public void handleRawKeyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        // Track pressed keys for movement hold detection
        pressedKeys.add(keyCode);

        // Handle jump via Shift keys
        if (keyCode == KeyEvent.VK_SHIFT) {
            int loc = e.getKeyLocation();
            if (loc == KeyEvent.KEY_LOCATION_LEFT && selectedPieceWhite != null) {
                // Left Shift: white jump
                executeCommand(Command.createJump(selectedPieceWhite, Command.Player.WHITE));
            } else if (loc == KeyEvent.KEY_LOCATION_RIGHT && selectedPieceBlack != null) {
                // Right Shift: black jump
                executeCommand(Command.createJump(selectedPieceBlack, Command.Player.BLACK));
            }
            return;
        }
        // Handle special keys
        switch (keyCode) {
            case KeyEvent.VK_SPACE:
                // White player movement confirmation
                if (!whiteInMovementMode) {
                    whiteInMovementMode = true;
                    whitePendingDx = 0;
                    whitePendingDy = 0;
                    System.out.println("White player entered movement mode");
                } else {
                    // Execute accumulated move if exists
                    if ((whitePendingDx != 0 || whitePendingDy != 0) && selectedPieceWhite != null) {
                        Piece piece = pieces.get(selectedPieceWhite);

                        // Create move command for validation and execution
                        Command moveCommand = Command.createKeyInput("MOVE", Command.Player.WHITE);
                        executeCommand(moveCommand);

                        // Set to REST state after move
                        piece.getState().setState(State.PieceState.REST);
                    }
                    // Exit movement mode
                    whiteInMovementMode = false;
                    whitePendingDx = 0;
                    whitePendingDy = 0;
                }
                break;

            case KeyEvent.VK_ENTER:
                // Black player movement confirmation
                if (!blackInMovementMode) {
                    blackInMovementMode = true;
                    blackPendingDx = 0;
                    blackPendingDy = 0;
                    System.out.println("Black player entered movement mode");
                } else {
                    // Execute accumulated move if exists
                    if ((blackPendingDx != 0 || blackPendingDy != 0) && selectedPieceBlack != null) {
                        Piece piece = pieces.get(selectedPieceBlack);

                        // Create move command for validation and execution
                        Command moveCommand = Command.createKeyInput("MOVE", Command.Player.BLACK);
                        executeCommand(moveCommand);

                        // Set to REST state after move
                        piece.getState().setState(State.PieceState.REST);
                    }
                    // Exit movement mode
                    blackInMovementMode = false;
                    blackPendingDx = 0;
                    blackPendingDy = 0;
                }
                break;
            case KeyEvent.VK_ESCAPE:
                // Exit movement modes
                whiteInMovementMode = false;
                blackInMovementMode = false;
                Command command = Command.createGameControl("END_GAME");
                executeCommand(command);
                break;

            // WHITE PLAYER Controls (WASD)
            case KeyEvent.VK_W:
                if (!whiteInMovementMode) {
                    selectPieceWithDirection(Command.Player.WHITE, "UP");
                } else {
                    whitePendingDy--;
                }
                break;
            case KeyEvent.VK_S:
                if (!whiteInMovementMode) {
                    selectPieceWithDirection(Command.Player.WHITE, "DOWN");
                } else {
                    whitePendingDy++;
                    System.out.println("White player pending move: dx=" + whitePendingDx + ", dy=" + whitePendingDy);
                }
                break;
            case KeyEvent.VK_A:
                if (!whiteInMovementMode) {
                    selectPieceWithDirection(Command.Player.WHITE, "LEFT");
                } else {
                    whitePendingDx--;
                    System.out.println("White player pending move: dx=" + whitePendingDx + ", dy=" + whitePendingDy);
                }
                break;
            case KeyEvent.VK_D:
                if (!whiteInMovementMode) {
                    selectPieceWithDirection(Command.Player.WHITE, "RIGHT");
                } else {
                    whitePendingDx++;
                    System.out.println("White player pending move: dx=" + whitePendingDx + ", dy=" + whitePendingDy);
                }
                break;

            // BLACK PLAYER Controls (Arrow Keys)
            case KeyEvent.VK_UP:
                if (!blackInMovementMode) {
                    selectPieceWithDirection(Command.Player.BLACK, "UP");
                } else {
                    blackPendingDy--;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (!blackInMovementMode) {
                    selectPieceWithDirection(Command.Player.BLACK, "DOWN");
                } else {
                    blackPendingDy++;
                    System.out.println("Black player pending move: dx=" + blackPendingDx + ", dy=" + blackPendingDy);
                }
                break;
            case KeyEvent.VK_LEFT:
                if (!blackInMovementMode) {
                    selectPieceWithDirection(Command.Player.BLACK, "LEFT");
                } else {
                    blackPendingDx--;
                    System.out.println("Black player pending move: dx=" + blackPendingDx + ", dy=" + blackPendingDy);
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (!blackInMovementMode) {
                    selectPieceWithDirection(Command.Player.BLACK, "RIGHT");
                } else {
                    blackPendingDx++;
                    System.out.println("Black player pending move: dx=" + blackPendingDx + ", dy=" + blackPendingDy);
                }
                break;
        }

        // Handle hover keys using Command utility methods
        if (Command.isWhiteHoverKey(keyCode)) {
            String direction = Command.getHoverDirection(keyCode);
            hoverPieceWithDirection(Command.Player.WHITE, direction);
        } else if (Command.isBlackHoverKey(keyCode)) {
            String direction = Command.getHoverDirection(keyCode);
            hoverPieceWithDirection(Command.Player.BLACK, direction);
        }

        // Continue with remaining cases
        switch (keyCode) {
            // Hover to Selection conversion
            case KeyEvent.VK_V:
                // White player: Convert current hover to selection
                if (hoveredPieceWhite != null) {
                    selectedPieceWhite = hoveredPieceWhite;
                    System.out.println("White selected from hover: " + selectedPieceWhite);
                    Command selectCommand = Command.createGameControl("SELECT_FROM_HOVER:" + selectedPieceWhite);
                    logger.logCommand(selectCommand);
                }
                break;
            case KeyEvent.VK_M:
                // Black player: Convert current hover to selection
                if (hoveredPieceBlack != null) {
                    selectedPieceBlack = hoveredPieceBlack;
                    System.out.println("Black selected from hover: " + selectedPieceBlack);
                    Command selectCommand = Command.createGameControl("SELECT_FROM_HOVER:" + selectedPieceBlack);
                    logger.logCommand(selectCommand);
                }
                break;
        }

        // Handle number keys for piece selection using Command utility methods
        if (Command.isNumberKey(keyCode)) {
            int pieceIndex = Command.numberKeyToIndex(keyCode);
            Command.Player player = e.isShiftDown() ? Command.Player.BLACK : Command.Player.WHITE;
            selectPieceByNumber(pieceIndex, player);
        }
    }

    /**
     * Temporary delegate for InputHandler compatibility
     */
    public void keyPressed(KeyEvent e) {
        handleRawKeyPressed(e);
    }

    /**
     * Execute a command through the command system
     */
    private void executeCommand(Command command) {
        if (command == null)
            return;

        // Log the command
        logger.logCommand(command);

        // Process the command based on its type
        switch (command.getCommandType()) {
            case GAME_CONTROL:
                handleGameControlCommand(command);
                break;
            case KEY_INPUT:
                handleMovementCommand(command);
                break;
            case MOVE:
                handleMoveCommand(command);
                break;
            case JUMP:
                handleJumpCommand(command);
                break;
            default:
        }
    }

    /**
     * Public entry to process commands from external executors
     */
    public void processCommand(Command command) {
        executeCommand(command);
    }

    /**
     * Handle movement commands (WASD/arrows) - Move the selected piece step by step
     */
    private void handleMovementCommand(Command command) {
        // Get selected piece by player
        Command.Player player = command.getPlayer();
        Piece piece = (player == Command.Player.WHITE)
                ? pieces.get(selectedPieceWhite)
                : pieces.get(selectedPieceBlack);

        if (piece == null) {
            return;
        }

        if (!piece.getState().canPerformAction()) {
            return;
        }

        // Use accumulated movement values
        int dx = (player == Command.Player.WHITE) ? whitePendingDx : blackPendingDx;
        int dy = (player == Command.Player.WHITE) ? whitePendingDy : blackPendingDy;


        // Move the piece step by step
        movePieceStepByStep(piece, dx, dy);

        // Force repaint to see the change immediately
        frame.repaint();
    }

    /**
     * Handle move commands
     */
    private void handleMoveCommand(Command command) {
        // This would handle more complex moves like chess notation
    }

    /**
     * Handle jump commands: move piece by pending deltas, capture if landing on
     * enemy
     */
    private void handleJumpCommand(Command command) {
        String pieceId = command.getPieceId();
        if (!pieces.containsKey(pieceId))
            return;
        Piece piece = pieces.get(pieceId);
        // Determine pending jump deltas and reset
        int dx = (command.getPlayer() == Command.Player.WHITE) ? whitePendingDx : blackPendingDx;
        int dy = (command.getPlayer() == Command.Player.WHITE) ? whitePendingDy : blackPendingDy;
        if (command.getPlayer() == Command.Player.WHITE) {
            whitePendingDx = 0;
            whitePendingDy = 0;
        } else {
            blackPendingDx = 0;
            blackPendingDy = 0;
        }
        // Calculate landing position
        double nextX = piece.getX() + dx;
        double nextY = piece.getY() + dy;
        // Check for enemy at landing
        Piece target = findPieceAt(nextX, nextY);
        if (target != null && target.isWhite() != piece.isWhite()) {
            handleCollision(piece, target);
        } else {
            // Move piece to landing
            piece.setPosition(nextX, nextY);
        }
        // Set jump state
        piece.getState().setState(State.PieceState.JUMP);
        System.out.println("Piece " + pieceId + " jumped to (" + nextX + "," + nextY + ")");
    }

    /**
     * Select next piece for a player using direction keys
     */
    private void selectPieceWithDirection(Command.Player player, String direction) {
        List<String> playerPieces = Command.getPlayerPieces(player, pieces);
        if (playerPieces.isEmpty())
            return;

        String currentSelected = (player == Command.Player.WHITE) ? selectedPieceWhite : selectedPieceBlack;
        int currentIndex = playerPieces.indexOf(currentSelected);

        // If no piece selected or piece not found, start from beginning
        if (currentIndex == -1) {
            currentIndex = 0;
        } else {
            // Cycle through pieces based on direction
            switch (direction) {
                case "UP":
                case "LEFT":
                    currentIndex = (currentIndex - 1 + playerPieces.size()) % playerPieces.size();
                    break;
                case "DOWN":
                case "RIGHT":
                    currentIndex = (currentIndex + 1) % playerPieces.size();
                    break;
            }
        }

        String newSelectedPiece = playerPieces.get(currentIndex);

        if (player == Command.Player.WHITE) {
            hoveredPieceWhite = newSelectedPiece; // Update hovered piece first
            selectedPieceWhite = newSelectedPiece;
            System.out.println("White hovering: " + hoveredPieceWhite + ", selected: " + selectedPieceWhite);
        } else {
            hoveredPieceBlack = newSelectedPiece; // Update hovered piece first
            selectedPieceBlack = newSelectedPiece;
            System.out.println("Black hovering: " + hoveredPieceBlack + ", selected: " + selectedPieceBlack);
        }

        // Log the selection
        Command selectCommand = Command.createGameControl("SELECT_PIECE:" + newSelectedPiece);
        logger.logCommand(selectCommand);
    }

    /**
     * Move hover between pieces without selecting
     */
    private void hoverPieceWithDirection(Command.Player player, String direction) {
        List<String> playerPieces = Command.getPlayerPieces(player, pieces);
        if (playerPieces.isEmpty())
            return;

        String currentHovered = (player == Command.Player.WHITE) ? hoveredPieceWhite : hoveredPieceBlack;
        int currentIndex = playerPieces.indexOf(currentHovered);

        // If no piece hovered or piece not found, start from beginning
        if (currentIndex == -1) {
            currentIndex = 0;
        } else {
            // Cycle through pieces based on direction
            switch (direction) {
                case "UP":
                case "LEFT":
                    currentIndex = (currentIndex - 1 + playerPieces.size()) % playerPieces.size();
                    break;
                case "DOWN":
                case "RIGHT":
                    currentIndex = (currentIndex + 1) % playerPieces.size();
                    break;
            }
        }

        String newHoveredPiece = playerPieces.get(currentIndex);

        if (player == Command.Player.WHITE) {
            hoveredPieceWhite = newHoveredPiece;
            System.out.println("*** WHITE HOVER CHANGED TO: " + hoveredPieceWhite + " ***");
        } else {
            hoveredPieceBlack = newHoveredPiece;
            System.out.println("*** BLACK HOVER CHANGED TO: " + hoveredPieceBlack + " ***");
        }
    }

    /**
     * Move a piece step by step based on user input
     */
    private void movePieceStepByStep(Piece piece, int dx, int dy) {
        double currentX = piece.getX();
        double currentY = piece.getY();

        // Calculate the next position
        double nextX = currentX + dx;
        double nextY = currentY + dy;

        System.out.println("DEBUG: Current position: (" + currentX + "," + currentY + ")");
        System.out.println("DEBUG: Next position: (" + nextX + "," + nextY + ")");
        System.out.println("DEBUG: Requested move: (" + dx + "," + dy + ")");

        // Check if this move is allowed by the piece's moves.txt file
        if (!isValidMoveForPiece(piece, dx, dy)) {
            System.out.println("DEBUG: Move blocked - not allowed by piece movement rules!");
            return;
        }

        // Check board boundaries
        if (nextX < 0 || nextX >= board.getWidthCells() ||
                nextY < 0 || nextY >= board.getHeightCells()) {
            System.out.println("DEBUG: Move blocked - out of bounds!");
            return;
        }

        // Check for collisions with other pieces
        Piece collidingPiece = findPieceAt(nextX, nextY);
        if (collidingPiece != null && !collidingPiece.equals(piece)) {
            System.out.println("DEBUG: Collision detected with " + collidingPiece.getId());

            // Check if this is a valid capture (different colors)
            if (piece.isWhite() != collidingPiece.isWhite()) {
                System.out.println("DEBUG: Valid capture! " + piece.getId() + " can capture " + collidingPiece.getId());
                handleCollision(piece, collidingPiece);
                System.out.println("DEBUG: Capture completed!");
            } else {
                System.out.println("DEBUG: Same color collision - move blocked!");
                return;
            }
        } else {
            // No collision - animated move
            // Set piece to MOVE state for animation
            piece.getState().setState(State.PieceState.MOVE);

            // Create animation thread
            new Thread(() -> {
                try {
                    double progress = 0;
                    while (progress < 1.0) {
                        // Calculate interpolated position
                        double interpX = piece.getX() + (nextX - piece.getX()) * progress;
                        double interpY = piece.getY() + (nextY - piece.getY()) * progress;
                        piece.setPosition(interpX, interpY);

                        // Update progress
                        progress += 0.05; // Slower progress for smoother, slower animation
                        Thread.sleep(100); // Slower animation timing

                        // Request repaint
                        frame.repaint();
                    }

                    // Ensure final position is exact
                    piece.setPosition(nextX, nextY);

                    // Set back to REST state
                    piece.getState().setState(State.PieceState.REST);

                    // Final repaint
                    frame.repaint();

                } catch (InterruptedException e) {
                    // Handle interruption if needed
                    piece.setPosition(nextX, nextY);
                    piece.getState().setState(State.PieceState.REST);
                    frame.repaint();
                }
            }).start();

            System.out.println("DEBUG: Piece " + piece.getId() + " moving to (" + nextX + "," + nextY + ")");
        }

        // No need for immediate repaint here as animation thread handles it
    }

    /**
     * Check if a move is valid for a piece based on its moves.txt file
     */
    private boolean isValidMoveForPiece(Piece piece, int dx, int dy) {
        // Get the piece's moves from its state
        State state = piece.getState();
        if (state == null || state.getMoves() == null) {
            System.out.println("DEBUG: No moves defined for piece " + piece.getId() + ", allowing all moves");
            return true; // If no moves defined, allow all moves
        }

        Moves moves = state.getMoves();
        List<String> movesList = moves.getAllowedMoves();

        if (movesList == null || movesList.isEmpty()) {
            System.out.println("DEBUG: Empty moves list for piece " + piece.getId() + ", allowing all moves");
            return true; // If moves list is empty, allow all moves
        }

        System.out.println("DEBUG: Checking moves for piece " + piece.getId() + ": " + movesList);

        // Check if the requested move matches any of the allowed moves
        for (String moveStr : movesList) {
            if (moveStr.trim().isEmpty())
                continue;

            try {
                // Parse move string (format: "dx,dy")
                String[] parts = moveStr.split(",");
                if (parts.length >= 2) {
                    int allowedDx = Integer.parseInt(parts[0].trim());
                    int allowedDy = Integer.parseInt(parts[1].trim());

                    // Check if this move matches the requested move
                    if (allowedDx == dx && allowedDy == dy) {
                        System.out.println("DEBUG: Move (" + dx + "," + dy + ") is allowed for " + piece.getId());
                        return true;
                    }

                    // Also check negative direction (for bidirectional moves)
                    if (allowedDx == -dx && allowedDy == -dy) {
                        System.out.println("DEBUG: Move (" + dx + "," + dy + ") is allowed (reverse direction) for "
                                + piece.getId());
                        return true;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("DEBUG: Invalid move format in moves.txt: " + moveStr);
            }
        }

        System.out.println("DEBUG: Move (" + dx + "," + dy + ") is NOT allowed for " + piece.getId());
        System.out.println("DEBUG: Available moves: " + movesList);

        // Temporary fallback - allow basic moves for common pieces
        String pieceType = piece.getId().substring(0, 1);
        System.out.println("DEBUG: Piece type: " + pieceType + ", trying fallback moves");

        switch (pieceType) {
            case "P": // Pawn
                // Check if it's a forward move (dy = 1 for black, dy = -1 for white)
                boolean isWhitePawn = piece.getId().contains("W");
                int forwardDirection = isWhitePawn ? -1 : 1; // White moves up (-y), Black moves down (+y)

                // Allow diagonal captures
                if (Math.abs(dx) == 1 && dy == forwardDirection) {
                    // Only allow diagonal moves if there's an enemy piece to capture
                    double nextX = piece.getX() + dx;
                    double nextY = piece.getY() + dy;
                    Piece targetPiece = findPieceAt(nextX, nextY);
                    if (targetPiece != null && targetPiece.isWhite() != piece.isWhite()) {
                        System.out.println("DEBUG: Allowing pawn capture move");
                        return true;
                    }
                    return false;
                }

                // Allow forward moves
                if (dx == 0) {
                    // Check if this is the pawn's first move
                    boolean isStartingPosition = (isWhitePawn && piece.getY() == 6)
                            || (!isWhitePawn && piece.getY() == 1);

                    // One square forward is always allowed
                    if (dy == forwardDirection) {
                        System.out.println("DEBUG: Allowing regular pawn move");
                        return true;
                    }

                    // Two squares forward only on first move
                    if (isStartingPosition && dy == (forwardDirection * 2)) {
                        System.out.println("DEBUG: Allowing pawn's initial two-square move");
                        return true;
                    }
                }
                break;
            case "R": // Rook
                if ((dx == 0 && Math.abs(dy) == 1) || (Math.abs(dx) == 1 && dy == 0)) {
                    System.out.println("DEBUG: Allowing rook move as fallback");
                    return true;
                }
                break;
            case "N": // Knight
                if ((Math.abs(dx) == 2 && Math.abs(dy) == 1) || (Math.abs(dx) == 1 && Math.abs(dy) == 2)) {
                    System.out.println("DEBUG: Allowing knight move as fallback");
                    return true;
                }
                break;
            case "B": // Bishop
                if (Math.abs(dx) == 1 && Math.abs(dy) == 1) {
                    System.out.println("DEBUG: Allowing bishop move as fallback");
                    return true;
                }
                break;
            case "Q": // Queen
            case "K": // King
                if ((Math.abs(dx) <= 1 && Math.abs(dy) <= 1) && !(dx == 0 && dy == 0)) {
                    System.out.println("DEBUG: Allowing queen/king move as fallback");
                    return true;
                }
                break;
        }

        return false;
    }

    /**
     * Handle collision between two pieces
     */
    private void handleCollision(Piece movingPiece, Piece targetPiece) {
        // Check if it's a capture (different players)
        if (movingPiece.isWhite() != targetPiece.isWhite()) {
            // Find map keys for moving and target pieces
            String movingKey = null;
            String targetKey = null;
            for (Map.Entry<String, Piece> e : pieces.entrySet()) {
                if (e.getValue() == movingPiece)
                    movingKey = e.getKey();
                if (e.getValue() == targetPiece)
                    targetKey = e.getKey();
            }

            // Remove the captured piece
            if (targetKey != null) {
                pieces.remove(targetKey);
                if (DEBUG)
                    System.out.println("DEBUG: Removed captured piece " + targetKey);
            }

            // Move attacking piece to target position
            movingPiece.setPosition(targetPiece.getX(), targetPiece.getY());
            if (DEBUG)
                System.out.println("DEBUG: Moved piece " + movingKey + " to position " + targetPiece.getX() + ","
                        + targetPiece.getY());

            // Log capture using full keys
            Command.Player capturer = movingPiece.isWhite() ? Command.Player.WHITE : Command.Player.BLACK;
            String logKey = movingKey != null ? movingKey : movingPiece.getId();
            String logCaptured = targetKey != null ? targetKey : targetPiece.getId();
            Command captureCommand = Command.createMove(
                    logKey,
                    movingPiece.getX(),
                    movingPiece.getY(),
                    capturer);
            logger.logCapture(capturer, logCaptured, captureCommand);

            // Set piece to jump state (celebrating capture)
            movingPiece.getState().setState(State.PieceState.JUMP);

            System.out.println(String.format("%s captured %s!", logKey, logCaptured));

            // Check if game ended due to king capture
            checkGameEndConditions();
        }
    }

    /**
     * Find piece at specific coordinates
     */
    private Piece findPieceAt(double x, double y) {
        double tolerance = 0.1; // Small tolerance

        for (Piece piece : pieces.values()) {
            double distance = Math.sqrt(
                    Math.pow(piece.getX() - x, 2) +
                            Math.pow(piece.getY() - y, 2));
            if (distance <= tolerance) {
                return piece;
            }
        }
        return null;
    }

    /**
     * Select piece by number for a specific player (0-7 for player's pieces)
     */
    private void selectPieceByNumber(int index, Command.Player player) {
        java.util.List<String> playerPieces = pieces.keySet().stream()
                .filter(id -> {
                    return (player == Command.Player.WHITE && id.contains("W")) ||
                            (player == Command.Player.BLACK && id.contains("B"));
                })
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        if (index < playerPieces.size()) {
            selectPiece(playerPieces.get(index), player);
        }
    }

    /**
     * Select a piece by ID for a specific player
     */
    public void selectPiece(String pieceId, Command.Player player) {
        if (pieces.containsKey(pieceId)) {
            Piece piece = pieces.get(pieceId);

            // Check if piece belongs to the player
            boolean isPieceValid = (player == Command.Player.WHITE && pieceId.contains("W")) ||
                    (player == Command.Player.BLACK && pieceId.contains("B"));

            if (isPieceValid) {
                if (player == Command.Player.WHITE) {
                    selectedPieceWhite = pieceId;
                } else {
                    selectedPieceBlack = pieceId;
                }

                piece.getState().setState(State.PieceState.IDLE);
                Command selectCommand = Command.createGameControl("SELECT_PIECE:" + pieceId);
                logger.logCommand(selectCommand);
                System.out.println(player + " selected piece: " + pieceId);
            } else {
                System.out.println(player + " cannot select opponent's piece: " + pieceId);
            }
        }
    }

    /**
     * Handle game control commands
     */
    private void handleGameControlCommand(Command command) {
        String controlType = command.getKeyInput(); // For game control commands, we use keyInput field

        if (controlType.equals("END_GAME")) {
            stopGame();
        }
        // Add other game control handling...
    }

    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        // Don't automatically set pieces to REST - let the player decide when to stop
        // moving
    }

    public void keyTyped(KeyEvent e) {
        // Not used
    }

    public Board getBoard() {
        return board;
    }

    public org.kamatech.chess.Graphics getGraphics() {
        return graphics;
    }

    public Physics getPhysics() {
        return physics;
    }

    public GameLogger getLogger() {
        return logger;
    }

    public Map<String, Piece> getPieces() {
        return new HashMap<>(pieces);
    }

    public boolean isRunning() {
        return running;
    }

    public String getSelectedPieceWhite() {
        return selectedPieceWhite;
    }

    public String getSelectedPieceBlack() {
        return selectedPieceBlack;
    }

    public String getHoveredPieceWhite() {
        return hoveredPieceWhite;
    }

    public String getHoveredPieceBlack() {
        return hoveredPieceBlack;
    }

    @Override
    public Game clone() {
        Game cloned = new Game(board.clone(), pieceFactory, graphicsFactory, physicsFactory);
        for (Map.Entry<String, Piece> entry : pieces.entrySet()) {
            cloned.pieces.put(entry.getKey(), entry.getValue().clone());
        }
        return cloned;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create initial board image
                Img boardImg = new Img();

                // Try to load from project root first
                String boardPath = "c:\\הנדסאים\\CTD25\\board.png";
                try {
                    boardImg.read(boardPath, new Dimension(800, 800), true, null);
                } catch (Exception e) {
                    System.out.println("Could not load board from project root, creating default board");
                    // Create a simple colored board if image loading fails
                    java.awt.image.BufferedImage defaultBoard = new java.awt.image.BufferedImage(800, 800,
                            java.awt.image.BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g2d = defaultBoard.createGraphics();
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillRect(0, 0, 800, 800);

                    // Draw chess board pattern
                    for (int row = 0; row < 8; row++) {
                        for (int col = 0; col < 8; col++) {
                            if ((row + col) % 2 == 1) {
                                g2d.setColor(Color.DARK_GRAY);
                                g2d.fillRect(col * 100, row * 100, 100, 100);
                            }
                        }
                    }
                    g2d.dispose();

                    // Set the default board image
                    boardImg.setImage(defaultBoard);
                }

                // Create board with standard chess dimensions (8x8)
                Board board = new Board(
                        100, // cellHeightPixels
                        100, // cellWidthPixels
                        1, // cellHeightMeters
                        1, // cellWidthMeters
                        8, // widthCells (standard chess board)
                        8, // heightCells (standard chess board)
                        boardImg);

                // Create factories
                GraphicsFactory graphicsFactory = new GraphicsFactory();
                PhysicsFactory physicsFactory = new PhysicsFactory();
                PieceFactory pieceFactory = new PieceFactory(graphicsFactory, physicsFactory);

                // Create and start game
                Game game = new Game(board, pieceFactory, graphicsFactory, physicsFactory);
                game.startGame();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting game: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
