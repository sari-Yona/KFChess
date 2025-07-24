package org.kamatech.chess.listeners;

import org.kamatech.chess.events.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MoveTableListener implements EventListener<PieceMovedEvent> {
    private final JTable whiteTable;
    private final JTable blackTable;
    private final DefaultTableModel whiteModel;
    private final DefaultTableModel blackModel;
    private final JLabel whiteScoreLabel;
    private final JLabel blackScoreLabel;
    private int whiteScore = 0;
    private int blackScore = 0;
    private long gameStartTime;

    public MoveTableListener() {
        this.gameStartTime = System.currentTimeMillis();

        // Create table models
        String[] columns = { "Move #", "Time", "Piece", "From → To", "Capture" };
        this.whiteModel = new DefaultTableModel(columns, 0);
        this.blackModel = new DefaultTableModel(columns, 0);

        // Create tables
        this.whiteTable = new JTable(whiteModel);
        this.blackTable = new JTable(blackModel);

        // Create score labels
        this.whiteScoreLabel = new JLabel("Score: 0");
        this.blackScoreLabel = new JLabel("Score: 0");
        whiteScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        blackScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add visual styling to score labels
        whiteScoreLabel.setFont(whiteScoreLabel.getFont().deriveFont(Font.BOLD, 14f));
        blackScoreLabel.setFont(blackScoreLabel.getFont().deriveFont(Font.BOLD, 14f));
        whiteScoreLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        blackScoreLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        whiteScoreLabel.setOpaque(true);
        blackScoreLabel.setOpaque(true);
        whiteScoreLabel.setBackground(Color.LIGHT_GRAY);
        blackScoreLabel.setBackground(Color.LIGHT_GRAY);

        setupTables();
    }

    private void setupTables() {
        // Configure tables appearance
        whiteTable.setFillsViewportHeight(true);
        blackTable.setFillsViewportHeight(true);

        // Set column widths
        whiteTable.getColumnModel().getColumn(0).setPreferredWidth(50); // Move #
        whiteTable.getColumnModel().getColumn(1).setPreferredWidth(60); // Time
        whiteTable.getColumnModel().getColumn(2).setPreferredWidth(50); // Piece
        whiteTable.getColumnModel().getColumn(3).setPreferredWidth(100); // From → To
        whiteTable.getColumnModel().getColumn(4).setPreferredWidth(60); // Capture

        blackTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        blackTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        blackTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        blackTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        blackTable.getColumnModel().getColumn(4).setPreferredWidth(60);
    }

    /**
     * Get piece value for scoring
     */
    private int getPieceValue(String pieceType) {
        if (pieceType == null)
            return 0;

        switch (pieceType.toUpperCase()) {
            case "P":
                return 1; // Pawn (חייל)
            case "N":
                return 3; // Knight (פרש)
            case "B":
                return 3; // Bishop (רץ)
            case "R":
                return 5; // Rook (צריח)
            case "Q":
                return 9; // Queen (מלכה)
            case "K":
                return 0; // King (no score for capturing king - game ends)
            default:
                return 0;
        }
    }

    /**
     * Update score labels
     */
    private void updateScoreLabels() {
        whiteScoreLabel.setText("Score: " + whiteScore);
        blackScoreLabel.setText("Score: " + blackScore);
        System.out.println("DEBUG: Updated scores - White: " + whiteScore + ", Black: " + blackScore);
    }

    @Override
    public void onEvent(PieceMovedEvent event) {
        // Calculate elapsed time
        long elapsedMs = event.timestamp - gameStartTime;
        String timeStr = String.format("%02d:%02d",
                (elapsedMs / 60000) % 60,
                (elapsedMs / 1000) % 60);

        // Format capture info and update score
        String captureInfo = "";
        if (event.capturedPiece != null) {
            captureInfo = "x" + event.capturedPiece;
            int pieceValue = getPieceValue(event.capturedPiece);

            System.out.println("DEBUG: Capture detected! Player: " + event.player +
                    ", Captured piece: " + event.capturedPiece +
                    ", Value: " + pieceValue);

            // Add points to the capturing player
            if ("WHITE".equals(event.player)) {
                whiteScore += pieceValue;
            } else if ("BLACK".equals(event.player)) {
                blackScore += pieceValue;
            }

            updateScoreLabels();
        }

        // Create row data
        Object[] rowData = {
                event.moveNumber,
                timeStr,
                event.pieceType,
                event.from + " → " + event.to,
                captureInfo
        };

        // Add to appropriate table
        if ("WHITE".equals(event.player)) {
            whiteModel.addRow(rowData);
            // Auto-scroll to bottom
            SwingUtilities.invokeLater(() -> {
                int lastRow = whiteTable.getRowCount() - 1;
                whiteTable.scrollRectToVisible(whiteTable.getCellRect(lastRow, 0, true));
            });
        } else if ("BLACK".equals(event.player)) {
            blackModel.addRow(rowData);
            // Auto-scroll to bottom
            SwingUtilities.invokeLater(() -> {
                int lastRow = blackTable.getRowCount() - 1;
                blackTable.scrollRectToVisible(blackTable.getCellRect(lastRow, 0, true));
            });
        }
    }

    public JTable getWhiteTable() {
        return whiteTable;
    }

    public JTable getBlackTable() {
        return blackTable;
    }

    public JLabel getWhiteScoreLabel() {
        return whiteScoreLabel;
    }

    public JLabel getBlackScoreLabel() {
        return blackScoreLabel;
    }

    public void resetTables() {
        whiteModel.setRowCount(0);
        blackModel.setRowCount(0);
        whiteScore = 0;
        blackScore = 0;
        updateScoreLabels();
        gameStartTime = System.currentTimeMillis();
    }
}
