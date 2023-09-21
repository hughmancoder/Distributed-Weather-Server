package com.utils;

public class LamportClock {
    private long time;

    public LamportClock() {
        this.time = 0;
    }

    public synchronized void tick() {
        ++time;
    }

    public synchronized void sync(long otherTime) {
        time = Math.max(time, otherTime) + 1;
    }

    public synchronized long getTime() {
        return time;
    }
}
