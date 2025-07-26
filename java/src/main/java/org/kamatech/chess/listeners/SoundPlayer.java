package org.kamatech.chess.listeners;

import org.kamatech.chess.events.*;
import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Sound player for chess game events
 */
public class SoundPlayer implements EventListener<SoundEvent> {

    public SoundPlayer() {
        // Initialize sound player
        System.out.println("DEBUG: SoundPlayer created");
    }

    @Override
    public void onEvent(SoundEvent event) {
        // Play sound based on the sound type
        switch (event.soundType) {
            case MOVE:
                System.out.println("DEBUG: Playing MOVE sound via SoundEvent");
                generateMoveSound();
                break;
            case EAT:
                System.out.println("DEBUG: Playing EAT sound via SoundEvent");
                generateEatSound();
                break;
            default:
                System.out.println("DEBUG: Unknown sound type: " + event.soundType);
                break;
        }
    }

    /**
     * Generate a soft "click" sound for piece moves
     */
    private void generateMoveSound() {
        new Thread(() -> {
            try {
                // Generate a soft click sound programmatically
                int sampleRate = 44100;
                int duration = 150; // milliseconds
                int samples = (int) (sampleRate * duration / 1000.0);

                byte[] buffer = new byte[samples * 2]; // 16-bit samples

                // Generate a soft click sound
                for (int i = 0; i < samples; i++) {
                    double time = (double) i / sampleRate;
                    // Soft click with quick decay
                    double amplitude = Math.exp(-time * 15) * 0.3;
                    short sample = (short) (amplitude * Short.MAX_VALUE * Math.sin(2 * Math.PI * 800 * time));

                    buffer[i * 2] = (byte) (sample & 0xFF);
                    buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
                }

                playGeneratedSound(buffer, sampleRate);
                System.out.println("DEBUG: Move sound generated and played successfully");

            } catch (Exception e) {
                System.out.println("ERROR: Failed to generate move sound: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Generate a "thud" sound for piece captures
     */
    private void generateEatSound() {
        new Thread(() -> {
            try {
                // Generate a thud sound programmatically
                int sampleRate = 44100;
                int duration = 300; // milliseconds
                int samples = (int) (sampleRate * duration / 1000.0);

                byte[] buffer = new byte[samples * 2]; // 16-bit samples

                // Generate a thud sound
                for (int i = 0; i < samples; i++) {
                    double time = (double) i / sampleRate;
                    // Lower frequency thud with slower decay
                    double amplitude = Math.exp(-time * 8) * 0.5;
                    double freq = 200 + (100 * Math.exp(-time * 10)); // Frequency drops quickly
                    short sample = (short) (amplitude * Short.MAX_VALUE * Math.sin(2 * Math.PI * freq * time));

                    buffer[i * 2] = (byte) (sample & 0xFF);
                    buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
                }

                playGeneratedSound(buffer, sampleRate);
                System.out.println("DEBUG: Eat sound generated and played successfully");

            } catch (Exception e) {
                System.out.println("ERROR: Failed to generate eat sound: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Play a generated sound from byte array
     */
    private void playGeneratedSound(byte[] audioData, int sampleRate) throws Exception {
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);

        try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(audioData);
                AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioData.length / 2)) {

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            // Wait for sound to finish
            Thread.sleep(500);
            clip.close();
        }
    }

    /**
     * Play system sound for moves
     */
    private void playSystemSound() {
        new Thread(() -> {
            try {
                java.awt.Toolkit.getDefaultToolkit().beep();
                System.out.println("DEBUG: System beep played successfully");
            } catch (Exception e) {
                System.out.println("ERROR: Failed to play system sound: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Play different system sound for captures
     */
    private void playSystemBeep() {
        new Thread(() -> {
            try {
                // Double beep for capture
                java.awt.Toolkit.getDefaultToolkit().beep();
                Thread.sleep(100);
                java.awt.Toolkit.getDefaultToolkit().beep();
                System.out.println("DEBUG: Double beep played successfully");
            } catch (Exception e) {
                System.out.println("ERROR: Failed to play capture sound: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Play a sound file from resources
     */
    private void playSound(String soundFileName) {
        System.out.println("DEBUG: Attempting to play sound: " + soundFileName);

        new Thread(() -> {
            try {
                // Load sound file from resources
                InputStream audioStream = getClass().getClassLoader().getResourceAsStream(soundFileName);
                if (audioStream == null) {
                    System.out.println("ERROR: Sound file not found in resources: " + soundFileName);
                    return;
                }

                System.out.println("DEBUG: Sound file loaded successfully: " + soundFileName);

                // For WAV files, AudioInputStream should work perfectly
                // WAV is natively supported by Java's AudioSystem
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioStream);

                System.out.println("DEBUG: AudioInputStream created for: " + soundFileName);

                // Get audio format and create clip
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);

                System.out.println("DEBUG: Clip opened and ready to play: " + soundFileName);

                // Play the sound
                clip.start();

                System.out.println("DEBUG: Sound started playing: " + soundFileName);

                // Clean up when done
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        System.out.println("DEBUG: Sound finished playing: " + soundFileName);
                        clip.close();
                    }
                });

            } catch (UnsupportedAudioFileException e) {
                System.out.println(
                        "ERROR: Unsupported audio format: " + soundFileName + " (Error: " + e.getMessage() + ")");
            } catch (IOException e) {
                System.out.println(
                        "ERROR: IO error loading sound file: " + soundFileName + " (Error: " + e.getMessage() + ")");
            } catch (LineUnavailableException e) {
                System.out.println(
                        "ERROR: Audio line unavailable for: " + soundFileName + " (Error: " + e.getMessage() + ")");
            } catch (Exception e) {
                System.out.println(
                        "ERROR: Unexpected error playing sound: " + soundFileName + " (Error: " + e.getMessage() + ")");
            }
        }).start();
    }
}
