package org.kamatech.chess.listeners;

import org.kamatech.chess.events.*;
import javax.sound.sampled.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sound player for chess game events - plays WAV files with AGGRESSIVE settings
 */
public class SoundPlayer implements EventListener<SoundEvent> {
    
    private final BlockingQueue<String> soundQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public SoundPlayer() {
        System.out.println("DEBUG: SoundPlayer created - AGGRESSIVE WAV playback mode");
        
        // Pre-load audio system by playing a silent test
        preloadAudioSystem();
        
        startSoundWorker();
    }
    
    /**
     * Pre-load the audio system to avoid delay on first sound
     */
    private void preloadAudioSystem() {
        try {
            System.out.println("DEBUG: Pre-loading audio system...");
            
            // Try to initialize the audio system with a very short silent test
            String testFile = "c:\\הנדסאים\\CTD25\\java\\src\\main\\resources\\1.wav";
            java.io.File wavFile = new java.io.File(testFile);
            
            if (wavFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavFile);
                Mixer.Info[] mixers = AudioSystem.getMixerInfo();
                
                for (int i = 0; i < mixers.length; i++) {
                    try {
                        Mixer mixer = AudioSystem.getMixer(mixers[i]);
                        DataLine.Info clipInfo = new DataLine.Info(Clip.class, audioStream.getFormat());
                        if (mixer.isLineSupported(clipInfo)) {
                            Clip clip = (Clip) mixer.getLine(clipInfo);
                            clip.open(audioStream);
                            // Don't actually play - just initialize
                            clip.close();
                            System.out.println("DEBUG: Audio system pre-loaded with mixer: " + mixers[i].getName());
                            break;
                        }
                    } catch (Exception e) {
                        // Continue to next mixer
                    }
                }
                audioStream.close();
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Audio pre-loading failed: " + e.getMessage());
        }
    }
    
    /**
     * Start the single worker thread that processes sound requests
     */
    private void startSoundWorker() {
        Thread soundWorker = new Thread(() -> {
            while (running) {
                try {
                    String fileName = soundQueue.take(); // Blocks until sound available
                    playWavFileSync(fileName);
                } catch (InterruptedException e) {
                    System.out.println("DEBUG: Sound worker interrupted");
                    break;
                } catch (Exception e) {
                    System.out.println("ERROR: Sound worker error: " + e.getMessage());
                }
            }
        });
        soundWorker.setDaemon(true);
        soundWorker.setName("SoundPlayer-Worker");
        soundWorker.start();
        System.out.println("DEBUG: Sound worker thread started");
    }

    @Override
    public void onEvent(SoundEvent event) {
        // Add sound to queue - only the latest sound will play
        String fileName;
        switch (event.soundType) {
            case MOVE:
                System.out.println("DEBUG: Queuing MOVE sound");
                fileName = "1.wav";
                break;
            case JUMP:
                System.out.println("DEBUG: Queuing JUMP sound");
                fileName = "2.wav";
                break;
            case EAT:
                System.out.println("DEBUG: Queuing EAT sound");
                fileName = "3.wav";
                break;
            default:
                System.out.println("DEBUG: Unknown sound type: " + event.soundType);
                return;
        }
        
        // Clear queue and add new sound (only play the latest request)
        soundQueue.clear();
        soundQueue.offer(fileName);
    }

    /**
     * Play a WAV file - clean WAV playback only
     */
    private synchronized void playWavFileSync(String fileName) {
        System.out.println("DEBUG: Playing WAV file: " + fileName);
        
        // Try EVERY available mixer until one works
        String wavPath = "c:\\הנדסאים\\CTD25\\java\\src\\main\\resources\\" + fileName;
        java.io.File wavFile = new java.io.File(wavPath);
        
        if (!wavFile.exists()) {
            System.out.println("ERROR: WAV file does not exist at: " + wavPath);
            return;
        }
        
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        System.out.println("DEBUG: Trying ALL " + mixers.length + " mixers for " + fileName);
        
        for (int i = 0; i < mixers.length; i++) {
            try {
                System.out.println("DEBUG: Trying mixer " + i + ": " + mixers[i].getName());
                
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavFile);
                Mixer mixer = AudioSystem.getMixer(mixers[i]);
                
                DataLine.Info clipInfo = new DataLine.Info(Clip.class, audioStream.getFormat());
                if (mixer.isLineSupported(clipInfo)) {
                    Clip clip = (Clip) mixer.getLine(clipInfo);
                    clip.open(audioStream);
                    
                    // FORCE VOLUME TO MAXIMUM
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        gainControl.setValue(6.0f); // VERY LOUD
                    }
                    
                    clip.start();
                    System.out.println("DEBUG: SUCCESS! Playing with mixer " + i + ": " + mixers[i].getName());
                    
                    // Wait for short time
                    Thread.sleep(500);
                    
                    clip.stop();
                    clip.close();
                    audioStream.close();
                    
                    System.out.println("DEBUG: Successfully played " + fileName + " with mixer " + i);
                    return; // SUCCESS - stop trying other mixers
                }
                
                audioStream.close();
                
            } catch (Exception e) {
                System.out.println("DEBUG: Mixer " + i + " failed: " + e.getMessage());
                // Continue to next mixer
            }
        }
        
        System.out.println("ERROR: ALL MIXERS FAILED for " + fileName);
    }
    
    /**
     * Clean shutdown
     */
    public void shutdown() {
        running = false;
        soundQueue.clear();
    }
}
