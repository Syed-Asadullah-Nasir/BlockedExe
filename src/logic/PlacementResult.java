package logic;

import model.CellPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Describes what happened when the player tried to place a piece.
// The screen uses it to choose sounds (Phase 11) and animations (Phase 12).
public class PlacementResult {

    private final boolean placed;
    private final List<CellPosition> placedCells;
    private final int colorIndex;
    private final int linesCleared;
    private final int pointsEarned;
    private final List<CellPosition> clearedCells;
    private final List<Integer> clearedColors;

    private PlacementResult(boolean placed, List<CellPosition> placedCells, int colorIndex,
                            int linesCleared, int pointsEarned,
                            List<CellPosition> clearedCells, List<Integer> clearedColors) {
        this.placed = placed;
        this.placedCells = placedCells;
        this.colorIndex = colorIndex;
        this.linesCleared = linesCleared;
        this.pointsEarned = pointsEarned;
        this.clearedCells = clearedCells;
        this.clearedColors = clearedColors;
    }

    // Nothing changed: the move was not allowed.
    public static PlacementResult rejected() {
        return new PlacementResult(false, new ArrayList<CellPosition>(), 0, 0, 0,
                new ArrayList<CellPosition>(), new ArrayList<Integer>());
    }

    // The piece was placed. clearedColors[i] is the colour that clearedCells[i] had before it was cleared.
    public static PlacementResult accepted(List<CellPosition> placedCells, int colorIndex,
                                           int linesCleared, int pointsEarned,
                                           List<CellPosition> clearedCells,
                                           List<Integer> clearedColors) {
        return new PlacementResult(true, placedCells, colorIndex, linesCleared, pointsEarned,
                clearedCells, clearedColors);
    }

    public boolean isPlaced() {
        return placed;
    }

    // Board positions of the cells that were just filled.
    public List<CellPosition> getPlacedCells() {
        return Collections.unmodifiableList(placedCells);
    }

    public int getCellsPlaced() {
        return placedCells.size();
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public int getLinesCleared() {
        return linesCleared;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public List<CellPosition> getClearedCells() {
        return Collections.unmodifiableList(clearedCells);
    }

    public List<Integer> getClearedColors() {
        return Collections.unmodifiableList(clearedColors);
    }
}
