package model;

import util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockPiece {

    private final List<CellPosition> cells;
    private final int colorIndex;
    private final int width;
    private final int height;

    // Cell offsets are relative to the piece's top-left cell, so they must not be negative.
    public BlockPiece(List<CellPosition> cells, int colorIndex) {
        if (cells == null || cells.isEmpty()) {
            throw new IllegalArgumentException("A piece needs at least one cell");
        }
        if (colorIndex < 1 || colorIndex > Constants.COLOR_COUNT) {
            throw new IllegalArgumentException("Invalid colour index: " + colorIndex);
        }

        int maxRow = 0;
        int maxCol = 0;
        for (CellPosition cell : cells) {
            if (cell == null || cell.getRow() < 0 || cell.getCol() < 0) {
                throw new IllegalArgumentException("Cell offsets must be zero or positive");
            }
            maxRow = Math.max(maxRow, cell.getRow());
            maxCol = Math.max(maxCol, cell.getCol());
        }

        this.cells = new ArrayList<>(cells);
        this.colorIndex = colorIndex;
        this.height = maxRow + 1;
        this.width = maxCol + 1;
    }

    // Copy constructor
    public BlockPiece(BlockPiece other) {
        this(other.cells, other.colorIndex);
    }

    // Read-only view: callers cannot change the piece through this list
    public List<CellPosition> getCells() {
        return Collections.unmodifiableList(cells);
    }

    public int getCellCount() {
        return cells.size();
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
