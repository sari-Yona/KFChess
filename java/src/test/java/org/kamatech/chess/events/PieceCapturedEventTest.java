package org.kamatech.chess.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * מבחנים עבור PieceCapturedEvent - אירוע לכידת כלי
 */
public class PieceCapturedEventTest {

    @Test
    @DisplayName("יש לוודא שיצירת אירוע לכידת חייל עובדת")
    void testPawnCaptureEvent() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("Pawn");

        // Assert
        assertEquals("Pawn", event.pieceType, "סוג הכלי צריך להיות Pawn");
    }

    @Test
    @DisplayName("יש לוודא שיצירת אירוע לכידת מלכה עובדת")
    void testQueenCaptureEvent() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("Queen");

        // Assert
        assertEquals("Queen", event.pieceType, "סוג הכלי צריך להיות Queen");
    }

    @Test
    @DisplayName("יש לוודא שכל סוגי הכלים נתמכים בלכידה")
    void testAllCaptureablePieceTypes() {
        // Arrange
        String[] pieceTypes = { "Pawn", "Rook", "Knight", "Bishop", "Queen", "King" };

        // Act & Assert
        for (String pieceType : pieceTypes) {
            PieceCapturedEvent event = new PieceCapturedEvent(pieceType);
            assertEquals(pieceType, event.pieceType, "סוג הכלי " + pieceType + " צריך להיות נתמך בלכידה");
        }
    }

    @Test
    @DisplayName("יש לוודא שהאירוע מממש את ממשק Event")
    void testImplementsEventInterface() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("Knight");

        // Assert
        assertTrue(event instanceof Event, "PieceCapturedEvent צריך לממש את ממשק Event");
    }

    @Test
    @DisplayName("יש לוודא שאירועי לכידה שונים נוצרים בנפרד")
    void testMultipleCaptureEvents() {
        // Act
        PieceCapturedEvent event1 = new PieceCapturedEvent("Pawn");
        PieceCapturedEvent event2 = new PieceCapturedEvent("Bishop");
        PieceCapturedEvent event3 = new PieceCapturedEvent("Rook");

        // Assert
        assertEquals("Pawn", event1.pieceType);
        assertEquals("Bishop", event2.pieceType);
        assertEquals("Rook", event3.pieceType);

        // וודא שהם אירועים נפרדים
        assertNotSame(event1, event2, "אירועים שונים צריכים להיות עצמים נפרדים");
        assertNotSame(event2, event3, "אירועים שונים צריכים להיות עצמים נפרדים");
    }

    @Test
    @DisplayName("יש לוודא שניתן ליצור אירוע עם שם כלי ארוך")
    void testLongPieceName() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("Queen of Hearts");

        // Assert
        assertEquals("Queen of Hearts", event.pieceType, "שמות כלים ארוכים צריכים להיות נתמכים");
    }

    @Test
    @DisplayName("יש לוודא שניתן ליצור אירוע עם שם כלי ריק")
    void testEmptyPieceName() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("");

        // Assert
        assertEquals("", event.pieceType, "שם כלי ריק צריך להיות נתמך");
    }

    @Test
    @DisplayName("יש לוודא שניתן ליצור אירוע עם null")
    void testNullPieceName() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent(null);

        // Assert
        assertNull(event.pieceType, "null צריך להיות נתמך כשם כלי");
    }
}
