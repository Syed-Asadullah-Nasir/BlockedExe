package model;

import util.Constants;

import java.util.ArrayList;
import java.util.List;

public class BoardTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testPlaceInside();
        testOverlap();
        testPartlyOutside();
        testCorners();
        testClearRow();
        testClearColumn();
        testClearKeepsOtherCells();
        testClearTwoRows();
        testRowAndColumnCross();
        testNothingFull();
        testPartialRowNotCleared();
        testInvalidPlaceLeavesBoardUnchanged();
        testEmptyPieceRejected();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    // ----- tests -----

    private static void testPlaceInside() {
        Board board = new Board();
        BlockPiece square = makePiece(2, 2, 3);
        boolean placed = board.place(square, 2, 3);
        check("place inside returns true", placed);
        check("place inside fills top-left cell with colour 3", board.getCell(2, 3) == 3);
        check("place inside fills bottom-right cell", board.getCell(3, 4) == 3);
        check("place inside leaves neighbour empty", board.getCell(4, 3) == 0);
    }

    private static void testOverlap() {
        Board board = new Board();
        board.place(makePiece(2, 2, 1), 0, 0);
        BlockPiece single = makePiece(1, 1, 2);
        check("overlap: canPlace is false", !board.canPlace(single, 1, 1));
        check("overlap: place returns false", !board.place(single, 1, 1));
        check("overlap: touching but not overlapping is allowed", board.canPlace(single, 2, 2));
    }

    private static void testPartlyOutside() {
        Board board = new Board();
        BlockPiece square = makePiece(2, 2, 1);
        check("outside top edge rejected", !board.canPlace(square, -1, 3));
        check("outside bottom edge rejected", !board.canPlace(square, 7, 3));
        check("outside left edge rejected", !board.canPlace(square, 3, -1));
        check("outside right edge rejected", !board.canPlace(square, 3, 7));
    }

    private static void testCorners() {
        BlockPiece square = makePiece(2, 2, 1);
        int last = Constants.BOARD_SIZE - 2;
        check("corner top-left accepted", new Board().canPlace(square, 0, 0));
        check("corner top-right accepted", new Board().canPlace(square, 0, last));
        check("corner bottom-left accepted", new Board().canPlace(square, last, 0));
        check("corner bottom-right accepted", new Board().canPlace(square, last, last));

        Board board = new Board();
        BlockPiece single = makePiece(1, 1, 1);
        check("single cell in last corner placed", board.place(single, 7, 7));
        check("single cell in last corner is stored", board.getCell(7, 7) == 1);
    }

    private static void testClearRow() {
        Board board = new Board();
        board.place(makePiece(1, 8, 4), 3, 0);
        check("full row is detected", board.findFullRows().size() == 1);
        check("cells to clear lists 8 cells", board.findCellsToClear().size() == 8);
        check("cell colour is still readable before clearing", board.getCell(3, 0) == 4);
        int cleared = board.clearCompletedLines();
        check("clearing one row returns 1", cleared == 1);
        check("board is empty after clearing the row", board.isEmpty());
    }

    private static void testClearColumn() {
        Board board = new Board();
        board.place(makePiece(8, 1, 5), 0, 6);
        check("full column is detected", board.findFullColumns().size() == 1);
        int cleared = board.clearCompletedLines();
        check("clearing one column returns 1", cleared == 1);
        check("board is empty after clearing the column", board.isEmpty());
    }

    private static void testClearKeepsOtherCells() {
        Board board = new Board();
        board.place(makePiece(1, 8, 1), 3, 0);
        board.place(makePiece(1, 1, 2), 0, 0);
        board.clearCompletedLines();
        check("unrelated cell survives the clear", board.getCell(0, 0) == 2);
        check("row 3 is cleared", board.getCell(3, 4) == 0);
    }

    private static void testClearTwoRows() {
        Board board = new Board();
        board.place(makePiece(1, 8, 1), 1, 0);
        board.place(makePiece(1, 8, 2), 6, 0);
        check("two full rows cleared together returns 2", board.clearCompletedLines() == 2);
        check("board is empty after two rows", board.isEmpty());
    }

    private static void testRowAndColumnCross() {
        Board board = new Board();
        // Row 3 is full. Column 5 is filled around row 3, and the row fills the gap.
        board.place(makePiece(1, 8, 1), 3, 0);
        board.place(makePiece(3, 1, 2), 0, 5);
        board.place(makePiece(4, 1, 2), 4, 5);

        check("cross: one full row found", board.findFullRows().size() == 1);
        check("cross: one full column found", board.findFullColumns().size() == 1);
        check("cross: intersection listed once (8 + 8 - 1 = 15 cells)",
                board.findCellsToClear().size() == 15);

        int cleared = board.clearCompletedLines();
        check("cross: lines cleared is 2", cleared == 2);
        check("cross: intersection cell is empty afterwards", board.getCell(3, 5) == 0);
        check("cross: board is empty afterwards", board.isEmpty());
    }

    private static void testNothingFull() {
        Board board = new Board();
        board.place(makePiece(2, 2, 1), 0, 0);
        check("nothing full clears 0 lines", board.clearCompletedLines() == 0);
        check("nothing full keeps the piece", board.getCell(0, 0) == 1);
    }

    private static void testPartialRowNotCleared() {
        Board board = new Board();
        board.place(makePiece(1, 7, 1), 2, 0);
        check("7 of 8 cells is not a full row", board.findFullRows().isEmpty());
        check("partial row clears 0 lines", board.clearCompletedLines() == 0);
    }

    private static void testInvalidPlaceLeavesBoardUnchanged() {
        Board board = new Board();
        board.place(makePiece(2, 2, 1), 0, 0);
        String before = snapshot(board);

        boolean overlapResult = board.place(makePiece(2, 2, 2), 1, 1);
        boolean outsideResult = board.place(makePiece(2, 2, 2), 7, 7);

        check("invalid overlap place returns false", !overlapResult);
        check("invalid outside place returns false", !outsideResult);
        check("board unchanged after invalid places", before.equals(snapshot(board)));
    }

    private static void testEmptyPieceRejected() {
        boolean rejected = false;
        try {
            new BlockPiece(new ArrayList<CellPosition>(), 1);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check("BlockPiece rejects an empty cell list", rejected);
    }

    // ----- helpers -----

    // Builds a filled rectangle piece of the given size.
    private static BlockPiece makePiece(int rows, int cols, int colorIndex) {
        List<CellPosition> cells = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cells.add(new CellPosition(row, col));
            }
        }
        return new BlockPiece(cells, colorIndex);
    }

    private static String snapshot(Board board) {
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                text.append(board.getCell(row, col)).append(',');
            }
        }
        return text.toString();
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
