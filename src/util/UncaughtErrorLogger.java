package util;

// The last safety net. If some bug throws an exception that nothing catches, Java would only
// print it to the console and the player would never know. This writes it to logs/game.log
// instead, so the team can find the bug afterwards.
// A repeating bug (the game timer fires 60 times a second) is written only once, not 60 times.
public class UncaughtErrorLogger implements Thread.UncaughtExceptionHandler {

    private static final int STACK_LINES_TO_LOG = 4;

    private String lastError = "";

    // Call once at program start.
    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtErrorLogger());
    }

    @Override
    public void uncaughtException(Thread thread, Throwable error) {
        String description = error.toString();
        if (description.equals(lastError)) {
            return;   // the same error again: already logged
        }
        lastError = description;

        StringBuilder message = new StringBuilder("Unexpected error in thread ");
        message.append(thread.getName()).append(": ").append(description);
        StackTraceElement[] stack = error.getStackTrace();
        for (int i = 0; i < stack.length && i < STACK_LINES_TO_LOG; i++) {
            message.append(" | at ").append(stack[i]);
        }
        report(message.toString());
    }

    // Protected so a test can catch the message instead of writing the real log file.
    protected void report(String message) {
        FileLogger.log(message);
    }
}
