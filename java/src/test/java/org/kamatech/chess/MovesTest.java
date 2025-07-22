package org.kamatech.chess;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class MovesTest {

    @Test
    void testGettersAndClone() {
        Moves original = new Moves(Arrays.asList("1,0", "0,1"), 250L);
        assertEquals(2, original.getAllowedMoves().size());
        assertEquals(250L, original.getCooldown());

        Moves clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.getAllowedMoves(), clone.getAllowedMoves());
        assertEquals(original.getCooldown(), clone.getCooldown());
    }

    @Test
    void testHandlePawnMoveWithinBounds() {
        Piece p = new Piece("PW", new State(null, null, null));
        p.setPosition(4, 4);
        Moves.handlePawnMove(p, 1, 0);
        assertEquals(5.0, p.getX());
        assertEquals(4.0, p.getY());
        assertEquals(State.PieceState.REST, p.getState().getCurrentState());
    }

    @Test
    void testHandlePawnMoveOutOfBounds() {
        Piece p = new Piece("PW", new State(null, null, null));
        p.setPosition(7, 7);
        Moves.handlePawnMove(p, 1, 0);
        assertEquals(7.0, p.getX());
        assertEquals(7.0, p.getY());
    }

    @Test
    void testHandleKnightMove() {
        Piece p = new Piece("NW", new State(null, null, null));
        p.setPosition(3, 3);
        Moves.handleKnightMove(p, 2, 1);
        assertEquals(5.0, p.getX());
        assertEquals(4.0, p.getY());
    }

    @Test
    void testSlideMove() {
        Piece p = new Piece("RB", new State(null, null, null));
        p.setPosition(0, 0);
        Moves.slideMove(p, 0, 7);
        assertEquals(0.0, p.getX());
        assertEquals(7.0, p.getY());
    }

    @Test
    void testSlideMoveBetter() {
        Piece p = new Piece("BQ", new State(null, null, null));
        p.setPosition(2, 2);
        Moves.slideMoveBetter(p, -1, -1);
        assertEquals(1.0, p.getX());
        assertEquals(1.0, p.getY());
    }

    @Test
    void testIsPieceOwnedByPlayer() {
        Piece whitePiece = new Piece("PW", new State(null, null, null));
        Piece blackPiece = new Piece("PB", new State(null, null, null));
        assertTrue(Moves.isPieceOwnedByPlayer(whitePiece, Command.Player.WHITE));
        assertFalse(Moves.isPieceOwnedByPlayer(whitePiece, Command.Player.BLACK));
        assertTrue(Moves.isPieceOwnedByPlayer(blackPiece, Command.Player.BLACK));
        assertFalse(Moves.isPieceOwnedByPlayer(blackPiece, Command.Player.WHITE));
        assertTrue(Moves.isPieceOwnedByPlayer(blackPiece, Command.Player.SYSTEM));
    }
}
