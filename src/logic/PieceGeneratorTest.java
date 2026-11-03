package logic;

import model.BlockPiece;
import model.CellPosition;
import util.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PieceGeneratorTest {

    private static final int EXPECTED_SHAPE_COUNT = 20;

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testShapeCount();
        testShapesAreValidAndConnected();
        testNoDuplicateShapes();
        testTrays();
        testAllShapesAppear();
        testPiecesAreSeparateObjects();
        testCellListIsReadOnly();

        printAllShapes();
        printSampleTrays();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    // ----- tests -----

    private static void testShapeCount() {
        check("library has " + EXPECTED_SHAPE_COUNT + " shapes",
                ShapeLibrary.getShapeCount() == EXPECTED_SHAPE_COUNT);
        check("getAllShapes matches getShapeCount",
                ShapeLibrary.getAllShapes().size() == ShapeLibrary.getShapeCount());
    }

    private static void testShapesAreValidAndConnected() {
        boolean allValid = true;
        boolean allConnected = true;
        for (List<CellPosition> shape : ShapeLibrary.getAllShapes()) {
            if (shape.isEmpty()) {
                allValid = false;
                continue;
            }
            BlockPiece piece = new BlockPiece(shape, 1);
            if (!isConnected(piece)) {
                allConnected = false;
            }
        }
        check("every shape has at least one cell", allValid);
        check("every shape is one connected group of cells", allConnected);
    }

    private static void testNoDuplicateShapes() {
        Set<String> signatures = new HashSet<>();
        for (List<CellPosition> shape : ShapeLibrary.getAllShapes()) {
            signatures.add(signature(new BlockPiece(shape, 1)));
        }
        check("no shape is listed twice", signatures.size() == EXPECTED_SHAPE_COUNT);
    }

    private static void testTrays() {
        PieceGenerator generator = new PieceGenerator();
        boolean sizeOk = true;
        boolean notNull = true;
        boolean colourOk = true;

        for (int i = 0; i < 1000; i++) {
            BlockPiece[] tray = generator.generateTray();
            if (tray.length != Constants.TRAY_SIZE) {
                sizeOk = false;
            }
            for (BlockPiece piece : tray) {
                if (piece == null) {
                    notNull = false;
                    continue;
                }
                if (piece.getColorIndex() < 1 || piece.getColorIndex() > Constants.COLOR_COUNT) {
                    colourOk = false;
                }
            }
        }
        check("every tray has 3 slots", sizeOk);
        check("no tray slot is null", notNull);
        check("every colour index is between 1 and 7", colourOk);
    }

    private static void testAllShapesAppear() {
        PieceGenerator generator = new PieceGenerator();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            seen.add(signature(generator.generatePiece()));
        }
        check("all " + EXPECTED_SHAPE_COUNT + " shapes appear in 2000 random pieces",
                seen.size() == EXPECTED_SHAPE_COUNT);
    }

    private static void testPiecesAreSeparateObjects() {
        PieceGenerator generator = new PieceGenerator();
        BlockPiece[] firstTray = generator.generateTray();
        BlockPiece[] secondTray = generator.generateTray();

        boolean separate = true;
        for (int i = 0; i < firstTray.length; i++) {
            if (firstTray[i] == secondTray[i]) {
                separate = false;
            }
            for (int j = i + 1; j < firstTray.length; j++) {
                if (firstTray[i] == firstTray[j]) {
                    separate = false;
                }
            }
        }
        check("pieces are never shared between slots or trays", separate);
    }

    private static void testCellListIsReadOnly() {
        BlockPiece piece = new PieceGenerator().generatePiece();
        boolean blocked = false;
        try {
            piece.getCells().add(new CellPosition(5, 5));
        } catch (UnsupportedOperationException e) {
            blocked = true;
        }
        check("a piece's cell list cannot be changed from outside", blocked);
    }

    // ----- helpers -----

    // Flood fill from the first cell. Connected if every cell is reached.
    private static boolean isConnected(BlockPiece piece) {
        boolean[][] filled = new boolean[piece.getHeight()][piece.getWidth()];
        for (CellPosition cell : piece.getCells()) {
            filled[cell.getRow()][cell.getCol()] = true;
        }

        CellPosition start = piece.getCells().get(0);
        boolean[][] visited = new boolean[piece.getHeight()][piece.getWidth()];
        int reached = fill(filled, visited, start.getRow(), start.getCol());
        return reached == piece.getCellCount();
    }

    private static int fill(boolean[][] filled, boolean[][] visited, int row, int col) {
        if (row < 0 || row >= filled.length || col < 0 || col >= filled[0].length) {
            return 0;
        }
        if (!filled[row][col] || visited[row][col]) {
            return 0;
        }
        visited[row][col] = true;
        return 1
                + fill(filled, visited, row + 1, col)
                + fill(filled, visited, row - 1, col)
                + fill(filled, visited, row, col + 1)
                + fill(filled, visited, row, col - 1);
    }

    private static String[] pieceToRows(BlockPiece piece) {
        char[][] grid = new char[piece.getHeight()][piece.getWidth()];
        for (char[] line : grid) {
            java.util.Arrays.fill(line, '.');
        }
        for (CellPosition cell : piece.getCells()) {
            grid[cell.getRow()][cell.getCol()] = 'X';
        }

        String[] rows = new String[grid.length];
        for (int i = 0; i < grid.length; i++) {
            rows[i] = new String(grid[i]);
        }
        return rows;
    }

    private static String signature(BlockPiece piece) {
        return String.join("/", pieceToRows(piece));
    }

    private static void printAllShapes() {
        System.out.println();
        System.out.println("--- All shapes in the library (compare with Section 10 of the PDF) ---");
        int number = 1;
        for (List<CellPosition> shape : ShapeLibrary.getAllShapes()) {
            System.out.println("Shape " + number);
            for (String row : pieceToRows(new BlockPiece(shape, 1))) {
                System.out.println("  " + row);
            }
            number++;
        }
    }

    private static void printSampleTrays() {
        System.out.println();
        System.out.println("--- 5 sample trays ---");
        PieceGenerator generator = new PieceGenerator();
        for (int trayNumber = 1; trayNumber <= 5; trayNumber++) {
            System.out.println("Tray " + trayNumber);
            BlockPiece[] tray = generator.generateTray();
            List<String[]> allRows = new ArrayList<>();
            int tallest = 0;
            for (BlockPiece piece : tray) {
                String[] rows = pieceToRows(piece);
                allRows.add(rows);
                tallest = Math.max(tallest, rows.length);
            }
            for (int line = 0; line < tallest; line++) {
                StringBuilder text = new StringBuilder("  ");
                for (String[] rows : allRows) {
                    String part = line < rows.length ? rows[line] : "";
                    text.append(String.format("%-8s", part));
                }
                System.out.println(text);
            }
            System.out.println("  colours: " + tray[0].getColorIndex() + " "
                    + tray[1].getColorIndex() + " " + tray[2].getColorIndex());
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
