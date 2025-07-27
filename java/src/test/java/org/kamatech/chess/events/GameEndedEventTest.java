package org.kamatech.chess.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * מבחנים עבור GameEndedEvent - אירוע סיום משחק
 */
public class GameEndedEventTest {

    @Test
    @DisplayName("יש לוודא שיצירת אירוע סיום משחק עם מנצח עובדת")
    void testGameEndedEventWithWinner() {
        // Act
        GameEndedEvent event = new GameEndedEvent("WHITE");

        // Assert
        assertEquals("WHITE", event.winner, "המנצח צריך להיות WHITE");
    }

    @Test
    @DisplayName("יש לוודא שיצירת אירוע עם מנצח שחור עובדת")
    void testGameEndedEventWithBlackWinner() {
        // Act
        GameEndedEvent event = new GameEndedEvent("BLACK");

        // Assert
        assertEquals("BLACK", event.winner, "המנצח צריך להיות BLACK");
    }

    @Test
    @DisplayName("יש לוודא שיצירת אירוע עם תיקו עובדת")
    void testGameEndedEventWithDraw() {
        // Act
        GameEndedEvent event = new GameEndedEvent("DRAW");

        // Assert
        assertEquals("DRAW", event.winner, "התוצאה צריכה להיות DRAW");
    }

    @Test
    @DisplayName("יש לוודא שהאירוע מממש את ממשק Event")
    void testImplementsEventInterface() {
        // Act
        GameEndedEvent event = new GameEndedEvent("WHITE");

        // Assert
        assertTrue(event instanceof Event, "GameEndedEvent צריך לממש את ממשק Event");
    }

    @Test
    @DisplayName("יש לוודא שניתן ליצור מספר אירועי סיום משחק")
    void testMultipleGameEndedEvents() {
        // Act
        GameEndedEvent event1 = new GameEndedEvent("WHITE");
        GameEndedEvent event2 = new GameEndedEvent("BLACK");
        GameEndedEvent event3 = new GameEndedEvent("DRAW");

        // Assert
        assertEquals("WHITE", event1.winner);
        assertEquals("BLACK", event2.winner);
        assertEquals("DRAW", event3.winner);

        assertNotSame(event1, event2, "אירועים שונים צריכים להיות עצמים נפרדים");
        assertNotSame(event2, event3, "אירועים שונים צריכים להיות עצמים נפרדים");
    }

    @Test
    @DisplayName("יש לוודא שניתן ליצור אירוע עם מנצח null")
    void testGameEndedEventWithNullWinner() {
        // Act
        GameEndedEvent event = new GameEndedEvent(null);

        // Assert
        assertNull(event.winner, "מנצח null צריך להיות נתמך");
    }

    @Test
    @DisplayName("יש לוודא שניתן ליצור אירוע עם מנצח עם שם מותאם אישית")
    void testGameEndedEventWithCustomWinner() {
        // Act
        GameEndedEvent event = new GameEndedEvent("COMPUTER");

        // Assert
        assertEquals("COMPUTER", event.winner, "שמות מנצחים מותאמים אישית צריכים להיות נתמכים");
    }

    @Test
    @DisplayName("יש לוודא שניתן ליצור אירוע עם מחרוזת ריקה כמנצח")
    void testGameEndedEventWithEmptyWinner() {
        // Act
        GameEndedEvent event = new GameEndedEvent("");

        // Assert
        assertEquals("", event.winner, "מחרוזת ריקה כמנצח צריכה להיות נתמכת");
    }
}
