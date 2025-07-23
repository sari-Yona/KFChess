package org.kamatech.chess;

/**
 * Represents a game command with support for textual format and player tracking
 * Format: [PLAYER][PIECE] [FROM]->[TO] or [KEY_COMMAND]
 * Examples: "WQ e2->e5", "WASD_UP", "ARROW_LEFT"
 * This class now also handles input processing
 */
public class Command {
    public enum CommandType {
        MOVE, // Piece movement
        JUMP, // Piece jump/dodge
        KEY_INPUT, // Keyboard input
        GAME_CONTROL // Game control (pause, reset, etc.)
    }

    public enum Player {
        WHITE, BLACK, SYSTEM
    }

    private final CommandType commandType;
    private final Player player;
    private final String pieceId;
    private final String fromPosition;
    private final String toPosition;
    private final String keyInput;
    private final long timestamp;
    private final String rawCommand;

    // Constructor for movement commands
    public Command(Player player, String pieceId, String fromPosition, String toPosition) {
        this.commandType = CommandType.MOVE;
        this.player = player;
        this.pieceId = pieceId;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.keyInput = null;
        this.timestamp = System.currentTimeMillis();
        this.rawCommand = formatMoveCommand();
    }

    // Constructor for jump commands
    public Command(Player player, String pieceId, String fromPosition, String toPosition, boolean isJump) {
        this.commandType = isJump ? CommandType.JUMP : CommandType.MOVE;
        this.player = player;
        this.pieceId = pieceId;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.keyInput = null;
        this.timestamp = System.currentTimeMillis();
        this.rawCommand = formatMoveCommand();
    }

    // Constructor for key input commands
    public Command(Player player, String keyInput) {
        this.commandType = CommandType.KEY_INPUT;
        this.player = player;
        this.pieceId = null;
        this.fromPosition = null;
        this.toPosition = null;
        this.keyInput = keyInput;
        this.timestamp = System.currentTimeMillis();
        this.rawCommand = formatKeyCommand();
    }

    // Constructor for parsing textual commands
    public Command(String textualCommand) {
        this.timestamp = System.currentTimeMillis();
        this.rawCommand = textualCommand.trim();

        // Parse the command
        CommandData parsed = parseTextualCommand(textualCommand);
        this.commandType = parsed.type;
        this.player = parsed.player;
        this.pieceId = parsed.pieceId;
        this.fromPosition = parsed.fromPosition;
        this.toPosition = parsed.toPosition;
        this.keyInput = parsed.keyInput;
    }

    // Legacy constructor for backward compatibility
    public Command(String type, long timestamp) {
        this.commandType = CommandType.GAME_CONTROL;
        this.player = Player.SYSTEM;
        this.pieceId = null;
        this.fromPosition = null;
        this.toPosition = null;
        this.keyInput = type;
        this.timestamp = timestamp;
        this.rawCommand = type;
    }

    // Getters
    public CommandType getCommandType() {
        return commandType;
    }

    public Player getPlayer() {
        return player;
    }

    public String getPieceId() {
        return pieceId;
    }

    public String getFromPosition() {
        return fromPosition;
    }

    public String getToPosition() {
        return toPosition;
    }

