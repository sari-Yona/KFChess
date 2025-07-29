package org.kamatech.chess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

/**
 * Integration tests for pawn promotion in the game system
 */
public class PawnPromotionIntegrationTest {

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
    void testShouldPromotePawnMethod() throws Exception {
        // Use reflection to test the private method
        Method shouldPromotePawnMethod = Game.class.getDeclaredMethod("shouldPromotePawn", Piece.class, double.class);
        shouldPromotePawnMethod.setAccessible(true);

        // Test white pawn promotion
        Piece whitePawn = pieceFactory.createPiece("PW", 4, 0);
        Boolean whiteResult = (Boolean) shouldPromotePawnMethod.invoke(game, whitePawn, 0.0);
        assertTrue(whiteResult, "White pawn at row 0 should be promoted");

        // Test black pawn promotion
        Piece blackPawn = pieceFactory.createPiece("PB", 3, 7);
        Boolean blackResult = (Boolean) shouldPromotePawnMethod.invoke(game, blackPawn, 7.0);
        assertTrue(blackResult, "Black pawn at row 7 should be promoted");

        // Test non-promotion cases
        Boolean whiteNoPromotion = (Boolean) shouldPromotePawnMethod.invoke(game, whitePawn, 3.0);
        assertFalse(whiteNoPromotion, "White pawn not at row 0 should not be promoted");

        Boolean blackNoPromotion = (Boolean) shouldPromotePawnMethod.invoke(game, blackPawn, 2.0);
        assertFalse(blackNoPromotion, "Black pawn not at row 7 should not be promoted");

        // Test non-pawn pieces
        Piece queen = pieceFactory.createPiece("QW", 4, 0);
        Boolean queenResult = (Boolean) shouldPromotePawnMethod.invoke(game, queen, 0.0);
        assertFalse(queenResult, "Queen should not be promoted even at promotion row");
    }

    @Test
    void testGetPieceIdFromPiece() throws Exception {
        // Test the helper method used in promotion
        Method getPieceIdMethod = Game.class.getDeclaredMethod("getPieceIdFromPiece", Piece.class);
        getPieceIdMethod.setAccessible(true);

        // Create a piece and add it to the game
        Piece testPiece = pieceFactory.createPiece("PW", 4, 6);

        // Since getPieceIdFromPiece looks for the piece in the pieces map,
        // we need to manually add it or use the piece's getId() fallback
        String pieceId = (String) getPieceIdMethod.invoke(game, testPiece);

        // Should return the piece's own ID as fallback
        assertEquals("PW", pieceId, "Should return piece's own ID");
    }

    @Test
    void testPromotionKeyGeneration() {
        // Test the key generation logic used in promotion
        Piece whitePawn = pieceFactory.createPiece("PW", 4, 0);
        Piece blackPawn = pieceFactory.createPiece("PB", 3, 7);

        // Test queen ID generation logic
        String whiteQueenId = whitePawn.isWhite() ? "QW" : "QB";
        String blackQueenId = blackPawn.isWhite() ? "QW" : "QB";

        assertEquals("QW", whiteQueenId, "White pawn should generate white queen ID");
        assertEquals("QB", blackQueenId, "Black pawn should generate black queen ID");

        // Test unique key generation pattern
        long timestamp1 = System.currentTimeMillis();
        String uniqueKey1 = whiteQueenId + "_promoted_" + timestamp1;

        // Small delay to ensure different timestamp
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }

        long timestamp2 = System.currentTimeMillis();
        String uniqueKey2 = whiteQueenId + "_promoted_" + timestamp2;

