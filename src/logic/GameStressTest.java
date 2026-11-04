package logic;

import model.BlockPiece;
import model.Board;
import model.GameMode;
import model.GameState;
import util.Constants;

import java.util.Random;

// Plays many complete games with a random "bot" and checks the rules after every single move.
// It never touches a file and needs no window. If a rule is ever broken, it prints what happened.
public class GameStressTest {

    private static final int GAMES_PER_MODE = 300;
    private static final int MOVE_LIMIT = 5000;

    private static int passed = 0;
    private static int failed = 0;
    private static final Random random = new Random(12345);

    public static void main(String[] args) {
        playManyGames(GameMode.CLASSIC);
        playManyGames(GameMode.TIME_ATTACK);
        testNothingWorksAfterTheEnd();
        testRestartAfterEveryEnd();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    // ----- tests -----

    private static void playManyGames(GameMode mode) {
        String problem = null;
        int finished = 0;
        int totalMoves = 0;
        int wins = 0;
        for (int i = 0; i < GAMES_PER_MODE && problem == null; i++) {
            Game game = new Game(mode, new PieceGenerator(), new ScoreManager("unused.txt"));
            int moves = 0;
            while (game.getState() == GameState.PLAYING && moves < MOVE_LIMIT && problem == null) {
                problem = playOneMove(game);
                moves++;
            }
            if (problem == null && game.getState() == GameState.PLAYING) {
                problem = "game " + i + " did not end after " + MOVE_LIMIT + " moves";
            }
            if (problem == null) {
                problem = checkEndState(game);
            }
            finished++;
            totalMoves += moves;
            if (game.getState() == GameState.WON) {
                wins++;
            }
        }
        check(mode + ": " + finished + " random games ran, " + totalMoves + " moves, no rule broken"
                + (problem == null ? "" : " -> " + problem), problem == null);
        System.out.println("      (" + mode + " games won: " + wins + ")");
    }

    private static void testNothingWorksAfterTheEnd() {
        Game game = playUntilFinished(GameMode.CLASSIC);
        int cells = countFilledCells(game.getBoard());
        int score = game.getScore();
        boolean allRejected = true;
        for (int slot = 0; slot < Constants.TRAY_SIZE; slot++) {
            for (int row = 0; row < Constants.BOARD_SIZE; row++) {
                for (int col = 0; col < Constants.BOARD_SIZE; col++) {
                    allRejected = allRejected && !game.tryPlace(slot, row, col).isPlaced();
                }
            }
        }
        check("after the end every placement is rejected", allRejected);
        check("after the end the board and score do not change",
                cells == countFilledCells(game.getBoard()) && score == game.getScore());
    }

    private static void testRestartAfterEveryEnd() {
        boolean allClean = true;
        for (GameMode mode : GameMode.values()) {
            Game game = playUntilFinished(mode);
            game.restart();
            allClean = allClean && game.getState() == GameState.PLAYING
                    && game.getScore() == 0
                    && game.getBoard().isEmpty()
                    && game.getPiecesLeft() == Constants.TRAY_SIZE;
        }
        check("restart after a finished game gives a clean new game in both modes", allClean);
    }

    // ----- the bot -----

    // Makes one move and returns a description of the broken rule, or null if everything is fine.
    private static String playOneMove(Game game) {
        // 1. Sometimes try a nonsense move first. It must change nothing.
        if (random.nextInt(4) == 0) {
            String problem = tryRandomMove(game);
            if (problem != null || game.getState() != GameState.PLAYING) {
                return problem;   // a lucky valid move may have ended the game
            }
        }

        // 2. Make a real move: a random piece at a random spot where it fits.
        int slot = randomFilledSlot(game);
        if (slot < 0) {
            return "state is PLAYING but the tray is empty";
        }
        BlockPiece piece = game.getPiece(slot);
        int[] spot = randomValidSpot(game.getBoard(), piece);
        if (spot == null) {
            // This piece fits nowhere. Another one must, because the game is still PLAYING.
            for (int other = 0; other < Constants.TRAY_SIZE; other++) {
                BlockPiece candidate = game.getPiece(other);
                if (candidate != null && randomValidSpot(game.getBoard(), candidate) != null) {
                    slot = other;
                    piece = candidate;
                    spot = randomValidSpot(game.getBoard(), candidate);
                    break;
                }
            }
        }
        if (spot == null) {
            return "state is PLAYING but no piece fits anywhere";
        }

        int scoreBefore = game.getScore();
        int cellsBefore = countFilledCells(game.getBoard());
        PlacementResult result = game.tryPlace(slot, spot[0], spot[1]);

        if (!result.isPlaced()) {
            return "a move that canPlace() allowed was rejected";
        }
        if (game.getScore() < scoreBefore) {
            return "score went down";
        }
        if (game.getScore() - scoreBefore != result.getPointsEarned()) {
            return "score change differs from pointsEarned";
        }
        int expectedCells = cellsBefore + piece.getCellCount() - result.getClearedCells().size();
        if (countFilledCells(game.getBoard()) != expectedCells) {
            return "filled cell count is wrong after the move";
        }
        return checkBoardRules(game);
    }

    // A random slot and position. If the game rejects it, nothing may change.
    private static String tryRandomMove(Game game) {
        int slot = random.nextInt(Constants.TRAY_SIZE + 2) - 1;   // includes -1 and TRAY_SIZE (bad slots)
        int row = random.nextInt(Constants.BOARD_SIZE + 6) - 3;   // includes spots outside the board
        int col = random.nextInt(Constants.BOARD_SIZE + 6) - 3;
        int cells = countFilledCells(game.getBoard());
        int score = game.getScore();
        int piecesLeft = game.getPiecesLeft();

        PlacementResult result;
        try {
            result = game.tryPlace(slot, row, col);
        } catch (RuntimeException e) {
            return "tryPlace(" + slot + "," + row + "," + col + ") threw " + e;
        }
        if (!result.isPlaced()) {
            boolean unchanged = cells == countFilledCells(game.getBoard())
                    && score == game.getScore() && piecesLeft == game.getPiecesLeft();
            return unchanged ? null : "a rejected move changed the game";
        }
        return checkBoardRules(game);   // it happened to be a valid move: check the result
    }

    // ----- checks -----

    private static String checkBoardRules(Game game) {
        Board board = game.getBoard();
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                int value = board.getCell(row, col);
                if (value < 0 || value > Constants.COLOR_COUNT) {
                    return "cell holds the invalid value " + value;
                }
            }
        }
        if (!board.findFullRows().isEmpty() || !board.findFullColumns().isEmpty()) {
            return "a complete row or column was left on the board";
        }
        if (game.getState() == GameState.PLAYING) {
            if (game.getPiecesLeft() == 0) {
                return "the tray was not refilled";
            }
            if (!game.hasAnyMove()) {
                return "PLAYING although no piece fits";
            }
        }
        return null;
    }

    private static String checkEndState(Game game) {
        GameMode mode = game.getMode();
        GameState state = game.getState();
        if (mode == GameMode.CLASSIC && state != GameState.GAME_OVER) {
            return "a finished Classic game must be GAME_OVER but is " + state;
        }
        if (mode == GameMode.TIME_ATTACK && state != GameState.WON && state != GameState.FAILED) {
            return "a finished Time Attack game must be WON or FAILED but is " + state;
        }
        if (state == GameState.WON && game.getScore() < Constants.TARGET_SCORE) {
            return "WON with only " + game.getScore() + " points";
        }
        if (state == GameState.FAILED && game.getScore() >= Constants.TARGET_SCORE) {
            return "FAILED although the target score was reached";
        }
        if (state != GameState.WON && game.hasAnyMove()) {
            return "the game ended although a piece still fits";
        }
        if (game.getBestScore() < game.getScore()) {
            return "the best score is lower than the final score";
        }
        return null;
    }

    // ----- helpers -----

    private static Game playUntilFinished(GameMode mode) {
        Game game = new Game(mode, new PieceGenerator(), new ScoreManager("unused.txt"));
        int moves = 0;
        while (game.getState() == GameState.PLAYING && moves < MOVE_LIMIT) {
            playOneMove(game);
            moves++;
        }
        return game;
    }

    private static int randomFilledSlot(Game game) {
        int start = random.nextInt(Constants.TRAY_SIZE);
        for (int i = 0; i < Constants.TRAY_SIZE; i++) {
            int slot = (start + i) % Constants.TRAY_SIZE;
            if (game.getPiece(slot) != null) {
                return slot;
            }
        }
        return -1;
    }

    // A random {row, col} where the piece fits, or null.
    private static int[] randomValidSpot(Board board, BlockPiece piece) {
        int[] chosen = null;
        int seen = 0;
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                if (board.canPlace(piece, row, col)) {
                    seen++;
                    if (random.nextInt(seen) == 0) {   // picks every valid spot with equal chance
                        chosen = new int[]{row, col};
                    }
                }
            }
        }
        return chosen;
    }

    private static int countFilledCells(Board board) {
        int count = 0;
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                if (board.getCell(row, col) != Constants.EMPTY_CELL) {
                    count++;
                }
            }
        }
        return count;
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
