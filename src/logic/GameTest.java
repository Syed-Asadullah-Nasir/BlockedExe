package logic;

import model.BlockPiece;
import model.Board;
import model.CellPosition;
import model.GameMode;
import model.GameState;
import util.Constants;

import java.util.ArrayList;
import java.util.List;

public class GameTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testNewGame();
        testInvalidPlacementKeepsPiece();
        testInvalidSlots();
        testUsedSlotRejected();
        testTrayRefill();
        testSingleLineScore();
        testTwoLinesScoreMore();
        testGameOverClassic();
        testNotOverWhileMoveExists();
        testRejectedAfterGameOver();
        testTimeAttackFails();
        testTimeAttackWins();
        testRestartKeepsBestScore();
        testHasAnyMoveOnEmptyBoard();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    // ----- tests -----

    private static void testNewGame() {
        Game game = new Game(GameMode.CLASSIC);
        check("new game is PLAYING", game.getState() == GameState.PLAYING);
        check("new game score is 0", game.getScore() == 0);
        check("new game board is empty", game.getBoard().isEmpty());
        check("new game tray has 3 pieces", game.getPiecesLeft() == 3);
        check("new game mode is kept", game.getMode() == GameMode.CLASSIC);
    }

    private static void testInvalidPlacementKeepsPiece() {
        Game game = fixedGame(GameMode.CLASSIC, rect(1, 1), rect(1, 1), rect(1, 1));
        PlacementResult first = game.tryPlace(0, 0, 0);
        check("valid placement is accepted", first.isPlaced());
        check("valid placement scores 1 point", game.getScore() == 1);
        check("used slot is emptied", game.getPiece(0) == null);

        PlacementResult overlap = game.tryPlace(1, 0, 0);
        check("overlapping placement is rejected", !overlap.isPlaced());
        check("rejected piece stays in its slot", game.getPiece(1) != null);
        check("rejected placement does not change the score", game.getScore() == 1);
        check("rejected placement leaves 2 pieces", game.getPiecesLeft() == 2);

        PlacementResult outside = game.tryPlace(1, 7, 8);
        check("placement outside the board is rejected", !outside.isPlaced());
        check("piece still in slot after outside attempt", game.getPiece(1) != null);
    }

    private static void testInvalidSlots() {
        Game game = fixedGame(GameMode.CLASSIC, rect(1, 1), rect(1, 1), rect(1, 1));
        check("slot -1 is rejected", !game.tryPlace(-1, 0, 0).isPlaced());
        check("slot 3 is rejected", !game.tryPlace(3, 0, 0).isPlaced());
        check("invalid slot getPiece returns null", game.getPiece(5) == null);
        check("board still empty after invalid slots", game.getBoard().isEmpty());
    }

    private static void testUsedSlotRejected() {
        Game game = fixedGame(GameMode.CLASSIC, rect(1, 1), rect(1, 1), rect(1, 1));
        game.tryPlace(0, 0, 0);
        check("an already used slot cannot be placed again", !game.tryPlace(0, 3, 3).isPlaced());
    }

    private static void testTrayRefill() {
        Game game = fixedGame(GameMode.CLASSIC, rect(1, 1), rect(1, 1), rect(1, 1));
        game.tryPlace(0, 0, 0);
        game.tryPlace(1, 0, 1);
        check("2 placed leaves 1 piece", game.getPiecesLeft() == 1);
        game.tryPlace(2, 0, 2);
        check("placing the third piece refills the tray", game.getPiecesLeft() == 3);
        check("game keeps playing after refill", game.getState() == GameState.PLAYING);
    }

    private static void testSingleLineScore() {
        Game game = fixedGame(GameMode.CLASSIC, rect(1, 4), rect(1, 4), rect(1, 1));
        PlacementResult first = game.tryPlace(0, 0, 0);
        check("first half of a row scores 4", first.getPointsEarned() == 4 && game.getScore() == 4);
        check("first half clears no line", first.getLinesCleared() == 0);

        PlacementResult second = game.tryPlace(1, 0, 4);
        check("completing a row clears 1 line", second.getLinesCleared() == 1);
        check("completing a row scores 4 + 10 = 14", second.getPointsEarned() == 14);
        check("total score is 18", game.getScore() == 18);
        check("result lists 8 cleared cells", second.getClearedCells().size() == 8);
        check("result lists a colour for each cleared cell", clearedColorsAreValid(second));
        check("the row is empty after clearing", game.getBoard().getCell(0, 0) == 0);
        check("result lists 4 placed cells", second.getCellsPlaced() == 4);
    }

    private static void testTwoLinesScoreMore() {
        Game oneLineGame = fixedGame(GameMode.CLASSIC, rect(1, 1), rect(1, 1), rect(1, 1));
        oneLineGame.getBoard().place(rect(1, 7), 0, 1);
        PlacementResult oneLine = oneLineGame.tryPlace(0, 0, 0);

        Game twoLineGame = fixedGame(GameMode.CLASSIC, rect(2, 1), rect(1, 1), rect(1, 1));
        twoLineGame.getBoard().place(rect(2, 7), 0, 1);
        PlacementResult twoLines = twoLineGame.tryPlace(0, 0, 0);

        check("one line move scores 1 + 10 = 11", oneLine.getPointsEarned() == 11);
        check("two lines at once clear 2 lines", twoLines.getLinesCleared() == 2);
        check("two lines at once score 2 + 20 + 10 = 32", twoLines.getPointsEarned() == 32);
        check("two lines score more than one line", twoLines.getPointsEarned() > oneLine.getPointsEarned());
        check("board is empty after clearing two rows", twoLineGame.getBoard().isEmpty());
    }

    private static void testGameOverClassic() {
        Game game = fixedGame(GameMode.CLASSIC, rect(2, 2), rect(2, 2), rect(2, 2));
        fillCheckerboardLeavingTopLeftHole(game.getBoard());
        check("before the move a piece still fits", game.hasAnyMove());

        PlacementResult result = game.tryPlace(0, 0, 0);
        check("the 2x2 piece fits into the hole", result.isPlaced());
        check("no piece fits afterwards, so GAME_OVER", game.getState() == GameState.GAME_OVER);
        check("remaining pieces are still in the tray", game.getPiecesLeft() == 2);
        check("best score is updated at game over", game.getBestScore() == 4);
    }

    private static void testNotOverWhileMoveExists() {
        Game game = fixedGame(GameMode.CLASSIC, rect(2, 2), rect(1, 1), rect(1, 1));
        fillCheckerboardLeavingTopLeftHole(game.getBoard());
        game.tryPlace(0, 0, 0);
        check("a 1x1 piece still fits, so the game continues", game.getState() == GameState.PLAYING);
        check("hasAnyMove is true while a piece fits", game.hasAnyMove());
        check("best score is not set while playing", game.getBestScore() == 0);
    }

    private static void testRejectedAfterGameOver() {
        Game game = fixedGame(GameMode.CLASSIC, rect(2, 2), rect(1, 1), rect(2, 2));
        fillCheckerboardLeavingTopLeftHole(game.getBoard());
        game.tryPlace(0, 0, 0);
        game.tryPlace(1, 0, 3);
        check("game is over when only 2x2 is left", game.getState() == GameState.GAME_OVER);
        check("placing after game over is rejected", !game.tryPlace(2, 0, 3).isPlaced());
    }

    private static void testTimeAttackFails() {
        Game game = fixedGame(GameMode.TIME_ATTACK, rect(2, 2), rect(2, 2), rect(2, 2));
        fillCheckerboardLeavingTopLeftHole(game.getBoard());
        game.tryPlace(0, 0, 0);
        check("Time Attack with no moves left is FAILED", game.getState() == GameState.FAILED);
    }

    private static void testTimeAttackWins() {
        Game game = fixedGame(GameMode.TIME_ATTACK, rect(1, 1), rect(1, 1), rect(1, 1));
        game.getScoreManager().addPoints(Constants.TARGET_SCORE - 1);
        check("still PLAYING just below the target", game.getState() == GameState.PLAYING);
        game.tryPlace(0, 0, 0);
        check("reaching the target is WON", game.getState() == GameState.WON);
        check("best score is saved on a win", game.getBestScore() == Constants.TARGET_SCORE);
        check("placing after a win is rejected", !game.tryPlace(1, 0, 1).isPlaced());
    }

    private static void testRestartKeepsBestScore() {
        Game game = fixedGame(GameMode.CLASSIC, rect(2, 2), rect(2, 2), rect(2, 2));
        fillCheckerboardLeavingTopLeftHole(game.getBoard());
        game.tryPlace(0, 0, 0);
        game.restart();
        check("restart sets PLAYING", game.getState() == GameState.PLAYING);
        check("restart resets the score", game.getScore() == 0);
        check("restart empties the board", game.getBoard().isEmpty());
        check("restart gives a full tray", game.getPiecesLeft() == 3);
        check("restart keeps the best score", game.getBestScore() == 4);
    }

    private static void testHasAnyMoveOnEmptyBoard() {
        Game game = new Game(GameMode.CLASSIC);
        check("a random tray always has a move on an empty board", game.hasAnyMove());
    }

    // ----- helpers -----

    // Gives a game the same three pieces every time a tray is generated.
    private static class FixedPieceGenerator extends PieceGenerator {

        private final BlockPiece[] pieces;

        FixedPieceGenerator(BlockPiece first, BlockPiece second, BlockPiece third) {
            pieces = new BlockPiece[]{first, second, third};
        }

        @Override
        public BlockPiece[] generateTray() {
            BlockPiece[] tray = new BlockPiece[pieces.length];
            for (int i = 0; i < pieces.length; i++) {
                tray[i] = new BlockPiece(pieces[i]);
            }
            return tray;
        }
    }

    private static Game fixedGame(GameMode mode, BlockPiece first, BlockPiece second, BlockPiece third) {
        return new Game(mode, new FixedPieceGenerator(first, second, third));
    }

    private static BlockPiece rect(int rows, int cols) {
        List<CellPosition> cells = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cells.add(new CellPosition(row, col));
            }
        }
        return new BlockPiece(cells, 2);
    }

    // Fills every cell where (row + col) is even, except the top-left 2x2 area.
    // No row or column is full, no 2x2 fits anywhere except the hole, and a 1x1 fits in the gaps.
    private static void fillCheckerboardLeavingTopLeftHole(Board board) {
        BlockPiece single = rect(1, 1);
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                boolean inHole = row < 2 && col < 2;
                if ((row + col) % 2 == 0 && !inHole) {
                    board.place(single, row, col);
                }
            }
        }
    }

    private static boolean clearedColorsAreValid(PlacementResult result) {
        if (result.getClearedColors().size() != result.getClearedCells().size()) {
            return false;
        }
        for (int colorIndex : result.getClearedColors()) {
            if (colorIndex < 1 || colorIndex > Constants.COLOR_COUNT) {
                return false;
            }
        }
        return true;
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
