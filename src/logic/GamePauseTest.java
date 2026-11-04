package logic;

import model.BlockPiece;
import model.CellPosition;
import model.GameMode;
import model.GameState;
import util.Constants;

import java.util.ArrayList;
import java.util.List;

public class GamePauseTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testPauseBlocksPlacement();
        testResumeAllowsPlacement();
        testPauseAndResumeOnlyInTheRightState();
        testPauseDoesNothingAfterTheGameEnded();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    private static void testPauseBlocksPlacement() {
        Game game = singleCellGame(GameMode.CLASSIC);
        game.pause();
        check("pause sets PAUSED", game.getState() == GameState.PAUSED);

        PlacementResult result = game.tryPlace(0, 0, 0);
        check("placing while paused is rejected", !result.isPlaced());
        check("board stays empty while paused", game.getBoard().isEmpty());
        check("piece stays in its slot while paused", game.getPiece(0) != null);
        check("score stays 0 while paused", game.getScore() == 0);
    }

    private static void testResumeAllowsPlacement() {
        Game game = singleCellGame(GameMode.CLASSIC);
        game.pause();
        game.resume();
        check("resume sets PLAYING", game.getState() == GameState.PLAYING);
        check("placing works again after resume", game.tryPlace(0, 0, 0).isPlaced());
    }

    private static void testPauseAndResumeOnlyInTheRightState() {
        Game game = singleCellGame(GameMode.CLASSIC);
        game.resume();
        check("resume while PLAYING changes nothing", game.getState() == GameState.PLAYING);
        game.pause();
        game.pause();
        check("pausing twice stays PAUSED", game.getState() == GameState.PAUSED);
    }

    private static void testPauseDoesNothingAfterTheGameEnded() {
        Game game = singleCellGame(GameMode.TIME_ATTACK);
        game.getScoreManager().addPoints(Constants.TARGET_SCORE - 1);
        game.tryPlace(0, 0, 0);
        check("game is WON", game.getState() == GameState.WON);

        game.pause();
        check("pause after a win keeps WON", game.getState() == GameState.WON);
        game.resume();
        check("resume after a win keeps WON", game.getState() == GameState.WON);
    }

    // ----- helpers -----

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
