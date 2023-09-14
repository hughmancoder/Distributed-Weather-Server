package com.utility;

public class LamportClock {
    private long time;

    public synchronized void tick() {
        time++;
    }

    public synchronized void update(long otherTime) {
        time = Math.max(time, otherTime) + 1;
    }

    public synchronized long getTime() {
        return time;
    }
}
