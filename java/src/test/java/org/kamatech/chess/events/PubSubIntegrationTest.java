package org.kamatech.chess.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import org.kamatech.chess.listeners.ScoreTracker;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * מבחני אינטגרציה למערכת PUB/SUB השלמה
 */
public class PubSubIntegrationTest {

    private EventBus eventBus;
    private ScoreTracker scoreTracker;
    private TestMoveListener moveListener;
    private TestGameStartListener gameStartListener;
    private TestGameEndListener gameEndListener;
    private TestSoundListener soundListener;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    /**
     * מאזין בדיקה לאירועי תנועה
     */
    static class TestMoveListener implements EventListener<PieceMovedEvent> {
        private final List<PieceMovedEvent> receivedEvents = new ArrayList<>();

        @Override
        public void onEvent(PieceMovedEvent event) {
            receivedEvents.add(event);
        }

        public List<PieceMovedEvent> getReceivedEvents() {
            return receivedEvents;
        }

        public void clear() {
            receivedEvents.clear();
        }
    }

    /**
     * מאזין בדיקה לאירועי התחלת משחק
     */
    static class TestGameStartListener implements EventListener<GameStartedEvent> {
        private final List<GameStartedEvent> receivedEvents = new ArrayList<>();

        @Override
        public void onEvent(GameStartedEvent event) {
            receivedEvents.add(event);
        }

        public List<GameStartedEvent> getReceivedEvents() {
            return receivedEvents;
        }

        public void clear() {
            receivedEvents.clear();
        }
    }

    /**
     * מאזין בדיקה לאירועי סיום משחק
     */
    static class TestGameEndListener implements EventListener<GameEndedEvent> {
        private final List<GameEndedEvent> receivedEvents = new ArrayList<>();

        @Override
        public void onEvent(GameEndedEvent event) {
            receivedEvents.add(event);
        }

        public List<GameEndedEvent> getReceivedEvents() {
            return receivedEvents;
        }

        public void clear() {
            receivedEvents.clear();
        }
    }

    /**
     * מאזין בדיקה לאירועי צליל
     */
    static class TestSoundListener implements EventListener<SoundEvent> {
        private final List<SoundEvent> receivedEvents = new ArrayList<>();

        @Override
        public void onEvent(SoundEvent event) {
            receivedEvents.add(event);
        }

        public List<SoundEvent> getReceivedEvents() {
            return receivedEvents;
        }

        public void clear() {
            receivedEvents.clear();
        }
    }

    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
        scoreTracker = new ScoreTracker();
        moveListener = new TestMoveListener();
        gameStartListener = new TestGameStartListener();
        gameEndListener = new TestGameEndListener();
        soundListener = new TestSoundListener();

        // הכנת הפלט לבדיקת הודעות הקונסול
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    @DisplayName("יש לוודא שמשחק שלם עובד עם מערכת PUB/SUB")
    void testCompleteGameScenario() {
        // Arrange - הרשמת כל המאזינים
        eventBus.subscribe(PieceMovedEvent.class, moveListener);
        eventBus.subscribe(PieceCapturedEvent.class, scoreTracker);
        eventBus.subscribe(GameStartedEvent.class, gameStartListener);
        eventBus.subscribe(GameEndedEvent.class, gameEndListener);
        eventBus.subscribe(SoundEvent.class, soundListener);

        // Act - סימולציה של משחק
        // התחלת משחק
        eventBus.publish(new GameStartedEvent());

        // מהלכים ראשונים
        eventBus.publish(new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.MOVE));

