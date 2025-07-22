package org.kamatech.chess;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/**
 * Handles all key input and delegates to Game instance.
 */
public class InputHandler implements KeyListener {
    private final Game game;

    public InputHandler(Game game) {
        this.game = game;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        game.handleRawKeyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        game.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        game.keyTyped(e);
    }
}
