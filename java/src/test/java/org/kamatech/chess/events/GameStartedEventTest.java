package org.kamatech.chess.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * מבחנים עבור GameStartedEvent - אירוע התחלת משחק
 */
public class GameStartedEventTest {

    @Test
    @DisplayName("יש לוודא שיצירת אירוע התחלת משחק עובדת")
    void testGameStartedEventCreation() {
        // Act
        GameStartedEvent event = new GameStartedEvent();

        // Assert
        assertNotNull(event, "האירוע צריך להיות לא null");
    }

    @Test
    @DisplayName("יש לוודא שהאירוע מממש את ממשק Event")
    void testImplementsEventInterface() {
        // Act
        GameStartedEvent event = new GameStartedEvent();

        // Assert
        assertTrue(event instanceof Event, "GameStartedEvent צריך לממש את ממשק Event");
    }

    @Test
    @DisplayName("יש לוודא שניתן ליצור מספר אירועי התחלת משחק")
    void testMultipleGameStartedEvents() {
        // Act
        GameStartedEvent event1 = new GameStartedEvent();
        GameStartedEvent event2 = new GameStartedEvent();

        // Assert
        assertNotNull(event1);
        assertNotNull(event2);
        assertNotSame(event1, event2, "אירועים שונים צריכים להיות עצמים נפרדים");
    }

    @Test
    @DisplayName("יש לוודא שהאירוע הוא פשוט ללא מאפיינים נוספים")
    void testEventSimplicity() {
        // Act
        GameStartedEvent event = new GameStartedEvent();

        // Assert - האירוע צריך להיות פשוט ולא לזרוק חריגות
        assertDoesNotThrow(() -> {
            event.toString(); // בדיקה שלא זורקת חריגה
            event.hashCode(); // בדיקה שלא זורקת חריגה
            event.equals(new GameStartedEvent()); // בדיקה שלא זורקת חריגה
        });
    }
}
