package org.kamatech.chess.api;

import org.kamatech.chess.Graphics;

public interface IGraphicsFactory {
    Graphics createGraphics(String pieceCode, String configPath);
}
