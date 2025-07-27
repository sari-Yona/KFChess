package org.kamatech.chess.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import org.kamatech.chess.events.PieceCapturedEvent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * מבחנים עבור ScoreTracker - מאזין לעקיבת ניקוד
 */
public class ScoreTrackerTest {

    private ScoreTracker scoreTracker;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        scoreTracker = new ScoreTracker();

        // הכנת הפלט לבדיקת הודעות הקונסול
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    @DisplayName("יש לוודא שהניקוד מתחיל מאפס")
    void testInitialScore() {
        // Assert
        assertEquals(0, scoreTracker.getScore(), "הניקוד ההתחלתי צריך להיות 0");
    }

    @Test
    @DisplayName("יש לוודא שלכידת חייל מוסיפה נקודה אחת")
    void testPawnCapture() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("Pawn");
        scoreTracker.onEvent(event);

        // Assert
        assertEquals(1, scoreTracker.getScore(), "לכידת חייל צריכה להוסיף נקודה אחת");

        String output = outputStream.toString();
        assertTrue(output.contains("Score updated: 1"), "צריכה להיות הודעה על עדכון הניקוד");
    }

    @Test
    @DisplayName("יש לוודא שלכידת סוס מוסיפה 3 נקודות")
    void testKnightCapture() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("Knight");
        scoreTracker.onEvent(event);

        // Assert
        assertEquals(3, scoreTracker.getScore(), "לכידת סוס צריכה להוסיף 3 נקודות");
    }

    @Test
    @DisplayName("יש לוודא שלכידת רץ מוסיפה 3 נקודות")
    void testBishopCapture() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("Bishop");
        scoreTracker.onEvent(event);

        // Assert
        assertEquals(3, scoreTracker.getScore(), "לכידת רץ צריכה להוסיף 3 נקודות");
    }

    @Test
    @DisplayName("יש לוודא שלכידת צריח מוסיפה 5 נקודות")
    void testRookCapture() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("Rook");
        scoreTracker.onEvent(event);

        // Assert
        assertEquals(5, scoreTracker.getScore(), "לכידת צריח צריכה להוסיף 5 נקודות");
    }

    @Test
    @DisplayName("יש לוודא שלכידת מלכה מוסיפה 9 נקודות")
    void testQueenCapture() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("Queen");
        scoreTracker.onEvent(event);

        // Assert
        assertEquals(9, scoreTracker.getScore(), "לכידת מלכה צריכה להוסיף 9 נקודות");
    }

    @Test
    @DisplayName("יש לוודא שלכידת כלי לא מוכר לא משנה את הניקוד")
    void testUnknownPieceCapture() {
        // Act
        PieceCapturedEvent event = new PieceCapturedEvent("UnknownPiece");
        scoreTracker.onEvent(event);

        // Assert
        assertEquals(0, scoreTracker.getScore(), "לכידת כלי לא מוכר לא צריכה לשנות את הניקוד");
    }

    @Test
    @DisplayName("יש לוודא שניקוד מצטבר נכון עם מספר לכידות")
    void testMultipleCaptures() {
        // Act
        scoreTracker.onEvent(new PieceCapturedEvent("Pawn")); // +1 = 1
        scoreTracker.onEvent(new PieceCapturedEvent("Knight")); // +3 = 4
        scoreTracker.onEvent(new PieceCapturedEvent("Bishop")); // +3 = 7
        scoreTracker.onEvent(new PieceCapturedEvent("Rook")); // +5 = 12
        scoreTracker.onEvent(new PieceCapturedEvent("Queen")); // +9 = 21

        // Assert
        assertEquals(21, scoreTracker.getScore(), "הניקוד המצטבר צריך להיות 21");
    }

    @Test
    @DisplayName("יש לוודא שלכידת מספר חיילים מצטברת נכון")
    void testMultiplePawnCaptures() {
        // Act
        scoreTracker.onEvent(new PieceCapturedEvent("Pawn"));
        scoreTracker.onEvent(new PieceCapturedEvent("Pawn"));
        scoreTracker.onEvent(new PieceCapturedEvent("Pawn"));

        // Assert
        assertEquals(3, scoreTracker.getScore(), "3 חיילים צריכים להוסיף 3 נקודות");
    }

    @Test
    @DisplayName("יש לוודא שההודעות נכתבות לקונסול")
    void testConsoleOutput() {
        // Act
        scoreTracker.onEvent(new PieceCapturedEvent("Pawn"));
        scoreTracker.onEvent(new PieceCapturedEvent("Queen"));

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Score updated: 1"), "צריכה להיות הודעה ראשונה");
        assertTrue(output.contains("Score updated: 10"), "צריכה להיות הודעה שנייה");
    }

    @Test
    @DisplayName("יש לוודא שהמאזין מממש את ממשק EventListener")
    void testImplementsEventListener() {
        // Assert
        assertTrue(scoreTracker instanceof org.kamatech.chess.events.EventListener,
                "ScoreTracker צריך לממש את ממשק EventListener");
    }

    @Test
    @DisplayName("יש לוודא שלכידת כלים עם אותיות קטנות עובדת")
    void testLowercasePieceNames() {
        // Act
        scoreTracker.onEvent(new PieceCapturedEvent("pawn"));

        // Assert
        assertEquals(0, scoreTracker.getScore(), "שמות כלים באותיות קטנות לא צריכים להיות מזוהים");
    }

    @Test
    @DisplayName("יש לוודא שטיפול ב-null לא גורם לחריגה")
    void testNullPieceName() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            scoreTracker.onEvent(new PieceCapturedEvent(null));
        }, "טיפול ב-null לא צריך לגרום לחריגה");

        assertEquals(0, scoreTracker.getScore(), "null לא צריך לשנות את הניקוד");
    }

    void tearDown() {
        System.setOut(originalOut);
    }
}
