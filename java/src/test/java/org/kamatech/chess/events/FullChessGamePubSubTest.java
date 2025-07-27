package org.kamatech.chess.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import org.kamatech.chess.listeners.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * טסט מקיף לכל מערכת PUB/SUB - סימולציה של משחק שח מלא
 */
public class FullChessGamePubSubTest {

    private EventBus eventBus;
    private ScoreTracker whiteScoreTracker;
    private ScoreTracker blackScoreTracker;
    private MoveLog moveLog;
    private SoundPlayer soundPlayer;
    private MoveTableListener moveTableListener;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
        whiteScoreTracker = new ScoreTracker();
        blackScoreTracker = new ScoreTracker();
        moveLog = new MoveLog();
        soundPlayer = new SoundPlayer();
        moveTableListener = new MoveTableListener();

        // הרשמת כל המאזינים לאירועים הרלוונטיים
        eventBus.subscribe(PieceMovedEvent.class, moveLog);
        eventBus.subscribe(PieceMovedEvent.class, moveTableListener);
        eventBus.subscribe(PieceCapturedEvent.class, whiteScoreTracker);
        eventBus.subscribe(PieceCapturedEvent.class, blackScoreTracker);
        eventBus.subscribe(SoundEvent.class, soundPlayer);

        // הכנת הפלט לבדיקת הודעות הקונסול
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    @DisplayName("סימולציה של משחק שח קצר עם כל מערכת PUB/SUB")
    void testCompleteChessGameSimulation() {
        // Act - סימולציה של משחק קצר

        // התחלת משחק
        eventBus.publish(new GameStartedEvent());

        // מהלך 1: e2-e4 (לבן)
        eventBus.publish(new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.MOVE));

