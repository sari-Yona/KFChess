package org.kamatech.chess.events;

public class SoundEvent implements Event {
    public enum SoundType {
        MOVE, // תנועה רגילה
        EAT // אכילת כלי
    }

    public final SoundType soundType;
    public final long timestamp;

    public SoundEvent(SoundType soundType) {
        this.soundType = soundType;
        this.timestamp = System.currentTimeMillis();
    }
}
