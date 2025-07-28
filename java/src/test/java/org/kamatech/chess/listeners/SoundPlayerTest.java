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
        assertTrue(output.contains("will play WAV files from resources"), "צריכה להיות הודעה על WAV files");
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
    @DisplayName("יש לוודא שאירוע צליל קפיצה מטופל נכון")
    void testJumpSoundEvent() {
        // Act
        SoundEvent jumpEvent = new SoundEvent(SoundEvent.SoundType.JUMP);
        soundPlayer.onEvent(jumpEvent);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("DEBUG: Playing JUMP sound via SoundEvent"),
                "צריכה להיות הודעה על השמעת צליל קפיצה");
    }

    @Test
    @DisplayName("יש לוודא שמספר אירועי צליל מטופלים בזה אחר זה")
    void testMultipleSoundEvents() {
        // Act
        soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.MOVE));
        soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.EAT));
        soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.JUMP));
        soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.MOVE));

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("DEBUG: Playing MOVE sound via SoundEvent"),
                "צריכה להיות הודעה על השמעת צליל תנועה");
        assertTrue(output.contains("DEBUG: Playing EAT sound via SoundEvent"),
                "צריכה להיות הודעה על השמעת צליל אכילה");
        assertTrue(output.contains("DEBUG: Playing JUMP sound via SoundEvent"),
                "צריכה להיות הודעה על השמעת צליל קפיצה");

        // ספירת מספר פעמים שהופיע כל צליל
        String[] lines = output.split("\n");
        int moveCount = 0;
        int eatCount = 0;
        int jumpCount = 0;

        for (String line : lines) {
            if (line.contains("DEBUG: Playing MOVE sound via SoundEvent")) {
                moveCount++;
            } else if (line.contains("DEBUG: Playing EAT sound via SoundEvent")) {
                eatCount++;
            } else if (line.contains("DEBUG: Playing JUMP sound via SoundEvent")) {
                jumpCount++;
            }
        }

        assertEquals(2, moveCount, "צריכים להיות 2 צלילי תנועה");
        assertEquals(1, eatCount, "צריך להיות 1 צליל אכילה");
        assertEquals(1, jumpCount, "צריך להיות 1 צליל קפיצה");
    }

    @Test
    @DisplayName("יש לוודא שהמתודה onEvent לא זורקת חריגה")
    void testOnEventDoesNotThrow() {
        // Act & Assert - לא אמור לזרוק חריגה
        assertDoesNotThrow(() -> {
            soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.MOVE));
            soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.JUMP));
            soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.EAT));
        }, "onEvent לא צריך לזרוק חריגה");
    }

    @Test
    @DisplayName("יש לוודא שהמערכת מטפלת בהרבה אירועי צליל")
    void testManyEvents() {
        // Act
        for (int i = 0; i < 5; i++) {
            soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.MOVE));
            soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.JUMP));
            soundPlayer.onEvent(new SoundEvent(SoundEvent.SoundType.EAT));
        }

        // Assert - לא אמור לזרוק חריגה ולעבוד נכון
        String output = outputStream.toString();
        assertTrue(output.contains("DEBUG: Playing MOVE sound via SoundEvent"));
        assertTrue(output.contains("DEBUG: Playing JUMP sound via SoundEvent"));
        assertTrue(output.contains("DEBUG: Playing EAT sound via SoundEvent"));

        // ספירת מספר ההודעות
        String[] lines = output.split("\n");
        int totalDebugMessages = 0;
        for (String line : lines) {
            if (line.contains("DEBUG: Playing") && line.contains("sound via SoundEvent")) {
                totalDebugMessages++;
            }
        }

        assertEquals(15, totalDebugMessages, "צריכים להיות 15 הודעות DEBUG (5 MOVE + 5 JUMP + 5 EAT)");
    }

    void tearDown() {
        System.setOut(originalOut);
    }
}
