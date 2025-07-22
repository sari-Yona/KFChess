package org.kamatech.chess;

import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * Game logger for tracking moves, commands, and scoring
 * Maintains separate logs for each player and calculates scores
 */
public class GameLogger {
    private final List<Command> gameLog;
    private final Map<Command.Player, List<Command>> playerLogs;
    private final Map<Command.Player, Integer> playerScores;
    private final Map<String, Integer> pieceValues;
    private final String logDirectory;

    public GameLogger() {
        this("game_logs");
    }

    public GameLogger(String logDirectory) {
        this.logDirectory = logDirectory;
        this.gameLog = new ArrayList<>();
        this.playerLogs = new HashMap<>();
        this.playerScores = new HashMap<>();
        this.pieceValues = initializePieceValues();

        // Initialize player logs and scores
        for (Command.Player player : Command.Player.values()) {
            playerLogs.put(player, new ArrayList<>());
            playerScores.put(player, 0);
        }

        // Create log directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(logDirectory));
        } catch (Exception e) {
            System.err.println("Could not create log directory: " + e.getMessage());
        }
    }

    private Map<String, Integer> initializePieceValues() {
        Map<String, Integer> values = new HashMap<>();
        // Standard chess piece values
        values.put("P", 1); // Pawn
        values.put("N", 3); // Knight
        values.put("B", 3); // Bishop
        values.put("R", 5); // Rook
        values.put("Q", 9); // Queen
        values.put("K", 100); // King (game-ending value)
        return values;
    }

    /**
     * Log a command to the game history
     */
    public void logCommand(Command command) {
        gameLog.add(command);
        playerLogs.get(command.getPlayer()).add(command);

        System.out.println("LOGGED: " + command.toString());
    }

    /**
     * Log a piece capture and update scores
     */
    public void logCapture(Command.Player capturingPlayer, String capturedPieceId, Command command) {
        logCommand(command);

        // Extract piece type from ID (e.g., "KW" -> "K")
        String pieceType = capturedPieceId.substring(0, capturedPieceId.length() - 1);
        int points = pieceValues.getOrDefault(pieceType, 0);

        // Add points to capturing player
        playerScores.put(capturingPlayer, playerScores.get(capturingPlayer) + points);

        System.out.println(String.format("CAPTURE: %s captured %s (+%d points) - Total: %d",
                capturingPlayer, capturedPieceId, points, playerScores.get(capturingPlayer)));
    }

    /**
     * Get player's current score
     */
    public int getPlayerScore(Command.Player player) {
        return playerScores.getOrDefault(player, 0);
    }

    /**
     * Get all commands for a specific player
     */
    public List<Command> getPlayerLog(Command.Player player) {
        return new ArrayList<>(playerLogs.get(player));
    }

    /**
     * Get full game log
     */
    public List<Command> getGameLog() {
        return new ArrayList<>(gameLog);
    }

    /**
     * Get recent commands (last N commands)
     */
    public List<Command> getRecentCommands(int count) {
        int size = gameLog.size();
        int start = Math.max(0, size - count);
        return new ArrayList<>(gameLog.subList(start, size));
    }

    /**
     * Get commands within time range
     */
    public List<Command> getCommandsInTimeRange(long startTime, long endTime) {
        return gameLog.stream()
                .filter(cmd -> cmd.getTimestamp() >= startTime && cmd.getTimestamp() <= endTime)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Save logs to files
     */
    public void saveLogs() {
        try {
            // Save full game log
            saveLogToFile("full_game.log", gameLog);

            // Save separate player logs
            for (Command.Player player : Command.Player.values()) {
                if (!playerLogs.get(player).isEmpty()) {
                    String filename = player.name().toLowerCase() + "_player.log";
                    saveLogToFile(filename, playerLogs.get(player));
                }
            }

            // Save scores
            saveScoresToFile();

            System.out.println("Logs saved to " + logDirectory);

        } catch (Exception e) {
            System.err.println("Error saving logs: " + e.getMessage());
        }
    }

    private void saveLogToFile(String filename, List<Command> commands) throws IOException {
        Path filePath = Paths.get(logDirectory, filename);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            writer.println("# Game Log - " + new Date());
            writer.println("# Format: [timestamp] type: command");
            writer.println();

            for (Command command : commands) {
                writer.println(command.toString());
            }
        }
    }

    private void saveScoresToFile() throws IOException {
        Path filePath = Paths.get(logDirectory, "scores.txt");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            writer.println("# Game Scores - " + new Date());
            writer.println();

            for (Command.Player player : Command.Player.values()) {
                int score = playerScores.get(player);
                int moves = playerLogs.get(player).size();
                writer.println(String.format("%s: %d points (%d moves)",
                        player.name(), score, moves));
            }

            // Determine winner
            Command.Player winner = getWinner();
            if (winner != null) {
                writer.println();
                writer.println("WINNER: " + winner.name());
            }
        }
    }

    /**
     * Get the current winner based on scores
     */
    public Command.Player getWinner() {
        Command.Player winner = null;
        int maxScore = -1;

        for (Map.Entry<Command.Player, Integer> entry : playerScores.entrySet()) {
            if (entry.getKey() != Command.Player.SYSTEM && entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                winner = entry.getKey();
            }
        }

        return winner;
    }

    /**
     * Print current game statistics
     */
    public void printGameStats() {
        System.out.println("\n=== GAME STATISTICS ===");
        System.out.println("Total commands: " + gameLog.size());

        for (Command.Player player : Command.Player.values()) {
            if (player != Command.Player.SYSTEM) {
                int score = playerScores.get(player);
                int moves = playerLogs.get(player).size();
                System.out.println(String.format("%s: %d points (%d moves)",
                        player.name(), score, moves));
            }
        }

        Command.Player winner = getWinner();
        if (winner != null) {
            System.out.println("Current leader: " + winner.name());
        }
        System.out.println("=======================\n");
    }

    /**
     * Clear all logs (for new game)
     */
    public void clearLogs() {
        gameLog.clear();
        for (List<Command> log : playerLogs.values()) {
            log.clear();
        }
        for (Command.Player player : playerScores.keySet()) {
            playerScores.put(player, 0);
        }
        System.out.println("Game logs cleared for new game");
    }
}