        // מהלך 2: e7-e5 (שחור)
        eventBus.publish(new PieceMovedEvent("e7", "e5", "BLACK", "P", 2, null));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.MOVE));

        // מהלך 3: Nf3 (לבן)
        eventBus.publish(new PieceMovedEvent("g1", "f3", "WHITE", "N", 3, null));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.MOVE));

        // מהלך 4: Nc6 (שחור)
        eventBus.publish(new PieceMovedEvent("b8", "c6", "BLACK", "N", 4, null));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.MOVE));

        // מהלך 5: Bc4 (לבן)
        eventBus.publish(new PieceMovedEvent("f1", "c4", "WHITE", "B", 5, null));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.MOVE));

        // מהלך 6: d6 (שחור)
        eventBus.publish(new PieceMovedEvent("d7", "d6", "BLACK", "P", 6, null));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.MOVE));

        // מהלך 7: לכידת חייל (לבן לוכד בf7)
        eventBus.publish(new PieceMovedEvent("c4", "f7", "WHITE", "B", 7, "Pawn"));
        eventBus.publish(new PieceCapturedEvent("Pawn"));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.EAT));

        // מהלך 8: המלך נאלץ לזוז (שחור)
        eventBus.publish(new PieceMovedEvent("e8", "f7", "BLACK", "K", 8, "Bishop"));
        eventBus.publish(new PieceCapturedEvent("Bishop"));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.EAT));

        // סיום המשחק
        eventBus.publish(new GameEndedEvent("WHITE"));

        // Assert - בדיקת כל הרכיבים

        // בדיקת רישום המהלכים
        assertEquals(8, moveLog.getMoves().size(), "צריכים להיות 8 מהלכים ברישום");
        assertEquals("e2 → e4", moveLog.getMoves().get(0));
        assertEquals("e7 → e5", moveLog.getMoves().get(1));
        assertEquals("g1 → f3", moveLog.getMoves().get(2));
        assertEquals("b8 → c6", moveLog.getMoves().get(3));
        assertEquals("f1 → c4", moveLog.getMoves().get(4));
        assertEquals("d7 → d6", moveLog.getMoves().get(5));
        assertEquals("c4 → f7", moveLog.getMoves().get(6));
        assertEquals("e8 → f7", moveLog.getMoves().get(7));

        // בדיקת הניקוד (לבן לוכד חייל=1, שחור לוכד רץ=3)
        assertEquals(1, whiteScoreTracker.getScore(), "הלבן צריך לקבל נקודה אחת על החייל");
        assertEquals(3, blackScoreTracker.getScore(), "השחור צריך לקבל 3 נקודות על הרץ");

        // בדיקת הטבלאות
        assertEquals(4, moveTableListener.getWhiteTable().getRowCount(), "טבלת הלבן צריכה להכיל 4 מהלכים");
        assertEquals(4, moveTableListener.getBlackTable().getRowCount(), "טבלת השחור צריכה להכיל 4 מהלכים");

        // בדיקת עדכון הניקוד בתוויות
        assertEquals("Score: 1", moveTableListener.getWhiteScoreLabel().getText());
        assertEquals("Score: 3", moveTableListener.getBlackScoreLabel().getText());

        // בדיקת הודעות הצליל
        String output = outputStream.toString();
        assertTrue(output.contains("DEBUG: Playing MOVE sound via SoundEvent"));
        assertTrue(output.contains("DEBUG: Playing EAT sound via SoundEvent"));
    }

    @Test
    @DisplayName("בדיקת ביצועים עם הרבה אירועים")
    void testPerformanceWithManyEvents() {
        // Act - פרסום הרבה אירועים
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= 1000; i++) {
            String from = "a" + ((i % 8) + 1);
            String to = "b" + ((i % 8) + 1);
            String player = (i % 2 == 1) ? "WHITE" : "BLACK";

            eventBus.publish(new PieceMovedEvent(from, to, player, "P", i, null));

            if (i % 10 == 0) {
                eventBus.publish(new PieceCapturedEvent("Pawn"));
                eventBus.publish(new SoundEvent(SoundEvent.SoundType.EAT));
            } else {
                eventBus.publish(new SoundEvent(SoundEvent.SoundType.MOVE));
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert
        assertEquals(1000, moveLog.getMoves().size(), "צריכים להיות 1000 מהלכים");
        assertEquals(500, moveTableListener.getWhiteTable().getRowCount(), "500 מהלכים לבנים");
        assertEquals(500, moveTableListener.getBlackTable().getRowCount(), "500 מהלכים שחורים");
        assertEquals(100, whiteScoreTracker.getScore(), "100 נקודות (100 חיילים)");
        assertEquals(100, blackScoreTracker.getScore(), "100 נקודות (100 חיילים)");

        // בדיקת ביצועים - המערכת צריכה לעבוד תחת שנייה
        assertTrue(duration < 5000, "המערכת צריכה לעבוד תחת 5 שניות עבור 1000 אירועים");

        System.out.println("טיפול ב-1000 אירועים לקח: " + duration + " מילישניות");
    }

    @Test
    @DisplayName("בדיקת עמידות המערכת מול שגיאות")
    void testSystemResilience() {
        // Act & Assert - בדיקה שהמערכת לא קורסת מאירועים שגויים

        assertDoesNotThrow(() -> {
            // אירועים עם ערכי null
            eventBus.publish(new PieceMovedEvent(null, null, null, null, 0, null));
            eventBus.publish(new PieceCapturedEvent(null));

            // מספרי מהלכים שליליים
            eventBus.publish(new PieceMovedEvent("a1", "a2", "WHITE", "P", -1, null));

            // שחקנים לא מוכרים
            eventBus.publish(new PieceMovedEvent("a1", "a2", "YELLOW", "P", 1, null));

            // כלים לא מוכרים
            eventBus.publish(new PieceMovedEvent("a1", "a2", "WHITE", "DRAGON", 1, null));
            eventBus.publish(new PieceCapturedEvent("UNICORN"));

        }, "המערכת צריכה להיות עמידה מול נתונים שגויים");

        // המערכת צריכה להמשיך לפעול נכון אחרי שגיאות
        eventBus.publish(new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null));
        assertEquals(1, moveLog.getMoves().size(), "המערכת צריכה להמשיך לפעול אחרי שגיאות");
    }

    void tearDown() {
        System.setOut(originalOut);
    }
}
