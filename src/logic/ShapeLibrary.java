package logic;

import model.CellPosition;

import java.util.ArrayList;
import java.util.List;

// Holds every shape of the game. Rotated versions are separate shapes,
// because the game has no rotate button.
public final class ShapeLibrary {

    // "X" is a filled cell and "." is empty. Each inner array is one shape, row by row.
    private static final String[][] SHAPE_PATTERNS = {
            // Single square
            {"X"},

            // Straight lines
            {"XX"},
            {"X", "X"},
            {"XXX"},
            {"X", "X", "X"},
            {"XXXX"},
            {"X", "X", "X", "X"},

            // Square
            {"XX", "XX"},

            // Rectangles (3 rows by 2 columns, and 2 rows by 3 columns)
            {"XX", "XX", "XX"},
            {"XXX", "XXX"},

            // L shapes (4 rotations)
            {"X.", "X.", "XX"},
            {"XXX", "X.."},
            {"XX", ".X", ".X"},
            {"..X", "XXX"},

            // T shapes (4 rotations)
            {"XXX", ".X."},
            {".X", "XX", ".X"},
            {".X.", "XXX"},
            {"X.", "XX", "X."},

            // S shapes (2 rotations)
            {".XX", "XX."},
            {"X.", "XX", ".X"}
    };

    private ShapeLibrary() {
    }

    // Returns a fresh list of shapes. Each shape is a list of cell offsets
    // relative to its top-left corner.
    public static List<List<CellPosition>> getAllShapes() {
        List<List<CellPosition>> shapes = new ArrayList<>();
        for (String[] pattern : SHAPE_PATTERNS) {
            shapes.add(patternToCells(pattern));
        }
        return shapes;
    }

    public static int getShapeCount() {
        return SHAPE_PATTERNS.length;
    }

    private static List<CellPosition> patternToCells(String[] pattern) {
        List<CellPosition> cells = new ArrayList<>();
        for (int row = 0; row < pattern.length; row++) {
            for (int col = 0; col < pattern[row].length(); col++) {
                if (pattern[row].charAt(col) == 'X') {
                    cells.add(new CellPosition(row, col));
                }
            }
        }
        return cells;
    }
}
