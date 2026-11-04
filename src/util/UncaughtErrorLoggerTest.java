package util;

import java.util.ArrayList;
import java.util.List;

public class UncaughtErrorLoggerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testErrorIsReported();
        testSameErrorOnlyOnce();
        testDifferentErrorsAreAllReported();
        testInstallSetsTheDefaultHandler();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    private static void testErrorIsReported() {
        RecordingLogger logger = new RecordingLogger();
        logger.uncaughtException(Thread.currentThread(), new IllegalStateException("boom"));
        check("an error is reported", logger.messages.size() == 1);
        String text = logger.messages.get(0);
        check("the report names the error and a stack line",
                text.contains("IllegalStateException") && text.contains("boom") && text.contains("at "));
    }

    private static void testSameErrorOnlyOnce() {
        RecordingLogger logger = new RecordingLogger();
        for (int i = 0; i < 100; i++) {
            logger.uncaughtException(Thread.currentThread(), new IllegalStateException("same"));
        }
        check("100 identical errors give one log line", logger.messages.size() == 1);
    }

    private static void testDifferentErrorsAreAllReported() {
        RecordingLogger logger = new RecordingLogger();
        logger.uncaughtException(Thread.currentThread(), new IllegalStateException("first"));
        logger.uncaughtException(Thread.currentThread(), new NullPointerException("second"));
        logger.uncaughtException(Thread.currentThread(), new IllegalStateException("first"));
        check("a different error is logged again", logger.messages.size() == 3);
    }

    private static void testInstallSetsTheDefaultHandler() {
        Thread.UncaughtExceptionHandler before = Thread.getDefaultUncaughtExceptionHandler();
        UncaughtErrorLogger.install();
        check("install sets an UncaughtErrorLogger",
                Thread.getDefaultUncaughtExceptionHandler() instanceof UncaughtErrorLogger);
        Thread.setDefaultUncaughtExceptionHandler(before);
    }

    private static class RecordingLogger extends UncaughtErrorLogger {

        private final List<String> messages = new ArrayList<>();

        @Override
        protected void report(String message) {
            messages.add(message);
        }
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }
}
