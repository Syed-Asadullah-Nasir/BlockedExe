package logic;

import model.GameMode;
import util.Constants;
import util.FileLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// Keeps the current score and one best score per game mode, and saves the best scores
// in a text file (data/bestscore.txt), one line per mode:
//     CLASSIC=1200
//     TIME_ATTACK=640
// A missing, empty or damaged file never crashes the game: the bad parts become 0.
public class ScoreManager {

    private int score;
    private final int[] bestScores;
    private final String filePath;
    private boolean bestScoresChanged;   // true when there is something new to save

    public ScoreManager() {
        this(Constants.BEST_SCORE_FILE);
    }

    // A different file can be used in tests.
    public ScoreManager(String filePath) {
        this.score = 0;
        this.bestScores = new int[GameMode.values().length];
        this.filePath = filePath;
        this.bestScoresChanged = false;
    }

    // ----- current score -----

    public int getScore() {
        return score;
    }

    // Negative values are ignored so the score can never go down.
    public void addPoints(int points) {
        if (points > 0) {
            score += points;
        }
    }

    public void resetScore() {
        score = 0;
    }

    // ----- best scores -----

    public int getBestScore(GameMode mode) {
        return bestScores[mode.ordinal()];
    }

    public void setBestScore(GameMode mode, int value) {
        if (value >= 0 && value != bestScores[mode.ordinal()]) {
            bestScores[mode.ordinal()] = value;
            bestScoresChanged = true;
        }
    }

    // Raises the best score of this mode if the current score beats it.
    // Returns true when a new best was set.
    public boolean updateBestScore(GameMode mode) {
        if (score > bestScores[mode.ordinal()]) {
            bestScores[mode.ordinal()] = score;
            bestScoresChanged = true;
            return true;
        }
        return false;
    }

    // True when a best score was raised and not saved yet.
    public boolean hasUnsavedBestScores() {
        return bestScoresChanged;
    }

    // ----- file handling -----

    // Reads the best scores from the file. If the file does not exist it is created with zeros.
    public void loadBestScores() {
        clearBestScores();

        File file = new File(filePath);
        if (!file.exists()) {
            FileLogger.log("Best score file not found, creating " + filePath);
            saveBestScores();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                readLine(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            FileLogger.log("Could not read " + filePath + ": " + e);
            clearBestScores();
        }
        bestScoresChanged = false;
    }

    // Writes all best scores to the file, creating the data folder if needed.
    public void saveBestScores() {
        File file = new File(filePath);
        File folder = file.getParentFile();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }

        boolean saved = false;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (GameMode mode : GameMode.values()) {
                writer.write(mode.name() + "=" + bestScores[mode.ordinal()]);
                writer.newLine();
            }
            saved = true;
        } catch (IOException e) {
            FileLogger.log("Could not save " + filePath + ": " + e);
        }

        if (saved) {
            bestScoresChanged = false;
        }
    }

    // Saves only if a best score changed, so the file is not rewritten for nothing.
    public void saveBestScoresIfChanged() {
        if (bestScoresChanged) {
            saveBestScores();
        }
    }

    private void clearBestScores() {
        for (int i = 0; i < bestScores.length; i++) {
            bestScores[i] = 0;
        }
    }

    // Understands one line like "CLASSIC=1200". Anything else is ignored with a log message.
    private void readLine(String line) {
        String text = line.trim();
        if (text.isEmpty()) {
            return;
        }

        String[] parts = text.split("=", -1);
        if (parts.length != 2) {
            FileLogger.log("Ignored invalid line in " + filePath + ": " + text);
            return;
        }

        GameMode mode = findMode(parts[0].trim());
        if (mode == null) {
            FileLogger.log("Ignored unknown game mode in " + filePath + ": " + text);
            return;
        }
        bestScores[mode.ordinal()] = parseScore(parts[1].trim(), text);
    }

    private GameMode findMode(String name) {
        for (GameMode mode : GameMode.values()) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }

    // Returns 0 (and logs a warning) if the text is not a valid score.
    private int parseScore(String text, String wholeLine) {
        try {
            int value = Integer.parseInt(text);
            if (value < 0) {
                FileLogger.log("Negative best score replaced by 0 in " + filePath + ": " + wholeLine);
                return 0;
            }
            return value;
        } catch (NumberFormatException e) {
            FileLogger.log("Invalid best score replaced by 0 in " + filePath + ": " + wholeLine);
            return 0;
        }
    }
}
