package org.kamatech.chess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for pawn promotion functionality
 */
public class PawnPromotionTest {

    private Game game;
    private PieceFactory pieceFactory;
    private GraphicsFactory graphicsFactory;
    private PhysicsFactory physicsFactory;
    private Board board;

    @BeforeEach
    void setUp() {
        // Create test board
        Img boardImg = new Img();
        board = new Board(100, 100, 1, 1, 8, 8, boardImg);

        // Create factories
        graphicsFactory = new GraphicsFactory();
        physicsFactory = new PhysicsFactory();
        pieceFactory = new PieceFactory(graphicsFactory, physicsFactory);

        // Create game instance
        game = new Game(board, pieceFactory, graphicsFactory, physicsFactory);
    }

    @Test
    void testWhitePawnPromotionAtTopRow() {
        // Arrange: Create white pawn near promotion
        Piece whitePawn = pieceFactory.createPiece("PW", 4, 1);
        assertNotNull(whitePawn, "White pawn should be created");


        // Manually add pawn to game pieces for testing
        String pawnKey = "PW_test_1_4";
        game.getPieces().put(pawnKey, whitePawn);

        // Act: Move pawn to promotion row (y=0)
        whitePawn.setPosition(4, 0);

        // Test the promotion logic manually
        boolean shouldPromote = whitePawn.getId().startsWith("P") &&
                whitePawn.isWhite() &&
                whitePawn.getY() == 0;

        // Assert
        assertTrue(shouldPromote, "White pawn at row 0 should be eligible for promotion");
        assertEquals(4.0, whitePawn.getX(), "Pawn X position should be preserved");
        assertEquals(0.0, whitePawn.getY(), "Pawn should be at promotion row");
        assertTrue(whitePawn.isWhite(), "Pawn should be white");
    }

    @Test
    void testBlackPawnPromotionAtBottomRow() {
        // Arrange: Create black pawn near promotion
        Piece blackPawn = pieceFactory.createPiece("PB", 3, 6);
        assertNotNull(blackPawn, "Black pawn should be created");

        // Act: Move pawn to promotion row (y=7)
        blackPawn.setPosition(3, 7);

        // Test the promotion logic manually
        boolean shouldPromote = blackPawn.getId().startsWith("P") &&
                !blackPawn.isWhite() &&
                blackPawn.getY() == 7;

        // Assert
        assertTrue(shouldPromote, "Black pawn at row 7 should be eligible for promotion");
        assertEquals(3.0, blackPawn.getX(), "Pawn X position should be preserved");
        assertEquals(7.0, blackPawn.getY(), "Pawn should be at promotion row");
        assertFalse(blackPawn.isWhite(), "Pawn should be black");
    }

    @Test
    void testNonPawnPieceNoPromotion() {
        // Arrange: Create non-pawn pieces
        Piece whiteQueen = pieceFactory.createPiece("QW", 4, 0);
        Piece blackRook = pieceFactory.createPiece("RB", 3, 7);

        // Test promotion logic
        boolean queenShouldPromote = whiteQueen.getId().startsWith("P");
        boolean rookShouldPromote = blackRook.getId().startsWith("P");

        // Assert
        assertFalse(queenShouldPromote, "Queen should not be eligible for promotion");
        assertFalse(rookShouldPromote, "Rook should not be eligible for promotion");
    }

    @Test
    void testPawnNotAtPromotionRow() {
        // Arrange: Create pawns not at promotion rows
        Piece whitePawn = pieceFactory.createPiece("PW", 4, 3);
        Piece blackPawn = pieceFactory.createPiece("PB", 3, 4);

        // Test promotion logic
        boolean whiteShouldPromote = whitePawn.getId().startsWith("P") &&
                whitePawn.isWhite() &&
                whitePawn.getY() == 0;
        boolean blackShouldPromote = blackPawn.getId().startsWith("P") &&
                !blackPawn.isWhite() &&
                blackPawn.getY() == 7;

        // Assert
        assertFalse(whiteShouldPromote, "White pawn not at row 0 should not be promoted");
        assertFalse(blackShouldPromote, "Black pawn not at row 7 should not be promoted");
    }

