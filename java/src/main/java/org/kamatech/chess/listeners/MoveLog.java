package org.kamatech.chess.listeners;

import org.kamatech.chess.events.*;


import java.util.ArrayList;
import java.util.List;

public class MoveLog implements EventListener<PieceMovedEvent> {
    private final List<String> moves = new ArrayList<>();

    @Override
    public void onEvent(PieceMovedEvent event) {
        String move = event.from + " → " + event.to;
        moves.add(move);
        System.out.println("Move added to log: " + move);
    }

    public List<String> getMoves() {
        return moves;
    }
}