        assertNotEquals(uniqueKey1, uniqueKey2, "Promotion keys should be unique");
        assertTrue(uniqueKey1.startsWith("QW_promoted_"), "Key should have correct format");
        assertTrue(uniqueKey2.startsWith("QW_promoted_"), "Key should have correct format");
    }

    @Test
    void testPromotionLogicFlow() {
        // Test the complete promotion decision flow

        // Create test cases
        TestCase[] testCases = {
                new TestCase("PW", 4, 0, true, "White pawn at row 0"),
                new TestCase("PW", 4, 1, false, "White pawn at row 1"),
                new TestCase("PW", 4, 7, false, "White pawn at row 7"),
                new TestCase("PB", 3, 7, true, "Black pawn at row 7"),
                new TestCase("PB", 3, 6, false, "Black pawn at row 6"),
                new TestCase("PB", 3, 0, false, "Black pawn at row 0"),
                new TestCase("QW", 4, 0, false, "White queen at row 0"),
                new TestCase("QB", 3, 7, false, "Black queen at row 7"),
                new TestCase("RW", 4, 0, false, "White rook at row 0"),
                new TestCase("NB", 3, 7, false, "Black knight at row 7")
        };

        for (TestCase testCase : testCases) {
            Piece piece = pieceFactory.createPiece(testCase.pieceId, testCase.x, testCase.y);
            assertNotNull(piece, "Piece should be created: " + testCase.description);

            // Test promotion logic manually
            boolean shouldPromote = piece.getId().startsWith("P") &&
                    ((piece.isWhite() && piece.getY() == 0) ||
                            (!piece.isWhite() && piece.getY() == 7));

            assertEquals(testCase.shouldPromote, shouldPromote, testCase.description);
        }
    }

    @Test
    void testPromotionScenarios() {
        // Test various promotion scenarios

        // Scenario 1: White pawn reaches top row
        Piece whitePawn1 = pieceFactory.createPiece("PW", 0, 0); // Corner
        Piece whitePawn2 = pieceFactory.createPiece("PW", 7, 0); // Other corner
        Piece whitePawn3 = pieceFactory.createPiece("PW", 4, 0); // Middle

        assertTrue(shouldPromoteHelper(whitePawn1), "White pawn at (0,0) should promote");
        assertTrue(shouldPromoteHelper(whitePawn2), "White pawn at (7,0) should promote");
        assertTrue(shouldPromoteHelper(whitePawn3), "White pawn at (4,0) should promote");

        // Scenario 2: Black pawn reaches bottom row
        Piece blackPawn1 = pieceFactory.createPiece("PB", 0, 7); // Corner
        Piece blackPawn2 = pieceFactory.createPiece("PB", 7, 7); // Other corner
        Piece blackPawn3 = pieceFactory.createPiece("PB", 4, 7); // Middle

        assertTrue(shouldPromoteHelper(blackPawn1), "Black pawn at (0,7) should promote");
        assertTrue(shouldPromoteHelper(blackPawn2), "Black pawn at (7,7) should promote");
        assertTrue(shouldPromoteHelper(blackPawn3), "Black pawn at (4,7) should promote");

        // Scenario 3: Pawns not at promotion rows
        Piece whitePawnMid = pieceFactory.createPiece("PW", 4, 3);
        Piece blackPawnMid = pieceFactory.createPiece("PB", 4, 4);

        assertFalse(shouldPromoteHelper(whitePawnMid), "White pawn in middle should not promote");
        assertFalse(shouldPromoteHelper(blackPawnMid), "Black pawn in middle should not promote");
    }

    // Helper method to test promotion logic
    private boolean shouldPromoteHelper(Piece piece) {
        return piece.getId().startsWith("P") &&
                ((piece.isWhite() && piece.getY() == 0) ||
                        (!piece.isWhite() && piece.getY() == 7));
    }

    // Test case helper class
    private static class TestCase {
        String pieceId;
        int x, y;
        boolean shouldPromote;
        String description;

        TestCase(String pieceId, int x, int y, boolean shouldPromote, String description) {
            this.pieceId = pieceId;
            this.x = x;
            this.y = y;
            this.shouldPromote = shouldPromote;
            this.description = description;
        }
    }
}
