package org.kamatech.chess.listeners;

import org.kamatech.chess.events.*;
public class ScoreTracker implements EventListener<PieceCapturedEvent> {
    private int score = 0;

    @Override
    public void onEvent(PieceCapturedEvent event) {
        switch (event.pieceType) {
            case "Pawn" -> score += 1;
            case "Knight", "Bishop" -> score += 3;
            case "Rook" -> score += 5;
            case "Queen" -> score += 9;
        }
        System.out.println("Score updated: " + score);
    }

    public int getScore() {
        return score;
    }
}
