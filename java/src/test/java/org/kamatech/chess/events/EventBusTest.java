package org.kamatech.chess.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * מבחנים עבור EventBus - המרכז הראשי של מערכת PUB/SUB
 */
public class EventBusTest {

    private EventBus eventBus;
    private TestMoveListener moveListener;
    private TestCaptureListener captureListener;
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
     * מאזין בדיקה לאירועי לכידה
     */
    static class TestCaptureListener implements EventListener<PieceCapturedEvent> {
        private final List<PieceCapturedEvent> receivedEvents = new ArrayList<>();

        @Override
        public void onEvent(PieceCapturedEvent event) {
            receivedEvents.add(event);
        }

        public List<PieceCapturedEvent> getReceivedEvents() {
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

        // יצירת מאזינים לבדיקה
        moveListener = new TestMoveListener();
        captureListener = new TestCaptureListener();
        gameStartListener = new TestGameStartListener();
        gameEndListener = new TestGameEndListener();
        soundListener = new TestSoundListener();

        // הכנת הפלט לבדיקת הודעות ה-DEBUG
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    @DisplayName("יש לוודא שהתחברות למאזין עובדת כראוי")
    void testSubscribe() {
        // Act
        eventBus.subscribe(PieceMovedEvent.class, moveListener);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Subscribed"), "אמורה להיות הודעת DEBUG על הרשמה");
        assertTrue(output.contains("PieceMovedEvent"), "אמורה להכיל את שם האירוע");
    }

    @Test
    @DisplayName("יש לוודא שאפשר להרשם למספר מאזינים לאותו אירוע")
    void testMultipleSubscribersToSameEvent() {
        // Arrange
        TestMoveListener secondMoveListener = new TestMoveListener();

        // Act
        eventBus.subscribe(PieceMovedEvent.class, moveListener);
        eventBus.subscribe(PieceMovedEvent.class, secondMoveListener);

        PieceMovedEvent event = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        eventBus.publish(event);

        // Assert
        assertEquals(1, moveListener.getReceivedEvents().size(), "המאזין הראשון צריך לקבל את האירוע");
        assertEquals(1, secondMoveListener.getReceivedEvents().size(), "המאזין השני צריך לקבל את האירוע");
        assertEquals(event, moveListener.getReceivedEvents().get(0));
        assertEquals(event, secondMoveListener.getReceivedEvents().get(0));
    }

    @Test
    @DisplayName("יש לוודא שפרסום אירוע מגיע למאזינים הנכונים")
    void testPublishToCorrectListeners() {
        // Arrange
        eventBus.subscribe(PieceMovedEvent.class, moveListener);
        eventBus.subscribe(PieceCapturedEvent.class, captureListener);

        // Act
        PieceMovedEvent moveEvent = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        eventBus.publish(moveEvent);

        // Assert
        assertEquals(1, moveListener.getReceivedEvents().size(), "מאזין התנועה צריך לקבל את האירוע");
        assertEquals(0, captureListener.getReceivedEvents().size(), "מאזין הלכידה לא צריך לקבל את האירוע");
        assertEquals(moveEvent, moveListener.getReceivedEvents().get(0));
    }

    @Test
    @DisplayName("יש לוודא שפרסום אירוע ללא מאזינים לא גורם לשגיאה")
    void testPublishWithNoListeners() {
        // Act & Assert - לא אמור לזרוק חריגה
        PieceMovedEvent event = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        assertDoesNotThrow(() -> eventBus.publish(event));
    }

    @Test
    @DisplayName("יש לוודא שכל סוגי האירועים עובדים")
    void testAllEventTypes() {
        // Arrange
        eventBus.subscribe(PieceMovedEvent.class, moveListener);
        eventBus.subscribe(PieceCapturedEvent.class, captureListener);
        eventBus.subscribe(GameStartedEvent.class, gameStartListener);
        eventBus.subscribe(GameEndedEvent.class, gameEndListener);
        eventBus.subscribe(SoundEvent.class, soundListener);

        // Act
        PieceMovedEvent moveEvent = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        PieceCapturedEvent captureEvent = new PieceCapturedEvent("Queen");
        GameStartedEvent gameStartEvent = new GameStartedEvent();
        GameEndedEvent gameEndEvent = new GameEndedEvent("WHITE");
        SoundEvent soundEvent = new SoundEvent(SoundEvent.SoundType.MOVE);

        eventBus.publish(moveEvent);
        eventBus.publish(captureEvent);
        eventBus.publish(gameStartEvent);
        eventBus.publish(gameEndEvent);
        eventBus.publish(soundEvent);

        // Assert
        assertEquals(1, moveListener.getReceivedEvents().size(), "מאזין התנועה צריך לקבל אירוע אחד");
        assertEquals(1, captureListener.getReceivedEvents().size(), "מאזין הלכידה צריך לקבל אירוע אחד");
        assertEquals(1, gameStartListener.getReceivedEvents().size(), "מאזין התחלת המשחק צריך לקבל אירוע אחד");
        assertEquals(1, gameEndListener.getReceivedEvents().size(), "מאזין סיום המשחק צריך לקבל אירוע אחד");
        assertEquals(1, soundListener.getReceivedEvents().size(), "מאזין הצליל צריך לקבל אירוע אחד");

        assertEquals(moveEvent, moveListener.getReceivedEvents().get(0));
        assertEquals(captureEvent, captureListener.getReceivedEvents().get(0));
        assertEquals(gameStartEvent, gameStartListener.getReceivedEvents().get(0));
        assertEquals(gameEndEvent, gameEndListener.getReceivedEvents().get(0));
        assertEquals(soundEvent, soundListener.getReceivedEvents().get(0));
    }

    @Test
    @DisplayName("יש לוודא שההודעות DEBUG מודפסות נכון")
    void testDebugMessages() {
        // Act
        eventBus.subscribe(PieceMovedEvent.class, moveListener);
        PieceMovedEvent event = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        eventBus.publish(event);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("DEBUG: EventBus - Subscribed"), "אמורה להיות הודעת הרשמה");
        assertTrue(output.contains("DEBUG: EventBus - Publishing"), "אמורה להיות הודעת פרסום");
        assertTrue(output.contains("DEBUG: EventBus - Calling onEvent"), "אמורה להיות הודעת קריאה למאזין");
    }

    @Test
    @DisplayName("יש לוודא שמאזין יכול לטפל במספר אירועים")
    void testListenerHandlingMultipleEvents() {
        // Arrange
        eventBus.subscribe(PieceMovedEvent.class, moveListener);

        // Act
        PieceMovedEvent event1 = new PieceMovedEvent("e2", "e4", "WHITE", "P", 1, null);
        PieceMovedEvent event2 = new PieceMovedEvent("e7", "e5", "BLACK", "P", 2, null);
        PieceMovedEvent event3 = new PieceMovedEvent("g1", "f3", "WHITE", "N", 3, null);

        eventBus.publish(event1);
        eventBus.publish(event2);
        eventBus.publish(event3);

        // Assert
        List<PieceMovedEvent> receivedEvents = moveListener.getReceivedEvents();
        assertEquals(3, receivedEvents.size(), "המאזין צריך לקבל 3 אירועים");
        assertEquals(event1, receivedEvents.get(0));
        assertEquals(event2, receivedEvents.get(1));
        assertEquals(event3, receivedEvents.get(2));
    }

    void tearDown() {
        System.setOut(originalOut);
    }
}
