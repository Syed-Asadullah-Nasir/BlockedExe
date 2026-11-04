package logic;

import model.BlockPiece;
import model.CellPosition;
import model.GameMode;
import model.GameState;
import util.Constants;

import java.util.ArrayList;
import java.util.List;

// Tests how Game and the shared ScoreManager work together. It never touches a file.
public class GameBestScoreTest {

    private static final long LIMIT_NANOS = Constants.TIME_ATTACK_SECONDS * 1000L * 1000000L;

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testBestScoreSurvivesTheNextGame();
        testNewGameStartsAtZero();
        testNewBestFlag();
        testNoNewBestWithoutPoints();
        testLowerScoreIsNoNewBest();
        testCommitBestScoreForAnUnfinishedGame();
        testRestartKeepsBestAndClearsFlag();
        testModesAreIndependent();
        testGameNeedsAScoreManager();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    // ----- tests -----

    private static void testBestScoreSurvivesTheNextGame() {
        ScoreManager shared = new ScoreManager("unused.txt");
        Game first = timeAttackGame(shared);
        first.tryPlace(0, 0, 0);
        first.addElapsedTime(LIMIT_NANOS);
        check("first game ends with best score 1", first.getState() == GameState.FAILED && first.getBestScore() == 1);

        Game second = timeAttackGame(shared);
        check("the next game still knows the best score", second.getBestScore() == 1);
    }

    private static void testNewGameStartsAtZero() {
        ScoreManager shared = new ScoreManager("unused.txt");
        Game first = timeAttackGame(shared);
        first.tryPlace(0, 0, 0);
        check("score is 1 after one piece", first.getScore() == 1);

        Game second = timeAttackGame(shared);
        check("a new game on the same ScoreManager starts at 0 points", second.getScore() == 0);
    }

    private static void testNewBestFlag() {
        ScoreManager shared = new ScoreManager("unused.txt");
        Game game = timeAttackGame(shared);
        check("flag is false at the start", !game.isNewBestReached());
        game.tryPlace(0, 0, 0);
        check("flag is still false while playing", !game.isNewBestReached());
        game.addElapsedTime(LIMIT_NANOS);
        check("ending with a higher score sets the new best flag", game.isNewBestReached());
        check("the best score is the final score", game.getBestScore() == 1);
    }

    private static void testNoNewBestWithoutPoints() {
        ScoreManager shared = new ScoreManager("unused.txt");
        Game game = timeAttackGame(shared);
        game.addElapsedTime(LIMIT_NANOS);
        check("0 points is never a new best", !game.isNewBestReached() && game.getBestScore() == 0);
    }

    private static void testLowerScoreIsNoNewBest() {
        ScoreManager shared = new ScoreManager("unused.txt");
        shared.setBestScore(GameMode.TIME_ATTACK, 50);
        Game game = timeAttackGame(shared);
        game.tryPlace(0, 0, 0);
        game.addElapsedTime(LIMIT_NANOS);
        check("1 point does not beat 50", !game.isNewBestReached());
        check("the best score stays 50", game.getBestScore() == 50);
    }

    private static void testCommitBestScoreForAnUnfinishedGame() {
        ScoreManager shared = new ScoreManager("unused.txt");
        Game game = timeAttackGame(shared);
        game.tryPlace(0, 0, 0);
        check("an unfinished game has not changed the best score yet", game.getBestScore() == 0);
        check("committing returns true for a better score", game.commitBestScore());
        check("committing stores the score as best", game.getBestScore() == 1);
        check("committing the same score again returns false", !game.commitBestScore());
        check("the game keeps playing after a commit", game.getState() == GameState.PLAYING);
        check("a commit marks the best score as unsaved", shared.hasUnsavedBestScores());
    }

    private static void testRestartKeepsBestAndClearsFlag() {
        ScoreManager shared = new ScoreManager("unused.txt");
        Game game = timeAttackGame(shared);
        game.tryPlace(0, 0, 0);
        game.addElapsedTime(LIMIT_NANOS);
        game.restart();
        check("restart keeps the best score", game.getBestScore() == 1);
        check("restart clears the new best flag", !game.isNewBestReached());
        check("restart sets the score to 0", game.getScore() == 0);
    }

    private static void testModesAreIndependent() {
        ScoreManager shared = new ScoreManager("unused.txt");
        Game timeAttack = timeAttackGame(shared);
        timeAttack.tryPlace(0, 0, 0);
        timeAttack.addElapsedTime(LIMIT_NANOS);

        Game classic = new Game(GameMode.CLASSIC, new FixedPieceGenerator(), shared);
        check("a Time Attack best does not change the Classic best", classic.getBestScore() == 0);
    }

    private static void testGameNeedsAScoreManager() {
        boolean rejected = false;
        try {
            new Game(GameMode.CLASSIC, new FixedPieceGenerator(), null);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check("a game without a ScoreManager is rejected", rejected);
    }

    // ----- helpers -----

    private static Game timeAttackGame(ScoreManager shared) {
        return new Game(GameMode.TIME_ATTACK, new FixedPieceGenerator(), shared);
    }

    private static class FixedPieceGenerator extends PieceGenerator {

        @Override
        public BlockPiece[] generateTray() {
            List<CellPosition> cells = new ArrayList<>();
            cells.add(new CellPosition(0, 0));
            BlockPiece[] tray = new BlockPiece[Constants.TRAY_SIZE];
            for (int i = 0; i < tray.length; i++) {
                tray[i] = new BlockPiece(cells, 2);
            }
            return tray;
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
