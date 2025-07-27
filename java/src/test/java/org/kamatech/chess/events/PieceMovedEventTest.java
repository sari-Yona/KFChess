package org.kamatech.chess.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * מבחנים עבור PieceMovedEvent - אירוע תזוזת כלי
 */
public class PieceMovedEventTest {

    @Test
    @DisplayName("יש לוודא שיצירת אירוע עם נתונים בסיסיים עובדת")
    void testBasicEventCreation() {
        // Act
        PieceMovedEvent event = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);

        // Assert
        assertEquals("e2", event.from, "מיקום המקור צריך להיות נכון");
        assertEquals("e4", event.to, "מיקום היעד צריך להיות נכון");
        assertEquals("WHITE", event.player, "השחקן צריך להיות נכון");
        assertEquals("P", event.pieceType, "סוג הכלי צריך להיות נכון");
        assertEquals(1, event.moveNumber, "מספר המהלך צריך להיות נכון");
        assertNull(event.capturedPiece, "לא צריך להיות כלי שנלכד");
        assertTrue(event.timestamp > 0, "הזמן צריך להיות מוגדר");
        assertTrue(event.timestamp <= System.currentTimeMillis(), "הזמן צריך להיות סביר");
    }

    @Test
    @DisplayName("יש לוודא שיצירת אירוע עם לכידת כלי עובדת")
    void testEventWithCapture() {
        // Act
        PieceMovedEvent event = new PieceMovedEvent("d4", "d5", "BLACK", "Q", 15, "Pawn");

        // Assert
        assertEquals("d4", event.from);
        assertEquals("d5", event.to);
        assertEquals("BLACK", event.player);
        assertEquals("Q", event.pieceType);
        assertEquals(15, event.moveNumber);
        assertEquals("Pawn", event.capturedPiece, "הכלי שנלכד צריך להיות מוגדר");
    }

    @Test
    @DisplayName("יש לוודא שזמני יצירה שונים לאירועים שונים")
    void testTimestampDifferences() throws InterruptedException {
        // Act
        PieceMovedEvent event1 = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        Thread.sleep(1); // המתנה קצרה כדי לוודא זמנים שונים
        PieceMovedEvent event2 = new PieceMovedEvent("e7", "e5", "BLACK", "P", 2, null);

        // Assert
        assertTrue(event2.timestamp >= event1.timestamp, "האירוע השני צריך להיות מאוחר יותר או בו זמנית");
    }

    @Test
    @DisplayName("יש לוודא שמספרי מהלכים שונים נשמרים נכון")
    void testDifferentMoveNumbers() {
        // Act
        PieceMovedEvent event1 = new PieceMovedEvent("a1", "a2", "WHITE", "R", 1, null);
        PieceMovedEvent event2 = new PieceMovedEvent("h8", "h7", "BLACK", "R", 100, null);

        // Assert
        assertEquals(1, event1.moveNumber);
        assertEquals(100, event2.moveNumber);
    }

    @Test
    @DisplayName("יש לוודא שכל סוגי הכלים נתמכים")
    void testAllPieceTypes() {
        // Act & Assert
        String[] pieceTypes = { "P", "R", "N", "B", "Q", "K" };
        for (String pieceType : pieceTypes) {
            PieceMovedEvent event = new PieceMovedEvent("a1", "a2", "WHITE", pieceType, 1, null);
            assertEquals(pieceType, event.pieceType, "סוג הכלי " + pieceType + " צריך להיות נתמך");
        }
    }

    @Test
    @DisplayName("יש לוודא ששני השחקנים נתמכים")
    void testBothPlayers() {
        // Act
        PieceMovedEvent whiteEvent = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        PieceMovedEvent blackEvent = new PieceMovedEvent("e7", "e5", "BLACK", "P", 2, null);

        // Assert
        assertEquals("WHITE", whiteEvent.player);
        assertEquals("BLACK", blackEvent.player);
    }

    @Test
    @DisplayName("יש לוודא שמיקומים שונים נשמרים נכון")
    void testDifferentPositions() {
        // Act
        PieceMovedEvent event = new PieceMovedEvent("a1", "h8", "WHITE", "Q", 1, null);

        // Assert
        assertEquals("a1", event.from, "מיקום המקור צריך להיות a1");
        assertEquals("h8", event.to, "מיקום היעד צריך להיות h8");
    }

    @Test
    @DisplayName("יש לוודא שכלים שנלכדו שונים נשמרים נכון")
    void testDifferentCapturedPieces() {
        // Act
        String[] capturedPieces = { "Pawn", "Rook", "Knight", "Bishop", "Queen" };

        for (String captured : capturedPieces) {
            PieceMovedEvent event = new PieceMovedEvent("a1", "a2", "WHITE", "Q", 1, captured);
            assertEquals(captured, event.capturedPiece, "הכלי שנלכד " + captured + " צריך להיות נכון");
        }
    }

    @Test
    @DisplayName("יש לוודא שהאירוע מממש את ממשק Event")
    void testImplementsEventInterface() {
        // Act
        PieceMovedEvent event = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);

        // Assert
        assertTrue(event instanceof Event, "PieceMovedEvent צריך לממש את ממשק Event");
    }
}
