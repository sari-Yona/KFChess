package org.kamatech.chess.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import org.kamatech.chess.events.SoundEvent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * מבחנים עבור SoundPlayer - מאזין להשמעת צלילים
 */
public class SoundPlayerTest {

    private SoundPlayer soundPlayer;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        soundPlayer = new SoundPlayer();

        // הכנת הפלט לבדיקת הודעות הקונסול
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    @DisplayName("יש לוודא שיצירת SoundPlayer מדפיסה הודעת DEBUG")
    void testSoundPlayerCreation() {
        // Act
        new SoundPlayer();

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("DEBUG: SoundPlayer created"), "צריכה להיות הודעת יצירה");
    }

    @Test
    @DisplayName("יש לוודא שהמאזין מממש את ממשק EventListener")
    void testImplementsEventListener() {
        // Assert
        assertTrue(soundPlayer instanceof org.kamatech.chess.events.EventListener,
                "SoundPlayer צריך לממש את ממשק EventListener");
    }

    @Test
    @DisplayName("יש לוודא שאירוע צליל תנועה מטופל נכון")
    void testMoveSoundEvent() {
        // Act
        SoundEvent moveEvent = new SoundEvent(SoundEvent.SoundType.MOVE);
        soundPlayer.onEvent(moveEvent);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("DEBUG: Playing MOVE sound via SoundEvent"),
                "צריכה להיות הודעה על השמעת צליל תנועה");
    }

    @Test
    @DisplayName("יש לוודא שאירוע צליל אכילה מטופל נכון")
    void testEatSoundEvent() {
        // Act
        SoundEvent eatEvent = new SoundEvent(SoundEvent.SoundType.EAT);
        soundPlayer.onEvent(eatEvent);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("DEBUG: Playing EAT sound via SoundEvent"),
                "צריכה להיות הודעה על השמעת צליל אכילה");
    }

    @Test
    @DisplayName("יש לוודא שמספר אירועי צליל מטופלים בזה אחר זה")
    void testMultipleSoundEvents() {
        // Act
        soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.MOVE));
        soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.EAT));
        soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.MOVE));

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("DEBUG: Playing MOVE sound via SoundEvent"),
                "צריכה להיות הודעה על השמעת צליל תנועה");
        assertTrue(output.contains("DEBUG: Playing EAT sound via SoundEvent"),
                "צריכה להיות הודעה על השמעת צליל אכילה");

        // ספירת מספר פעמים שהופיע כל צליל
        String[] lines = output.split("\n");
        int moveCount = 0;
        int eatCount = 0;

        for (String line : lines) {
            if (line.contains("DEBUG: Playing MOVE sound via SoundEvent")) {
                moveCount++;
            } else if (line.contains("DEBUG: Playing EAT sound via SoundEvent")) {
                eatCount++;
            }
        }

        assertEquals(2, moveCount, "צריכים להיות 2 צלילי תנועה");
        assertEquals(1, eatCount, "צריך להיות 1 צליל אכילה");
    }

    @Test
    @DisplayName("יש לוודא שהמתודה onEvent לא זורקת חריגה")
    void testOnEventDoesNotThrow() {
        // Act & Assert - לא אמור לזרוק חריגה
        assertDoesNotThrow(() -> {
            soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.MOVE));
            soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.EAT));
        }, "onEvent לא צריך לזרוק חריגה");
    }

    @Test
    @DisplayName("יש לוודא שהמערכת מטפלת בהרבה אירועי צליל")
    void testManyEvents() {
        // Act
        for (int i = 0; i < 10; i++) {
            soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.MOVE));
            soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.EAT));
        }

        // Assert - לא אמור לזרוק חריגה ולעבוד נכון
        String output = outputStream.toString();
        assertTrue(output.contains("DEBUG: Playing MOVE sound via SoundEvent"));
        assertTrue(output.contains("DEBUG: Playing EAT sound via SoundEvent"));

        // ספירת מספר ההודעות
        String[] lines = output.split("\n");
        int totalDebugMessages = 0;
        for (String line : lines) {
            if (line.contains("DEBUG: Playing") && line.contains("sound via SoundEvent")) {
                totalDebugMessages++;
            }
        }

        assertEquals(20, totalDebugMessages, "צריכים להיות 20 הודעות DEBUG (10 MOVE + 10 EAT)");
    }

    void tearDown() {
        System.setOut(originalOut);
    }
}
