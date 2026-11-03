package util;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FileLogger {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FileLogger() {
    }

    // Appends one timestamped line to logs/game.log. Never throws.
    public static void log(String message) {
        try {
            File logFile = new File(Constants.LOG_FILE);
            File folder = logFile.getParentFile();
            if (folder != null && !folder.exists()) {
                folder.mkdirs();
            }
            // try-with-resources closes the writer automatically
            try (FileWriter writer = new FileWriter(logFile, true)) {
                String time = LocalDateTime.now().format(TIME_FORMAT);
                writer.write(time + " " + message + System.lineSeparator());
            }
        } catch (Exception e) {
            // If logging itself fails, there is nothing useful left to do.
        }
    }
}
