package model;

import util.Constants;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final int size;
    private final int[][] grid;

    public Board() {
        size = Constants.BOARD_SIZE;
        grid = new int[size][size];
    }

    public int getSize() {
        return size;
    }

    // Returns 0 (empty) for coordinates outside the board.
    public int getCell(int row, int col) {
        if (!isInside(row, col)) {
            return Constants.EMPTY_CELL;
        }
        return grid[row][col];
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    // row and col are the board position of the piece's top-left cell.
    public boolean canPlace(BlockPiece piece, int row, int col) {
        if (piece == null) {
            return false;
        }
        for (CellPosition offset : piece.getCells()) {
            int boardRow = row + offset.getRow();
            int boardCol = col + offset.getCol();
            if (!isInside(boardRow, boardCol)) {
                return false;
            }
            if (grid[boardRow][boardCol] != Constants.EMPTY_CELL) {
                return false;
            }
        }
        return true;
    }

    // Fills the cells and returns true. If the placement is invalid, changes nothing and returns false.
    public boolean place(BlockPiece piece, int row, int col) {
        if (!canPlace(piece, row, col)) {
            return false;
        }
        for (CellPosition offset : piece.getCells()) {
            grid[row + offset.getRow()][col + offset.getCol()] = piece.getColorIndex();
        }
        return true;
    }

    public List<Integer> findFullRows() {
        List<Integer> fullRows = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            if (isRowFull(row)) {
                fullRows.add(row);
            }
        }
        return fullRows;
    }

    public List<Integer> findFullColumns() {
        List<Integer> fullColumns = new ArrayList<>();
        for (int col = 0; col < size; col++) {
            if (isColumnFull(col)) {
                fullColumns.add(col);
            }
        }
        return fullColumns;
    }

    // Every cell that belongs to a full row or a full column. Each cell appears once,
    // even at an intersection. Call this BEFORE clearCompletedLines() if you need the
    // cell colours (use getCell) for an animation.
    public List<CellPosition> findCellsToClear() {
        boolean[][] marked = new boolean[size][size];

        for (int row : findFullRows()) {
            for (int col = 0; col < size; col++) {
                marked[row][col] = true;
            }
        }
        for (int col : findFullColumns()) {
            for (int row = 0; row < size; row++) {
                marked[row][col] = true;
            }
        }

        List<CellPosition> cellsToClear = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (marked[row][col]) {
                    cellsToClear.add(new CellPosition(row, col));
                }
            }
        }
        return cellsToClear;
    }

    // Detects all full rows and columns first, then clears them together.
    // Returns the number of lines cleared (rows + columns).
    public int clearCompletedLines() {
        int linesCleared = findFullRows().size() + findFullColumns().size();
        List<CellPosition> cellsToClear = findCellsToClear();

        for (CellPosition cell : cellsToClear) {
            grid[cell.getRow()][cell.getCol()] = Constants.EMPTY_CELL;
        }
        return linesCleared;
    }

    public boolean isEmpty() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid[row][col] != Constants.EMPTY_CELL) {
                    return false;
                }
            }
        }
        return true;
    }

    public void reset() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                grid[row][col] = Constants.EMPTY_CELL;
            }
        }
    }

    private boolean isRowFull(int row) {
        for (int col = 0; col < size; col++) {
            if (grid[row][col] == Constants.EMPTY_CELL) {
                return false;
            }
        }
        return true;
    }

    private boolean isColumnFull(int col) {
        for (int row = 0; row < size; row++) {
            if (grid[row][col] == Constants.EMPTY_CELL) {
                return false;
            }
        }
        return true;
    }
}
