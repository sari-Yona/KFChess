package org.kamatech.chess.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import org.kamatech.chess.events.PieceMovedEvent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * מבחנים עבור MoveLog - מאזין לרישום מהלכים
 */
public class MoveLogTest {

    private MoveLog moveLog;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        moveLog = new MoveLog();

        // הכנת הפלט לבדיקת הודעות הקונסול
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    @DisplayName("יש לוודא שרשימת המהלכים מתחילה ריקה")
    void testInitialEmptyMoves() {
        // Assert
        List<String> moves = moveLog.getMoves();
        assertNotNull(moves, "רשימת המהלכים לא צריכה להיות null");
        assertTrue(moves.isEmpty(), "רשימת המהלכים צריכה להתחיל ריקה");
    }

    @Test
    @DisplayName("יש לוודא שהמאזין מממש את ממשק EventListener")
    void testImplementsEventListener() {
        // Assert
        assertTrue(moveLog instanceof org.kamatech.chess.events.EventListener,
                "MoveLog צריך לממש את ממשק EventListener");
    }

    @Test
    @DisplayName("יש לוודא שמהלך יחיד נרשם נכון")
    void testSingleMoveLogging() {
        // Act
        PieceMovedEvent event = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        moveLog.onEvent(event);

        // Assert
        List<String> moves = moveLog.getMoves();
        assertEquals(1, moves.size(), "צריך להיות מהלך אחד ברשימה");
        assertEquals("e2 → e4", moves.get(0), "המהלך צריך להיות בפורמט הנכון");

        String output = outputStream.toString();
        assertTrue(output.contains("Move added to log: e2 → e4"), "צריכה להיות הודעה על הוספת המהלך");
    }

    @Test
    @DisplayName("יש לוודא שמספר מהלכים נרשמים ברצף")
    void testMultipleMoves() {
        // Act
        moveLog.onEvent(new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null));
        moveLog.onEvent(new PieceMovedEvent("e7", "e5", "BLACK", "P", 2, null));
        moveLog.onEvent(new PieceMovedEvent("g1", "f3", "WHITE", "N", 3, null));

