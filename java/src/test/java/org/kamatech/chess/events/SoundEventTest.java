package org.kamatech.chess.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * מבחנים עבור SoundEvent - אירוע השמעת צליל
 */
public class SoundEventTest {

    @Test
    @DisplayName("יש לוודא שיצירת אירוע צליל תנועה עובדת")
    void testMoveSound() {
        // Act
        SoundEvent event = new SoundEvent(SoundEvent.SoundType.MOVE);

        // Assert
        assertEquals(SoundEvent.SoundType.MOVE, event.soundType, "סוג הצליל צריך להיות MOVE");
        assertTrue(event.timestamp > 0, "הזמן צריך להיות מוגדר");
        assertTrue(event.timestamp <= System.currentTimeMillis(), "הזמן צריך להיות סביר");
    }

    @Test
    @DisplayName("יש לוודא שיצירת אירוע צליל אכילה עובדת")
    void testEatSound() {
        // Act
        SoundEvent event = new SoundEvent(SoundEvent.SoundType.EAT);

        // Assert
        assertEquals(SoundEvent.SoundType.EAT, event.soundType, "סוג הצליל צריך להיות EAT");
        assertTrue(event.timestamp > 0, "הזמן צריך להיות מוגדר");
    }

    @Test
    @DisplayName("יש לוודא שיצירת אירוע צליל קפיצה עובדת")
    void testJumpSound() {
        // Act
        SoundEvent event = new SoundEvent(SoundEvent.SoundType.JUMP);

        // Assert
        assertEquals(SoundEvent.SoundType.JUMP, event.soundType, "סוג הצליל צריך להיות JUMP");
        assertTrue(event.timestamp > 0, "הזמן צריך להיות מוגדר");
    }

    @Test
    @DisplayName("יש לוודא שזמני יצירה שונים לאירועים שונים")
    void testTimestampDifferences() throws InterruptedException {
        // Act
        SoundEvent event1 = new SoundEvent(SoundEvent.SoundType.MOVE);
        Thread.sleep(1); // המתנה קצרה כדי לוודא זמנים שונים
        SoundEvent event2 = new SoundEvent(SoundEvent.SoundType.EAT);

        // Assert
        assertTrue(event2.timestamp >= event1.timestamp, "האירוע השני צריך להיות מאוחר יותר או בו זמנית");
    }

    @Test
    @DisplayName("יש לוודא שהאירוע מממש את ממשק Event")
    void testImplementsEventInterface() {
        // Act
        SoundEvent event = new SoundEvent(SoundEvent.SoundType.MOVE);

        // Assert
        assertTrue(event instanceof Event, "SoundEvent צריך לממש את ממשק Event");
    }

    @Test
    @DisplayName("יש לוודא שמתודת toString עובדת נכון")
    void testToStringMethod() {
        // Act
        SoundEvent moveEvent = new SoundEvent(SoundEvent.SoundType.MOVE);
        SoundEvent eatEvent = new SoundEvent(SoundEvent.SoundType.EAT);

        String moveString = moveEvent.toString();
        String eatString = eatEvent.toString();

        // Assert
        assertTrue(moveString.contains("MOVE"), "toString של MOVE צריך להכיל את הטקסט MOVE");
        assertTrue(moveString.contains("SoundEvent"), "toString צריך להכיל את שם הקלאס");
        assertTrue(moveString.contains("timestamp"), "toString צריך להכיל timestamp");

        assertTrue(eatString.contains("EAT"), "toString של EAT צריך להכיל את הטקסט EAT");
        assertTrue(eatString.contains("SoundEvent"), "toString צריך להכיל את שם הקלאס");
    }

    @Test
    @DisplayName("יש לוודא ששלושת סוגי הצלילים זמינים")
    void testAllSoundTypesAvailable() {
        // Act & Assert
        assertNotNull(SoundEvent.SoundType.MOVE, "סוג צליל MOVE צריך להיות זמין");
        assertNotNull(SoundEvent.SoundType.JUMP, "סוג צליל JUMP צריך להיות זמין");
        assertNotNull(SoundEvent.SoundType.EAT, "סוג צליל EAT צריך להיות זמין");

        // וודא שהם שונים
        assertNotEquals(SoundEvent.SoundType.MOVE, SoundEvent.SoundType.EAT, "סוגי הצלילים צריכים להיות שונים");
        assertNotEquals(SoundEvent.SoundType.MOVE, SoundEvent.SoundType.JUMP, "סוגי הצלילים צריכים להיות שונים");
        assertNotEquals(SoundEvent.SoundType.JUMP, SoundEvent.SoundType.EAT, "סוגי הצלילים צריכים להיות שונים");
    }

    @Test
    @DisplayName("יש לוודא שניתן ליצור מספר אירועי צליל")
    void testMultipleSoundEvents() {
        // Act
        SoundEvent event1 = new SoundEvent(SoundEvent.SoundType.MOVE);
        SoundEvent event2 = new SoundEvent(SoundEvent.SoundType.EAT);
        SoundEvent event3 = new SoundEvent(SoundEvent.SoundType.MOVE);
        SoundEvent event4 = new SoundEvent(SoundEvent.SoundType.JUMP);

        // Assert
        assertEquals(SoundEvent.SoundType.MOVE, event1.soundType);
        assertEquals(SoundEvent.SoundType.EAT, event2.soundType);
        assertEquals(SoundEvent.SoundType.MOVE, event3.soundType);
        assertEquals(SoundEvent.SoundType.JUMP, event4.soundType);

        // וודא שהם אירועים נפרדים
        assertNotSame(event1, event2, "אירועים שונים צריכים להיות עצמים נפרדים");
        assertNotSame(event1, event3, "גם אירועים מאותו סוג צריכים להיות עצמים נפרדים");
        assertNotSame(event1, event4, "אירועי MOVE ו-JUMP צריכים להיות עצמים נפרדים");
    }

    @Test
    @DisplayName("יש לוודא שכל ערכי ה-enum של SoundType עובדים")
    void testAllSoundTypeValues() {
        // Act & Assert
        for (SoundEvent.SoundType soundType : SoundEvent.SoundType.values()) {
            SoundEvent event = new SoundEvent(soundType);
            assertEquals(soundType, event.soundType, "כל ערכי SoundType צריכים לעבוד: " + soundType);
        }
    }
}