    public String getKeyInput() {
        return keyInput;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getRawCommand() {
        return rawCommand;
    }

    // Helper methods
    private String formatMoveCommand() {
        String playerPrefix = (player == Player.WHITE) ? "W" : "B";
        return playerPrefix + pieceId + " " + fromPosition + "->" + toPosition;
    }

    private String formatKeyCommand() {
        String playerPrefix = (player == Player.WHITE) ? "W" : "B";
        return playerPrefix + "_" + keyInput;
    }

    // Inner class for parsing results
    private static class CommandData {
        CommandType type;
        Player player;
        String pieceId;
        String fromPosition;
        String toPosition;
        String keyInput;
    }

    private CommandData parseTextualCommand(String command) {
        CommandData data = new CommandData();

        try {
            command = command.trim().toUpperCase();

            // Check for key input commands (W_SPACE, B_ARROW_UP, etc.)
            if (command.contains("_")) {
                data.type = CommandType.KEY_INPUT;
                if (command.startsWith("W")) {
                    data.player = Player.WHITE;
                    data.keyInput = command.substring(2); // Remove "W_"
                } else if (command.startsWith("B")) {
                    data.player = Player.BLACK;
                    data.keyInput = command.substring(2); // Remove "B_"
                } else {
                    data.player = Player.SYSTEM;
                    data.keyInput = command;
                }
                return data;
            }

            // Parse movement commands (WQ e2->e5)
            if (command.contains("->")) {
                data.type = CommandType.MOVE;

                // Extract player and piece
                char playerChar = command.charAt(0);
                data.player = (playerChar == 'W') ? Player.WHITE : Player.BLACK;

                // Find the piece ID (everything until space)
                int spaceIndex = command.indexOf(' ');
                if (spaceIndex > 1) {
                    data.pieceId = command.substring(1, spaceIndex);
                }

                // Extract positions
                String positions = command.substring(spaceIndex + 1);
                String[] parts = positions.split("->");
                if (parts.length == 2) {
                    data.fromPosition = parts[0].trim();
                    data.toPosition = parts[1].trim();
                }

                return data;
            }

            // Default to game control
            data.type = CommandType.GAME_CONTROL;
            data.player = Player.SYSTEM;
            data.keyInput = command;

        } catch (Exception e) {
            // If parsing fails, treat as system command
            data.type = CommandType.GAME_CONTROL;
            data.player = Player.SYSTEM;
            data.keyInput = command;
        }

        return data;
    }

    // Utility methods
    public boolean isMovement() {
        return commandType == CommandType.MOVE || commandType == CommandType.JUMP;
    }

    public boolean isKeyInput() {
        return commandType == CommandType.KEY_INPUT;
    }

    public boolean isWhitePlayer() {
        return player == Player.WHITE;
    }

    public boolean isBlackPlayer() {
        return player == Player.BLACK;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: %s", timestamp, commandType, rawCommand);
    }

    // Factory methods for common commands
    public static Command createMove(Player player, String pieceId, String from, String to) {
        return new Command(player, pieceId, from, to);
    }

    public static Command createJump(Player player, String pieceId, String from, String to) {
        return new Command(player, pieceId, from, to, true);
    }

    public static Command createKeyPress(Player player, String key) {
        return new Command(player, key);
    }

    public static Command parseCommand(String textualCommand) {
        return new Command(textualCommand);
    }

    // Simplified factory methods for game use
    public static Command createMove(String pieceId, double targetX, double targetY, Player player) {
        // Convert coordinates to chess notation for now
        String from = "a1"; // Placeholder
        String to = "a2"; // Placeholder
        return createMove(player, pieceId, from, to);
    }

    public static Command createJump(String pieceId, Player player) {
        String from = "a1"; // Placeholder
        String to = "a2"; // Placeholder
        return createJump(player, pieceId, from, to);
    }

    public static Command createKeyInput(String keyInput, Player player) {
        return createKeyPress(player, keyInput);
    }

    public static Command createGameControl(String controlAction) {
        return new Command(controlAction, System.currentTimeMillis());
    }

    // Compatibility getters for Game class
    public CommandType getType() {
        return commandType;
    }

    public String getCommandText() {
        return rawCommand;
    }

    public double getTargetX() {
        return 0.0; // Placeholder
    }

    public double getTargetY() {
        return 0.0; // Placeholder
    }

    /**
     * Input handling methods moved from Game.java to separate concerns
     */

    /**
     * Convert a key code to a direction string
     */
    public static String keyCodeToDirection(int keyCode) {
        switch (keyCode) {
            case 87: // KeyEvent.VK_W
            case 38: // KeyEvent.VK_UP
                return "UP";
            case 83: // KeyEvent.VK_S
            case 40: // KeyEvent.VK_DOWN
                return "DOWN";
            case 65: // KeyEvent.VK_A
            case 37: // KeyEvent.VK_LEFT
                return "LEFT";
            case 68: // KeyEvent.VK_D
            case 39: // KeyEvent.VK_RIGHT
                return "RIGHT";
            default:
                return null;
        }
    }

    /**
     * Check if a key code is a white player movement key
     */
    public static boolean isWhiteMovementKey(int keyCode) {
        return keyCode == 87 || keyCode == 83 || keyCode == 65 || keyCode == 68; // W,S,A,D
    }

    /**
     * Check if a key code is a black player movement key
     */
    public static boolean isBlackMovementKey(int keyCode) {
        return keyCode == 38 || keyCode == 40 || keyCode == 37 || keyCode == 39; // Arrow keys
    }

    /**
     * Check if a key code is a white player hover key
     */
    public static boolean isWhiteHoverKey(int keyCode) {
        return keyCode == 81 || keyCode == 69 || keyCode == 90 || keyCode == 67; // Q,E,Z,C
    }

    /**
     * Check if a key code is a black player hover key
     */
    public static boolean isBlackHoverKey(int keyCode) {
        return keyCode == 85 || keyCode == 79 || keyCode == 74 || keyCode == 76; // U,O,J,L
    }

    /**
     * Get hover direction from key code
     */
    public static String getHoverDirection(int keyCode) {
        switch (keyCode) {
            case 81: // Q
            case 85: // U
                return "UP";
            case 69: // E
            case 79: // O
                return "DOWN";
            case 90: // Z
            case 74: // J
                return "LEFT";
            case 67: // C
            case 76: // L
                return "RIGHT";
            default:
                return null;
        }
    }

    /**
     * Check if key code is a number key (1-8)
     */
    public static boolean isNumberKey(int keyCode) {
        return keyCode >= 49 && keyCode <= 56; // Keys 1-8
    }

    /**
     * Convert number key to piece index (0-7)
     */
    public static int numberKeyToIndex(int keyCode) {
        return keyCode - 49; // Convert 1-8 to 0-7
    }

    /**
     * Get list of piece IDs for a specific player
     */
    public static java.util.List<String> getPlayerPieces(Player player, java.util.Map<String, ?> pieces) {
        java.util.List<String> playerPieces = new java.util.ArrayList<>();
        String playerMarker = (player == Player.WHITE) ? "W" : "B";

        for (String pieceId : pieces.keySet()) {
            if (pieceId.contains(playerMarker)) {
                playerPieces.add(pieceId);
            }
        }

        // Sort for consistent ordering
        playerPieces.sort(String::compareTo);
        return playerPieces;
    }

    // Integrated validation - checks basic move legality
    public static boolean isValid(Command cmd, Board board) {
        if (!cmd.isMovement()) {
            return false;
        }
        String[] fromParts = cmd.getFromPosition().split(",");
        String[] toParts = cmd.getToPosition().split(",");
        if (fromParts.length != 2 || toParts.length != 2) {
            return false;
        }
        try {
            int fromRow = Integer.parseInt(fromParts[0].trim());
            int fromCol = Integer.parseInt(fromParts[1].trim());
            int toRow = Integer.parseInt(toParts[0].trim());
            int toCol = Integer.parseInt(toParts[1].trim());
            if (fromRow < 0 || fromRow >= board.getHeightCells() || fromCol < 0 || fromCol >= board.getWidthCells()) {
                return false;
            }
            if (toRow < 0 || toRow >= board.getHeightCells() || toCol < 0 || toCol >= board.getWidthCells()) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    // Integrated executor - delegates to Game
    public static void execute(Command cmd, Game game) {
        game.processCommand(cmd);
    }
}
