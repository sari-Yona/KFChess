package org.kamatech.chess.listeners;

import org.kamatech.chess.events.*;
import javax.sound.sampled.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sound player for chess game events - plays WAV files
 */
public class SoundPlayer implements EventListener<SoundEvent> {

    private final BlockingQueue<String> soundQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public SoundPlayer() {
        preloadAudioSystem();
        startSoundWorker();
    }

    /**
     * Pre-load the audio system to avoid delay on first sound
     */
    private void preloadAudioSystem() {
        try {
            java.io.InputStream stream = getClass().getResourceAsStream("/1.wav");
            if (stream != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(stream);
                Mixer.Info[] mixers = AudioSystem.getMixerInfo();

                for (int i = 0; i < mixers.length; i++) {
                    try {
                        Mixer mixer = AudioSystem.getMixer(mixers[i]);
                        DataLine.Info clipInfo = new DataLine.Info(Clip.class, audioStream.getFormat());
                        if (mixer.isLineSupported(clipInfo)) {
                            Clip clip = (Clip) mixer.getLine(clipInfo);
                            clip.open(audioStream);
                            clip.close();
                            break;
                        }
                    } catch (Exception e) {
                        // Continue to next mixer
                    }
                }
                audioStream.close();
            }
        } catch (Exception e) {
            // Audio pre-loading failed - continue anyway
        }
    }

    /**
     * Start the single worker thread that processes sound requests
     */
    private void startSoundWorker() {
        Thread soundWorker = new Thread(() -> {
            while (running) {
                try {
                    String fileName = soundQueue.take();
                    playWavFileSync(fileName);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    // Sound worker error - continue
                }
            }
        });
        soundWorker.setDaemon(true);
        soundWorker.setName("SoundPlayer-Worker");
        soundWorker.start();
    }

    @Override
    public void onEvent(SoundEvent event) {
        String fileName;
        switch (event.soundType) {
            case MOVE:
                fileName = "1.wav";
                break;
            case JUMP:
                fileName = "2.wav";
                break;
            case EAT:
                fileName = "3.wav";
                break;
            default:
                return;
        }

        soundQueue.clear();
        soundQueue.offer(fileName);
    }

    /**
     * Play a WAV file
     */
    private synchronized void playWavFileSync(String fileName) {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (int i = 0; i < mixers.length; i++) {
            try {
                java.io.InputStream stream = getClass().getResourceAsStream("/" + fileName);
                if (stream == null) {
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(stream);
                Mixer mixer = AudioSystem.getMixer(mixers[i]);

                DataLine.Info clipInfo = new DataLine.Info(Clip.class, audioStream.getFormat());
                if (mixer.isLineSupported(clipInfo)) {
                    Clip clip = (Clip) mixer.getLine(clipInfo);
                    clip.open(audioStream);

                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        gainControl.setValue(6.0f);
                    }

                    clip.start();
                    Thread.sleep(500);
                    clip.stop();
                    clip.close();
                    audioStream.close();
                    stream.close();
                    return;
                }

                audioStream.close();
                stream.close();

            } catch (Exception e) {
                // Continue to next mixer
            }
        }
    }

    /**
     * Clean shutdown
     */
    public void shutdown() {
        running = false;
        soundQueue.clear();
    }
}
