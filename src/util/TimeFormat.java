package util;

// Turns milliseconds into the mm:ss text shown on screen.
public final class TimeFormat {

    private TimeFormat() {
    }

    // Time played: whole seconds, rounded down (00:00 at the start).
    public static String formatElapsed(long millis) {
        return format(Math.max(0, millis) / 1000);
    }

    // Time left: rounded up, so a countdown shows 03:00 at the start
    // and only shows 00:00 when the time is really over.
    public static String formatRemaining(long millis) {
        return format((Math.max(0, millis) + 999) / 1000);
    }

    private static String format(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
