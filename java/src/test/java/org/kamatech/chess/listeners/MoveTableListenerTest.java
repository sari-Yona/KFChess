package org.kamatech.chess.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import org.kamatech.chess.events.PieceMovedEvent;

import javax.swing.JTable;
import javax.swing.JLabel;

/**
 * מבחנים עבור MoveTableListener - מאזין לטבלת מהלכים
 */
public class MoveTableListenerTest {

    private MoveTableListener moveTableListener;

    @BeforeEach
    void setUp() {
        moveTableListener = new MoveTableListener();
    }

    @Test
    @DisplayName("יש לוודא שהמאזין מממש את ממשק EventListener")
    void testImplementsEventListener() {
        // Assert
        assertTrue(moveTableListener instanceof org.kamatech.chess.events.EventListener,
                "MoveTableListener צריך לממש את ממשק EventListener");
    }

    @Test
    @DisplayName("יש לוודא שיצירת המאזין לא זורקת חריגה")
    void testConstructorDoesNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> new MoveTableListener(),
                "יצירת MoveTableListener לא צריכה לזרוק חריגה");
    }

    @Test
    @DisplayName("יש לוודא שקיימים טבלאות הלבן והשחור")
    void testTablesExist() {
        // Act
        JTable whiteTable = moveTableListener.getWhiteTable();
        JTable blackTable = moveTableListener.getBlackTable();

        // Assert
        assertNotNull(whiteTable, "טבלת הלבן צריכה להיות קיימת");
        assertNotNull(blackTable, "טבלת השחור צריכה להיות קיימת");
    }

    @Test
    @DisplayName("יש לוודא שקיימות תוויות הניקוד")
    void testScoreLabelsExist() {
        // Act
        JLabel whiteScoreLabel = moveTableListener.getWhiteScoreLabel();
        JLabel blackScoreLabel = moveTableListener.getBlackScoreLabel();

        // Assert
        assertNotNull(whiteScoreLabel, "תווית ניקוד הלבן צריכה להיות קיימת");
        assertNotNull(blackScoreLabel, "תווית ניקוד השחור צריכה להיות קיימת");

        assertEquals("Score: 0", whiteScoreLabel.getText(), "ניקוד הלבן ההתחלתי צריך להיות 0");
        assertEquals("Score: 0", blackScoreLabel.getText(), "ניקוד השחור ההתחלתי צריך להיות 0");
    }

    @Test
    @DisplayName("יש לוודא שהטבלאות מכילות את העמודות הנכונות")
    void testTableColumns() {
        // Act
        JTable whiteTable = moveTableListener.getWhiteTable();
        JTable blackTable = moveTableListener.getBlackTable();

        // Assert
        assertEquals(5, whiteTable.getColumnCount(), "טבלת הלבן צריכה להכיל 5 עמודות");
        assertEquals(5, blackTable.getColumnCount(), "טבלת השחור צריכה להכיל 5 עמודות");

        // בדיקת שמות העמודות
        String[] expectedColumns = { "Move #", "Time", "Piece", "From → To", "Capture" };
        for (int i = 0; i < expectedColumns.length; i++) {
            assertEquals(expectedColumns[i], whiteTable.getColumnName(i),
                    "עמודה " + i + " בטבלת הלבן צריכה להיות " + expectedColumns[i]);
            assertEquals(expectedColumns[i], blackTable.getColumnName(i),
                    "עמודה " + i + " בטבלת השחור צריכה להיות " + expectedColumns[i]);
        }
    }

    @Test
    @DisplayName("יש לוודא שמהלך של לבן נוסף לטבלת הלבן")
    void testWhiteMoveAddedToWhiteTable() {
        // Act
        PieceMovedEvent whiteEvent = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        moveTableListener.onEvent(whiteEvent);

        // Assert
        JTable whiteTable = moveTableListener.getWhiteTable();
        JTable blackTable = moveTableListener.getBlackTable();

        assertEquals(1, whiteTable.getRowCount(), "טבלת הלבן צריכה להכיל שורה אחת");
        assertEquals(0, blackTable.getRowCount(), "טבלת השחור צריכה להישאר ריקה");

        // בדיקת תוכן השורה
        assertEquals("1", whiteTable.getValueAt(0, 0).toString(), "מספר המהלך צריך להיות 1");
        assertEquals("P", whiteTable.getValueAt(0, 2).toString(), "סוג הכלי צריך להיות P");
        assertEquals("e2 → e4", whiteTable.getValueAt(0, 3).toString(), "המהלך צריך להיות e2 → e4");
        assertEquals("", whiteTable.getValueAt(0, 4).toString(), "לא צריכה להיות לכידה");
    }

    @Test
    @DisplayName("יש לוודא שמהלך של שחור נוסף לטבלת השחור")
    void testBlackMoveAddedToBlackTable() {
        // Act
        PieceMovedEvent blackEvent = new PieceMovedEvent("e7", "e5", "BLACK", "P", 2, null);
        moveTableListener.onEvent(blackEvent);

        // Assert
        JTable whiteTable = moveTableListener.getWhiteTable();
        JTable blackTable = moveTableListener.getBlackTable();

        assertEquals(0, whiteTable.getRowCount(), "טבלת הלבן צריכה להישאר ריקה");
        assertEquals(1, blackTable.getRowCount(), "טבלת השחור צריכה להכיל שורה אחת");

        // בדיקת תוכן השורה
        assertEquals("2", blackTable.getValueAt(0, 0).toString(), "מספר המהלך צריך להיות 2");
        assertEquals("P", blackTable.getValueAt(0, 2).toString(), "סוג הכלי צריך להיות P");
        assertEquals("e7 → e5", blackTable.getValueAt(0, 3).toString(), "המהלך צריך להיות e7 → e5");
    }

    @Test
    @DisplayName("יש לוודא שמהלך עם לכידה מוצג נכון")
    void testMoveWithCapture() {
        // Act
        PieceMovedEvent captureEvent = new PieceMovedEvent("d4", "e5", "WHITE", "P", 3, "Pawn");
        moveTableListener.onEvent(captureEvent);

        // Assert
        JTable whiteTable = moveTableListener.getWhiteTable();
        assertEquals(1, whiteTable.getRowCount());
        assertEquals("Pawn", whiteTable.getValueAt(0, 4).toString(), "הלכידה צריכה להיות מוצגת");
    }

    @Test
    @DisplayName("יש לוודא שמספר מהלכים נוספים בסדר הנכון")
    void testMultipleMoves() {
        // Act
        moveTableListener.onEvent(new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null));
        moveTableListener.onEvent(new PieceMovedEvent("e7", "e5", "BLACK", "P", 2, null));
        moveTableListener.onEvent(new PieceMovedEvent("g1", "f3", "WHITE", "N", 3, null));
        moveTableListener.onEvent(new PieceMovedEvent("b8", "c6", "BLACK", "N", 4, null));

        // Assert
        JTable whiteTable = moveTableListener.getWhiteTable();
        JTable blackTable = moveTableListener.getBlackTable();

        assertEquals(2, whiteTable.getRowCount(), "טבלת הלבן צריכה להכיל 2 שורות");
        assertEquals(2, blackTable.getRowCount(), "טבלת השחור צריכה להכיל 2 שורות");

        // בדיקת סדר המהלכים
        assertEquals("1", whiteTable.getValueAt(0, 0).toString());
        assertEquals("3", whiteTable.getValueAt(1, 0).toString());
        assertEquals("2", blackTable.getValueAt(0, 0).toString());
        assertEquals("4", blackTable.getValueAt(1, 0).toString());
    }

    @Test
    @DisplayName("יש לוודא שעמודת הזמן מכילה נתונים")
    void testTimeColumn() {
        // Act
        PieceMovedEvent event = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        moveTableListener.onEvent(event);

        // Assert
        JTable whiteTable = moveTableListener.getWhiteTable();
        String timeValue = whiteTable.getValueAt(0, 1).toString();
        assertNotNull(timeValue, "עמודת הזמן לא צריכה להיות null");
        assertFalse(timeValue.isEmpty(), "עמודת הזמן לא צריכה להיות ריקה");
    }

    @Test
    @DisplayName("יש לוודא שהמתודה onEvent לא זורקת חריגה")
    void testOnEventDoesNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            moveTableListener.onEvent(new PieceMovedEvent("a1", "a2", "WHITE", "R", 1, null));
            moveTableListener.onEvent(new PieceMovedEvent("h8", "h7", "BLACK", "R", 2, null));
        }, "onEvent לא צריך לזרוק חריגה");
    }

    @Test
    @DisplayName("יש לוודא שהניקוד מתעדכן עבור לכידות")
    void testScoreUpdateOnCapture() {
        // Act
        moveTableListener.onEvent(new PieceMovedEvent("d4", "e5", "WHITE", "P", 1, "Pawn"));
        moveTableListener.onEvent(new PieceMovedEvent("f7", "e6", "BLACK", "P", 2, "Queen"));

        // Assert
        JLabel whiteScoreLabel = moveTableListener.getWhiteScoreLabel();
        JLabel blackScoreLabel = moveTableListener.getBlackScoreLabel();

        assertEquals("Score: 1", whiteScoreLabel.getText(), "ניקוד הלבן צריך להתעדכן לאחר לכידת חייל");
        assertEquals("Score: 9", blackScoreLabel.getText(), "ניקוד השחור צריך להתעדכן לאחר לכידת מלכה");
    }
}
