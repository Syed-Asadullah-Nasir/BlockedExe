package model;

// A row and column pair. Used for offsets inside a piece and for locations on the board.
public class CellPosition {

    private final int row;
    private final int col;

    public CellPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
