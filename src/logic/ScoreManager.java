package logic;

import model.GameMode;

// Keeps the current score and one best score per game mode.
// Saving and loading the best scores from a file is added in Phase 10.
public class ScoreManager {

    private int score;
    private final int[] bestScores;

    public ScoreManager() {
        score = 0;
        bestScores = new int[GameMode.values().length];
    }

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

    public int getBestScore(GameMode mode) {
        return bestScores[mode.ordinal()];
    }

    // Used when best scores are loaded from the file.
    public void setBestScore(GameMode mode, int value) {
        if (value >= 0) {
            bestScores[mode.ordinal()] = value;
        }
    }

    // Raises the best score of this mode if the current score beats it.
    // Returns true when a new best was set.
    public boolean updateBestScore(GameMode mode) {
        if (score > bestScores[mode.ordinal()]) {
            bestScores[mode.ordinal()] = score;
            return true;
        }
        return false;
    }
}