        eventBus.publish(new PieceMovedEvent("e7", "e5", "BLACK", "P", 2, null));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.MOVE));

        // לכידת כלי
        eventBus.publish(new PieceMovedEvent("d1", "h5", "WHITE", "Q", 3, "Pawn"));
        eventBus.publish(new PieceCapturedEvent("Pawn"));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.EAT));

        // סיום משחק
        eventBus.publish(new GameEndedEvent("WHITE"));

        // Assert
        // בדיקת מאזין התנועות
        assertEquals(3, moveListener.getReceivedEvents().size(), "צריכים להיות 3 אירועי תנועה");
        assertEquals("e2", moveListener.getReceivedEvents().get(0).from);
        assertEquals("e4", moveListener.getReceivedEvents().get(0).to);

        // בדיקת עוקב הניקוד
        assertEquals(1, scoreTracker.getScore(), "הניקוד צריך להיות 1 (חייל)");

        // בדיקת מאזיני המשחק
        assertEquals(1, gameStartListener.getReceivedEvents().size(), "צריך להיות אירוע התחלת משחק");
        assertEquals(1, gameEndListener.getReceivedEvents().size(), "צריך להיות אירוע סיום משחק");
        assertEquals("WHITE", gameEndListener.getReceivedEvents().get(0).winner);

        // בדיקת מאזין הצלילים
        assertEquals(3, soundListener.getReceivedEvents().size(), "צריכים להיות 3 אירועי צליל");
        assertEquals(SoundEvent.SoundType.MOVE, soundListener.getReceivedEvents().get(0).soundType);
        assertEquals(SoundEvent.SoundType.MOVE, soundListener.getReceivedEvents().get(1).soundType);
        assertEquals(SoundEvent.SoundType.EAT, soundListener.getReceivedEvents().get(2).soundType);
    }

    @Test
    @DisplayName("יש לוודא שמספר מאזינים לאותו אירוע עובדים")
    void testMultipleListenersToSameEvent() {
        // Arrange
        TestMoveListener secondMoveListener = new TestMoveListener();

        eventBus.subscribe(PieceMovedEvent.class, moveListener);
        eventBus.subscribe(PieceMovedEvent.class, secondMoveListener);

        // Act
        PieceMovedEvent event = new PieceMovedEvent("a1", "a2", "WHITE", "R", 1, null);
        eventBus.publish(event);

        // Assert
        assertEquals(1, moveListener.getReceivedEvents().size());
        assertEquals(1, secondMoveListener.getReceivedEvents().size());
        assertEquals(event, moveListener.getReceivedEvents().get(0));
        assertEquals(event, secondMoveListener.getReceivedEvents().get(0));
    }

    @Test
    @DisplayName("יש לוודא שניתוק מאזין עובד (אם יש כזו פונקציונליות)")
    void testEventBusWithoutUnsubscribe() {
        // מכיוון שאין unsubscribe ב-EventBus, נבדוק שמאזינים פועלים רק אם הם רשומים

        // Act
        PieceMovedEvent event = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        eventBus.publish(event); // פרסום ללא מאזינים רשומים

        // Assert
        assertEquals(0, moveListener.getReceivedEvents().size(), "ללא הרשמה לא צריכים להתקבל אירועים");

        // הרשמה ובדיקה שהאירוע הבא מתקבל
        eventBus.subscribe(PieceMovedEvent.class, moveListener);
        eventBus.publish(event);

        assertEquals(1, moveListener.getReceivedEvents().size(), "אחרי הרשמה האירוע צריך להתקבל");
    }

    @Test
    @DisplayName("יש לוודא שמערכת עובדת תחת עומס")
    void testSystemUnderLoad() {
        // Arrange
        eventBus.subscribe(PieceMovedEvent.class, moveListener);
        eventBus.subscribe(PieceCapturedEvent.class, scoreTracker);

        // Act - פרסום הרבה אירועים
        for (int i = 1; i <= 100; i++) {
            eventBus.publish(new PieceMovedEvent("a" + (i % 8 + 1), "b" + (i % 8 + 1),
                    i % 2 == 0 ? "WHITE" : "BLACK", "P", i, null));
        }

        for (int i = 1; i <= 50; i++) {
            eventBus.publish(new PieceCapturedEvent("Pawn"));
        }

        // Assert
        assertEquals(100, moveListener.getReceivedEvents().size(), "צריכים להתקבל 100 אירועי תנועה");
        assertEquals(50, scoreTracker.getScore(), "הניקוד צריך להיות 50");
    }

    @Test
    @DisplayName("יש לוודא שכל סוגי האירועים עובדים יחד")
    void testAllEventTypesTogether() {
        // Arrange
        eventBus.subscribe(PieceMovedEvent.class, moveListener);
        eventBus.subscribe(PieceCapturedEvent.class, scoreTracker);
        eventBus.subscribe(GameStartedEvent.class, gameStartListener);
        eventBus.subscribe(GameEndedEvent.class, gameEndListener);
        eventBus.subscribe(SoundEvent.class, soundListener);

        // Act
        eventBus.publish(new GameStartedEvent());
        eventBus.publish(new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null));
        eventBus.publish(new PieceCapturedEvent("Queen"));
        eventBus.publish(new SoundEvent(SoundEvent.SoundType.MOVE));
        eventBus.publish(new GameEndedEvent("BLACK"));

        // Assert
        assertEquals(1, gameStartListener.getReceivedEvents().size());
        assertEquals(1, moveListener.getReceivedEvents().size());
        assertEquals(9, scoreTracker.getScore()); // Queen = 9 points
        assertEquals(1, soundListener.getReceivedEvents().size());
        assertEquals(1, gameEndListener.getReceivedEvents().size());
    }

    void tearDown() {
        System.setOut(originalOut);
    }
}
