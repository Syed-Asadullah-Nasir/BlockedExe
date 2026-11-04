package logic;

import model.BlockPiece;
import model.CellPosition;
import model.GameMode;
import model.GameState;
import util.Constants;
import util.TimeFormat;

import java.util.ArrayList;
import java.util.List;

public class GameTimeTest {

    private static final long NANOS_PER_MILLI = 1000000L;
    private static final long LIMIT_MILLIS = Constants.TIME_ATTACK_SECONDS * 1000L;

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testTimeCountsWhilePlaying();
        testPauseNeverSkipsTime();
        testZeroAndNegativeTimeIgnored();
        testClassicHasNoTimeLimit();
        testTimeAttackFailsAtTheLimit();
        testTimeAttackJustBeforeTheLimit();
        testTimeAttackOverShootIsClamped();
        testTimeStopsAfterAWin();
        testTimeStopsAfterGameOver();
        testRestartResetsTime();
        testIsFinished();
        testTimeFormat();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    // ----- tests -----

    private static void testTimeCountsWhilePlaying() {
        Game game = singleCellGame(GameMode.CLASSIC);
        game.addElapsedTime(millis(1500));
        game.addElapsedTime(millis(500));
        check("time adds up while PLAYING (2000 ms)", game.getElapsedMillis() == 2000);
    }

    private static void testPauseNeverSkipsTime() {
        Game game = singleCellGame(GameMode.CLASSIC);
        game.addElapsedTime(millis(1000));
        game.pause();
        game.addElapsedTime(millis(10000));
        check("time does not advance while PAUSED", game.getElapsedMillis() == 1000);
        game.resume();
        game.addElapsedTime(millis(250));
        check("after resume the time continues from 1000 ms", game.getElapsedMillis() == 1250);
    }

    private static void testZeroAndNegativeTimeIgnored() {
        Game game = singleCellGame(GameMode.CLASSIC);
        game.addElapsedTime(0);
        game.addElapsedTime(-5000);
        check("zero and negative time are ignored", game.getElapsedMillis() == 0);
    }

    private static void testClassicHasNoTimeLimit() {
        Game game = singleCellGame(GameMode.CLASSIC);
        game.addElapsedTime(millis(10L * 60 * 60 * 1000));
        check("Classic keeps PLAYING after 10 hours", game.getState() == GameState.PLAYING);
        check("Classic has no remaining time (0)", game.getRemainingMillis() == 0);
        check("Classic is never 'time up'", !game.isTimeUp());
    }

    private static void testTimeAttackFailsAtTheLimit() {
        Game game = singleCellGame(GameMode.TIME_ATTACK);
        check("Time Attack starts with the full time left", game.getRemainingMillis() == LIMIT_MILLIS);
        game.addElapsedTime(millis(LIMIT_MILLIS));
        check("Time Attack is FAILED when the time runs out", game.getState() == GameState.FAILED);
        check("Time Attack reports time up", game.isTimeUp());
        check("remaining time is 0", game.getRemainingMillis() == 0);
    }

    private static void testTimeAttackJustBeforeTheLimit() {
        Game game = singleCellGame(GameMode.TIME_ATTACK);
        game.addElapsedTime(millis(LIMIT_MILLIS - 1));
        check("1 ms before the limit the game is still PLAYING", game.getState() == GameState.PLAYING);
        check("1 ms is left", game.getRemainingMillis() == 1);
        game.addElapsedTime(millis(1));
        check("the last millisecond ends the game", game.getState() == GameState.FAILED);
    }

    private static void testTimeAttackOverShootIsClamped() {
        Game game = singleCellGame(GameMode.TIME_ATTACK);
        game.addElapsedTime(millis(LIMIT_MILLIS + 50000));
        check("a big time step ends the game", game.getState() == GameState.FAILED);
        check("elapsed time never goes past the limit", game.getElapsedMillis() == LIMIT_MILLIS);
    }

    private static void testTimeStopsAfterAWin() {
        Game game = singleCellGame(GameMode.TIME_ATTACK);
        game.getScoreManager().addPoints(Constants.TARGET_SCORE - 1);
        game.addElapsedTime(millis(30000));
        game.tryPlace(0, 0, 0);
        check("target reached is WON", game.getState() == GameState.WON);
        game.addElapsedTime(millis(200000));
        check("time stops after a win", game.getElapsedMillis() == 30000);
        check("a win is not changed into FAILED by late time", game.getState() == GameState.WON);
    }

    private static void testTimeStopsAfterGameOver() {
        Game game = singleCellGame(GameMode.TIME_ATTACK);
        game.addElapsedTime(millis(LIMIT_MILLIS));
        game.addElapsedTime(millis(5000));
        check("time stops after the game ended", game.getElapsedMillis() == LIMIT_MILLIS);
    }

    private static void testRestartResetsTime() {
        Game game = singleCellGame(GameMode.TIME_ATTACK);
        game.addElapsedTime(millis(LIMIT_MILLIS));
        game.restart();
        check("restart after a time out is PLAYING again", game.getState() == GameState.PLAYING);
        check("restart sets the time back to 0", game.getElapsedMillis() == 0);
        check("restart gives the full time again", game.getRemainingMillis() == LIMIT_MILLIS);
        check("time is counted again after restart", addAndRead(game, 700) == 700);
    }

    private static void testIsFinished() {
        Game game = singleCellGame(GameMode.TIME_ATTACK);
        check("a running game is not finished", !game.isFinished());
        game.pause();
        check("a paused game is not finished", !game.isFinished());
        game.resume();
        game.addElapsedTime(millis(LIMIT_MILLIS));
        check("a failed game is finished", game.isFinished());
    }

    private static void testTimeFormat() {
        check("elapsed 0 ms is 00:00", TimeFormat.formatElapsed(0).equals("00:00"));
        check("elapsed 59999 ms is 00:59 (rounded down)", TimeFormat.formatElapsed(59999).equals("00:59"));
        check("elapsed 65000 ms is 01:05", TimeFormat.formatElapsed(65000).equals("01:05"));
        check("remaining 180000 ms is 03:00", TimeFormat.formatRemaining(180000).equals("03:00"));
        check("remaining 179999 ms is 03:00 (rounded up)", TimeFormat.formatRemaining(179999).equals("03:00"));
        check("remaining 1 ms is 00:01", TimeFormat.formatRemaining(1).equals("00:01"));
        check("remaining 0 ms is 00:00", TimeFormat.formatRemaining(0).equals("00:00"));
        check("negative time shows 00:00", TimeFormat.formatRemaining(-500).equals("00:00"));
    }

    // ----- helpers -----

    private static long millis(long milliseconds) {
        return milliseconds * NANOS_PER_MILLI;
    }

    private static long addAndRead(Game game, long milliseconds) {
        game.addElapsedTime(millis(milliseconds));
        return game.getElapsedMillis();
    }

    private static class FixedPieceGenerator extends PieceGenerator {

        private final BlockPiece piece;

        FixedPieceGenerator(BlockPiece piece) {
            this.piece = piece;
        }

        @Override
        public BlockPiece[] generateTray() {
            BlockPiece[] tray = new BlockPiece[Constants.TRAY_SIZE];
            for (int i = 0; i < tray.length; i++) {
                tray[i] = new BlockPiece(piece);
            }
            return tray;
        }
    }

    private static Game singleCellGame(GameMode mode) {
        List<CellPosition> cells = new ArrayList<>();
        cells.add(new CellPosition(0, 0));
        return new Game(mode, new FixedPieceGenerator(new BlockPiece(cells, 2)));
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
