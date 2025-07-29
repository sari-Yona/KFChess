package org.kamatech.chess.events;

/**
 * Event for sound playback in the chess game
 */
public class SoundEvent implements Event {

    public enum SoundType {
        MOVE, // Sound for piece movement
        JUMP, // Sound for piece jump
        EAT // Sound for piece capture
    }

    public final SoundType soundType;
    public final long timestamp;

    public SoundEvent(SoundType soundType) {
        this.soundType = soundType;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "SoundEvent{soundType=" + soundType + ", timestamp=" + timestamp + "}";
    }
}