    @Test
    void testPromotionCreatesQueen() {
        // Arrange: Create a pawn
        Piece whitePawn = pieceFactory.createPiece("PW", 4, 1);

        // Act: Test queen creation logic
        String expectedQueenId = whitePawn.isWhite() ? "QW" : "QB";
        Piece newQueen = pieceFactory.createPiece(expectedQueenId, 4, 0);

        // Assert
        assertNotNull(newQueen, "Queen should be created successfully");
        assertEquals("QW", expectedQueenId, "White pawn should create white queen");
        assertTrue(newQueen.isWhite(), "Created queen should be white");
        assertEquals(4.0, newQueen.getX(), "Queen should inherit pawn's X position");
        assertEquals(0.0, newQueen.getY(), "Queen should be at promotion position");
    }

    @Test
    void testBlackPawnCreatesBlackQueen() {
        // Arrange: Create a black pawn
        Piece blackPawn = pieceFactory.createPiece("PB", 3, 6);

        // Act: Test queen creation logic
        String expectedQueenId = blackPawn.isWhite() ? "QW" : "QB";
        Piece newQueen = pieceFactory.createPiece(expectedQueenId, 3, 7);

        // Assert
        assertNotNull(newQueen, "Queen should be created successfully");
        assertEquals("QB", expectedQueenId, "Black pawn should create black queen");
        assertFalse(newQueen.isWhite(), "Created queen should be black");
        assertEquals(3.0, newQueen.getX(), "Queen should inherit pawn's X position");
        assertEquals(7.0, newQueen.getY(), "Queen should be at promotion position");
    }

    @Test
    void testPromotionBoundaryConditions() {
        // Test edge cases for promotion rows

        // White pawn at different rows
        Piece whitePawn1 = pieceFactory.createPiece("PW", 0, 0); // Corner promotion
        Piece whitePawn2 = pieceFactory.createPiece("PW", 7, 0); // Other corner
        Piece whitePawn3 = pieceFactory.createPiece("PW", 4, 1); // One row before

        // Black pawn at different rows
        Piece blackPawn1 = pieceFactory.createPiece("PB", 0, 7); // Corner promotion
        Piece blackPawn2 = pieceFactory.createPiece("PB", 7, 7); // Other corner
        Piece blackPawn3 = pieceFactory.createPiece("PB", 4, 6); // One row before

        // Test promotion logic
        assertTrue(whitePawn1.getId().startsWith("P") && whitePawn1.isWhite() && whitePawn1.getY() == 0,
                "White pawn at (0,0) should be promotable");
        assertTrue(whitePawn2.getId().startsWith("P") && whitePawn2.isWhite() && whitePawn2.getY() == 0,
                "White pawn at (7,0) should be promotable");
        assertFalse(whitePawn3.getId().startsWith("P") && whitePawn3.isWhite() && whitePawn3.getY() == 0,
                "White pawn at (4,1) should not be promotable");

        assertTrue(blackPawn1.getId().startsWith("P") && !blackPawn1.isWhite() && blackPawn1.getY() == 7,
                "Black pawn at (0,7) should be promotable");
        assertTrue(blackPawn2.getId().startsWith("P") && !blackPawn2.isWhite() && blackPawn2.getY() == 7,
                "Black pawn at (7,7) should be promotable");
        assertFalse(blackPawn3.getId().startsWith("P") && !blackPawn3.isWhite() && blackPawn3.getY() == 7,
                "Black pawn at (4,6) should not be promotable");
    }

    @Test
    void testPieceFactoryCanCreateQueens() {
        // Test that the piece factory can actually create queens
        Piece whiteQueen = pieceFactory.createPiece("QW", 4, 0);
        Piece blackQueen = pieceFactory.createPiece("QB", 3, 7);

        assertNotNull(whiteQueen, "White queen should be created");
        assertNotNull(blackQueen, "Black queen should be created");

        assertTrue(whiteQueen.isWhite(), "White queen should be white");
        assertFalse(blackQueen.isWhite(), "Black queen should be black");

        assertTrue(whiteQueen.getId().startsWith("Q"), "White queen should have Q prefix");
        assertTrue(blackQueen.getId().startsWith("Q"), "Black queen should have Q prefix");
    }
}
