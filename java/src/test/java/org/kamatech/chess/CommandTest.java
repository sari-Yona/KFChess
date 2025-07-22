package org.kamatech.chess;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.KeyEvent;

public class CommandTest {

    @Test
    void testParseMove() {
        Command cmd = Command.parseCommand("WQ e2->e4");
        assertEquals(Command.CommandType.MOVE, cmd.getCommandType());
        assertEquals(Command.Player.WHITE, cmd.getPlayer());
        assertEquals("Q", cmd.getPieceId());
        // Positions are uppercased by parser
        assertEquals("E2", cmd.getFromPosition());
        assertEquals("E4", cmd.getToPosition());
        assertNull(cmd.getKeyInput());
    }

    @Test
    void testParseKeyInput() {
        Command cmd = Command.parseCommand("W_SPACE");
        assertEquals(Command.CommandType.KEY_INPUT, cmd.getCommandType());
        assertEquals(Command.Player.WHITE, cmd.getPlayer());
        assertNull(cmd.getPieceId());
        assertEquals("SPACE", cmd.getKeyInput());
    }

    @Test
    void testCreateGameControl() {
        Command cmd = Command.createGameControl("END_GAME");
        assertEquals(Command.CommandType.GAME_CONTROL, cmd.getCommandType());
        assertEquals("END_GAME", cmd.getKeyInput());
    }

    @Test
    void testKeyCodeToDirection() {
        assertEquals("UP", Command.keyCodeToDirection(KeyEvent.VK_W));
        assertEquals("DOWN", Command.keyCodeToDirection(KeyEvent.VK_S));
        assertEquals("LEFT", Command.keyCodeToDirection(KeyEvent.VK_A));
        assertEquals("RIGHT", Command.keyCodeToDirection(KeyEvent.VK_D));
        assertNull(Command.keyCodeToDirection(KeyEvent.VK_X));
    }

    @Test
    void testMovementKeyChecks() {
        assertTrue(Command.isWhiteMovementKey(KeyEvent.VK_W));
        assertTrue(Command.isWhiteMovementKey(KeyEvent.VK_A));
        assertFalse(Command.isWhiteMovementKey(KeyEvent.VK_UP));
        assertTrue(Command.isBlackMovementKey(KeyEvent.VK_UP));
        assertFalse(Command.isBlackMovementKey(KeyEvent.VK_W));
    }

    @Test
    void testHoverKeyChecksAndDirection() {
        // White hover keys
        assertTrue(Command.isWhiteHoverKey(KeyEvent.VK_Q));
        assertTrue(Command.isWhiteHoverKey(KeyEvent.VK_E));
        assertEquals("UP", Command.getHoverDirection(KeyEvent.VK_Q));
        assertEquals("DOWN", Command.getHoverDirection(KeyEvent.VK_E));
        assertEquals("LEFT", Command.getHoverDirection(KeyEvent.VK_Z));
        assertEquals("RIGHT", Command.getHoverDirection(KeyEvent.VK_C));

        // Black hover keys
        assertTrue(Command.isBlackHoverKey(KeyEvent.VK_U));
        assertEquals("UP", Command.getHoverDirection(KeyEvent.VK_U));
        assertEquals("DOWN", Command.getHoverDirection(KeyEvent.VK_O));
        assertFalse(Command.isBlackHoverKey(KeyEvent.VK_Q));
        assertNull(Command.getHoverDirection(0));
    }

    @Test
    void testMovementAndKeyInputFlags() {
        // MOVE command
        Command move = Command.createMove(Command.Player.BLACK, "P1", "a2", "a3");
        assertTrue(move.isMovement(), "Move command should be movement");
        assertFalse(move.isKeyInput(), "Move command should not be key input");
        assertTrue(move.isBlackPlayer(), "Move command player should be black");

        // JUMP command
        Command jump = Command.createJump(Command.Player.WHITE, "P2", "b1", "c3");
        assertTrue(jump.isMovement(), "Jump command should be movement");
        assertTrue(jump.isWhitePlayer(), "Jump command player should be white");

        // KEY_INPUT command
        Command key = Command.createKeyInput("ENTER", Command.Player.SYSTEM);
        assertFalse(key.isMovement(), "Key command should not be movement");
        assertTrue(key.isKeyInput(), "Key command should be key input");
        assertFalse(key.isWhitePlayer());
        assertFalse(key.isBlackPlayer());
    }

    @Test
    void testToStringFormat() {
        Command cmd = Command.createMove(Command.Player.WHITE, "Q", "c1", "c4");
        String s = cmd.toString();
        assertTrue(s.contains("MOVE"), "toString should include command type");
        assertTrue(s.contains("Q"), "toString should include pieceId");
        // Format: [timestamp] ...
        assertTrue(s.matches("^\\[\\d+\\].*"), "toString should start with [timestamp]");
    }

    @Test
    void testStaticFactoryMethods() {
        // createKeyPress and createKeyInput
        Command kp = Command.createKeyPress(Command.Player.WHITE, "SPACE");
        assertEquals(Command.CommandType.KEY_INPUT, kp.getCommandType());
        assertEquals("SPACE", kp.getKeyInput());

        Command ci = Command.createKeyInput("SPACE", Command.Player.BLACK);
        assertEquals(Command.CommandType.KEY_INPUT, ci.getCommandType());

        // createGameControl
        Command gc = Command.createGameControl("PAUSE");
        assertEquals(Command.CommandType.GAME_CONTROL, gc.getCommandType());
        assertEquals("PAUSE", gc.getKeyInput());
    }
}
