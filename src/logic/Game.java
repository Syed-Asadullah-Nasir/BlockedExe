package logic;

import model.BlockPiece;
import model.Board;
import model.CellPosition;
import model.GameMode;
import model.GameState;
import util.Constants;

import java.util.ArrayList;
import java.util.List;

// The rules of the game. It has no Swing code, so it can be tested from the console.
// It also keeps the play time: the screen reports how much time passed, and the game
// counts it only while it is running.
public class Game {

    private static final long NANOS_PER_MILLI = 1000000L;

    private final Board board;
    private final PieceGenerator pieceGenerator;
    private final ScoreManager scoreManager;
    private final GameMode mode;
    private BlockPiece[] tray;
    private GameState state;
    private long elapsedNanos;

    public Game(GameMode mode) {
        this(mode, new PieceGenerator());
    }

    // The generator can be replaced in tests to get a fixed tray.
    public Game(GameMode mode, PieceGenerator pieceGenerator) {
        if (mode == null || pieceGenerator == null) {
            throw new IllegalArgumentException("mode and pieceGenerator are required");
        }
        this.mode = mode;
        this.pieceGenerator = pieceGenerator;
        this.board = new Board();
        this.scoreManager = new ScoreManager();
        this.tray = pieceGenerator.generateTray();
        this.state = GameState.PLAYING;
        this.elapsedNanos = 0;
    }

    // ----- getters -----

    public Board getBoard() {
        return board;
    }

    public GameMode getMode() {
        return mode;
    }

    public GameState getState() {
        return state;
    }

    // True once the game has ended in any way (game over, won or failed).
    public boolean isFinished() {
        return state == GameState.GAME_OVER
                || state == GameState.WON
                || state == GameState.FAILED;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public int getScore() {
        return scoreManager.getScore();
    }

    public int getBestScore() {
        return scoreManager.getBestScore(mode);
    }

    // Returns the piece in this slot, or null if the slot is empty or the index is invalid.
    public BlockPiece getPiece(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= tray.length) {
            return null;
        }
        return tray[slotIndex];
    }

    public int getPiecesLeft() {
        int count = 0;
        for (BlockPiece piece : tray) {
            if (piece != null) {
                count++;
            }
        }
        return count;
    }

    // ----- time -----

    // Adds play time (in nanoseconds). Only a running game counts time, so a pause can
    // never skip or lose time. In Time Attack the game fails when the time limit is reached.
    public void addElapsedTime(long nanos) {
        if (state != GameState.PLAYING || nanos <= 0) {
            return;
        }
        elapsedNanos += nanos;

        if (mode == GameMode.TIME_ATTACK && elapsedNanos >= getTimeLimitNanos()) {
            elapsedNanos = getTimeLimitNanos();
            finishGame(GameState.FAILED);
        }
    }

    public long getElapsedMillis() {
        return elapsedNanos / NANOS_PER_MILLI;
    }

    // Time Attack: how much time is left. Classic has no time limit and returns 0.
    public long getRemainingMillis() {
        if (mode != GameMode.TIME_ATTACK) {
            return 0;
        }
        return (getTimeLimitNanos() - elapsedNanos) / NANOS_PER_MILLI;
    }

    // True when a Time Attack game ended because the time ran out.
    public boolean isTimeUp() {
        return mode == GameMode.TIME_ATTACK && elapsedNanos >= getTimeLimitNanos();
    }

    private long getTimeLimitNanos() {
        return Constants.TIME_ATTACK_SECONDS * 1000L * NANOS_PER_MILLI;
    }

    // ----- actions -----

    // Tries to place the piece from a tray slot. row and col are the board position of the
    // piece's top-left cell. A rejected move changes nothing, so the piece stays in its slot.
    public PlacementResult tryPlace(int slotIndex, int row, int col) {
        BlockPiece piece = getPiece(slotIndex);
        if (state != GameState.PLAYING || piece == null) {
            return PlacementResult.rejected();
        }
        if (!board.place(piece, row, col)) {
            return PlacementResult.rejected();
        }

        PlacementResult result = clearLinesAndScore(piece, row, col);
        tray[slotIndex] = null;
        refillTrayIfEmpty();
        updateStateAfterMove();
        return result;
    }

    // True if at least one remaining piece fits somewhere on the board.
    public boolean hasAnyMove() {
        for (BlockPiece piece : tray) {
            if (piece != null && pieceFitsAnywhere(piece)) {
                return true;
            }
        }
        return false;
    }

    // Only a running game can be paused.
    public void pause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
        }
    }

    // Only a paused game can be resumed.
    public void resume() {
        if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
        }
    }

    public void restart() {
        board.reset();
        scoreManager.resetScore();
        tray = pieceGenerator.generateTray();
        state = GameState.PLAYING;
        elapsedNanos = 0;
    }

    // ----- helpers -----

    private PlacementResult clearLinesAndScore(BlockPiece piece, int row, int col) {
        List<CellPosition> placedCells = getBoardCells(piece, row, col);

        // Read the cells and their colours BEFORE clearing, for the flash animation later.
        List<CellPosition> clearedCells = board.findCellsToClear();
        List<Integer> clearedColors = new ArrayList<>();
        for (CellPosition cell : clearedCells) {
            clearedColors.add(board.getCell(cell.getRow(), cell.getCol()));
        }

        int linesCleared = board.clearCompletedLines();
        int points = calculatePoints(piece.getCellCount(), linesCleared);
        scoreManager.addPoints(points);

        return PlacementResult.accepted(placedCells, piece.getColorIndex(),
                linesCleared, points, clearedCells, clearedColors);
    }

    private List<CellPosition> getBoardCells(BlockPiece piece, int row, int col) {
        List<CellPosition> cells = new ArrayList<>();
        for (CellPosition offset : piece.getCells()) {
            cells.add(new CellPosition(row + offset.getRow(), col + offset.getCol()));
        }
        return cells;
    }

    private int calculatePoints(int cellCount, int linesCleared) {
        int points = cellCount * Constants.POINTS_PER_CELL;
        points += linesCleared * Constants.POINTS_PER_LINE;
        if (linesCleared >= 2) {
            points += (linesCleared - 1) * Constants.MULTI_LINE_BONUS_PER_EXTRA_LINE;
        }
        return points;
    }

    private void refillTrayIfEmpty() {
        if (getPiecesLeft() == 0) {
            tray = pieceGenerator.generateTray();
        }
    }

    // Win check first, then the "no move left" check.
    private void updateStateAfterMove() {
        if (mode == GameMode.TIME_ATTACK && scoreManager.getScore() >= Constants.TARGET_SCORE) {
            finishGame(GameState.WON);
        } else if (!hasAnyMove()) {
            if (mode == GameMode.CLASSIC) {
                finishGame(GameState.GAME_OVER);
            } else {
                finishGame(GameState.FAILED);
            }
        }
    }

    private void finishGame(GameState endState) {
        state = endState;
        scoreManager.updateBestScore(mode);
    }

    private boolean pieceFitsAnywhere(BlockPiece piece) {
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                if (board.canPlace(piece, row, col)) {
                    return true;
                }
            }
        }
        return false;
    }
}