        // Assert
        List<String> moves = moveLog.getMoves();
        assertEquals(3, moves.size(), "צריכים להיות 3 מהלכים ברשימה");
        assertEquals("e2 → e4", moves.get(0), "המהלך הראשון צריך להיות נכון");
        assertEquals("e7 → e5", moves.get(1), "המהלך השני צריך להיות נכון");
        assertEquals("g1 → f3", moves.get(2), "המהלך השלישי צריך להיות נכון");
    }

    @Test
    @DisplayName("יש לוודא שסדר המהלכים נשמר")
    void testMoveOrder() {
        // Act
        moveLog.onEvent(new PieceMovedEvent("a1", "a2", "WHITE", "R", 1, null));
        moveLog.onEvent(new PieceMovedEvent("b1", "b2", "BLACK", "R", 2, null));
        moveLog.onEvent(new PieceMovedEvent("c1", "c2", "WHITE", "R", 3, null));
        moveLog.onEvent(new PieceMovedEvent("d1", "d2", "BLACK", "R", 4, null));

        // Assert
        List<String> moves = moveLog.getMoves();
        assertEquals(4, moves.size());
        assertEquals("a1 → a2", moves.get(0));
        assertEquals("b1 → b2", moves.get(1));
        assertEquals("c1 → c2", moves.get(2));
        assertEquals("d1 → d2", moves.get(3));
    }

    @Test
    @DisplayName("יש לוודא שמהלכים עם לכידה נרשמים נכון")
    void testMovesWithCapture() {
        // Act
        PieceMovedEvent captureEvent = new PieceMovedEvent("d4", "e5", "WHITE", "P", 5, "Pawn");
        moveLog.onEvent(captureEvent);

        // Assert
        List<String> moves = moveLog.getMoves();
        assertEquals(1, moves.size());
        assertEquals("d4 → e5", moves.get(0), "מהלך עם לכידה צריך להיות בפורמט הבסיסי");

        String output = outputStream.toString();
        assertTrue(output.contains("Move added to log: d4 → e5"));
    }

    @Test
    @DisplayName("יש לוודא שמהלכים של שני השחקנים נרשמים")
    void testBothPlayerMoves() {
        // Act
        moveLog.onEvent(new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null));
        moveLog.onEvent(new PieceMovedEvent("e7", "e5", "BLACK", "P", 2, null));
        moveLog.onEvent(new PieceMovedEvent("f1", "c4", "WHITE", "B", 3, null));
        moveLog.onEvent(new PieceMovedEvent("b8", "c6", "BLACK", "N", 4, null));

        // Assert
        List<String> moves = moveLog.getMoves();
        assertEquals(4, moves.size());

        // בדיקה שהמהלכים של שני השחקנים נרשמו
        assertTrue(moves.contains("e2 → e4"), "מהלך לבן צריך להיות נרשם");
        assertTrue(moves.contains("e7 → e5"), "מהלך שחור צריך להיות נרשם");
        assertTrue(moves.contains("f1 → c4"), "מהלך לבן נוסף צריך להיות נרשם");
        assertTrue(moves.contains("b8 → c6"), "מהלך שחור נוסף צריך להיות נרשם");
    }

    @Test
    @DisplayName("יש לוודא שכל סוגי הכלים נרשמים נכון")
    void testAllPieceTypes() {
        // Act
        moveLog.onEvent(new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null)); // חייל
        moveLog.onEvent(new PieceMovedEvent("a1", "a2", "WHITE", "R", 2, null)); // צריח
        moveLog.onEvent(new PieceMovedEvent("b1", "c3", "WHITE", "N", 3, null)); // סוס
        moveLog.onEvent(new PieceMovedEvent("c1", "d2", "WHITE", "B", 4, null)); // רץ
        moveLog.onEvent(new PieceMovedEvent("d1", "d2", "WHITE", "Q", 5, null)); // מלכה
        moveLog.onEvent(new PieceMovedEvent("e1", "f1", "WHITE", "K", 6, null)); // מלך

        // Assert
        List<String> moves = moveLog.getMoves();
        assertEquals(6, moves.size(), "כל 6 הכלים צריכים להיות נרשמים");
        assertEquals("e2 → e4", moves.get(0));
        assertEquals("a1 → a2", moves.get(1));
        assertEquals("b1 → c3", moves.get(2));
        assertEquals("c1 → d2", moves.get(3));
        assertEquals("d1 → d2", moves.get(4));
        assertEquals("e1 → f1", moves.get(5));
    }

    @Test
    @DisplayName("יש לוודא שמהלכים לכל אורך הלוח נרשמים נכון")
    void testFullBoardMoves() {
        // Act
        moveLog.onEvent(new PieceMovedEvent("a1", "h8", "WHITE", "Q", 1, null));
        moveLog.onEvent(new PieceMovedEvent("h8", "a1", "BLACK", "Q", 2, null));

        // Assert
        List<String> moves = moveLog.getMoves();
        assertEquals(2, moves.size());
        assertEquals("a1 → h8", moves.get(0));
        assertEquals("h8 → a1", moves.get(1));
    }

    @Test
    @DisplayName("יש לוודא שרשימת המהלכים מחזירה עותק בטוח")
    void testMoveListSafety() {
        // Act
        moveLog.onEvent(new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null));
        List<String> moves1 = moveLog.getMoves();
        moveLog.onEvent(new PieceMovedEvent("e7", "e5", "BLACK", "P", 2, null));
        List<String> moves2 = moveLog.getMoves();

        // Assert
        assertEquals(1, moves1.size(), "הרשימה הראשונה צריכה להכיל מהלך אחד");
        assertEquals(2, moves2.size(), "הרשימה השנייה צריכה להכיל שני מהלכים");
    }

    @Test
    @DisplayName("יש לוודא שהודעות הקונסול מודפסות נכון")
    void testConsoleOutput() {
        // Act
        moveLog.onEvent(new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null));
        moveLog.onEvent(new PieceMovedEvent("d7", "d5", "BLACK", "P", 2, null));

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Move added to log: e2 → e4"));
        assertTrue(output.contains("Move added to log: d7 → d5"));
    }

    void tearDown() {
        System.setOut(originalOut);
    }
}
