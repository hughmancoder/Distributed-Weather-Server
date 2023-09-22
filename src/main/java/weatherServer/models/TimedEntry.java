package weatherServer.models;

public class TimedEntry implements Comparable<TimedEntry> {
    private String id;
    private long time;

    public TimedEntry(String id, long time) {
        this.id = id;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    @Override
    public int compareTo(TimedEntry other) {
        return Long.compare(this.time, other.time);
    }
}
