package weatherServer.utils;

import java.net.HttpURLConnection;

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

    public synchronized void syncFromHttpResponse(HttpURLConnection conn) {
        String lamportHeader = conn.getHeaderField("X-Lamport-Clock");
        if (lamportHeader != null) {
            sync(Long.parseLong(lamportHeader));
        }
    }
}
