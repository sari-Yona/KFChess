package org.kamatech.chess;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

public class StateTest {

    @Test
    void testInitialStateAndClone() {
        // Arrange
        Moves moves = new Moves(Collections.emptyList(), 123L);
        Graphics graphics = new Graphics(null);
        Physics physics = new Physics(null);

        // Act
        State state = new State(moves, graphics, physics);
        State clone = state.clone();

        // Assert
        assertNotSame(state, clone, "Cloned state should be a new instance");
        assertEquals(state.getCurrentState(), clone.getCurrentState(), "Clone should copy current state");
        assertEquals(state.getMoves().getCooldown(), clone.getMoves().getCooldown(),
                "Clone should copy moves cooldown");
        assertNotSame(state.getGraphics(), clone.getGraphics(), "Clone should create a new graphics instance");
        assertNotSame(state.getPhysics(), clone.getPhysics(), "Clone should create a new physics instance");
        assertTrue(state.canPerformAction(), "New state should allow action");
    }
}
