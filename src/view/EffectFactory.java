package view;

import effects.ClearFlashEffect;
import effects.EffectCell;
import effects.PlacementPopEffect;
import effects.ScorePopupEffect;
import effects.VisualEffect;
import logic.PlacementResult;
import model.CellPosition;
import util.Constants;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

// Turns a PlacementResult into the animations that should play. It lives in the view package
// because it needs screen positions (GameLayout) and the block pictures (BoardRenderer).
public class EffectFactory {

    private final BoardRenderer boardRenderer;
    private final Font popupFont;

    public EffectFactory(BoardRenderer boardRenderer, Font popupFont) {
        this.boardRenderer = boardRenderer;
        this.popupFont = popupFont;
    }

    // A rejected move gives an empty list.
    public List<VisualEffect> createEffects(PlacementResult result) {
        List<VisualEffect> effects = new ArrayList<>();
        if (!result.isPlaced()) {
            return effects;
        }

        effects.add(new PlacementPopEffect(
                toEffectCells(result.getPlacedCells(), singleColor(result))));

        if (result.getLinesCleared() > 0) {
            effects.add(new ClearFlashEffect(
                    toEffectCells(result.getClearedCells(), result.getClearedColors())));
        }
        if (result.getPointsEarned() > 0) {
            effects.add(createPopup(result));
        }
        return effects;
    }

    // "+points" above the middle of the placed piece.
    private ScorePopupEffect createPopup(PlacementResult result) {
        int sumX = 0;
        int sumY = 0;
        List<CellPosition> cells = result.getPlacedCells();
        for (CellPosition cell : cells) {
            sumX += GameLayout.getCellX(cell.getCol()) + Constants.CELL_SIZE / 2;
            sumY += GameLayout.getCellY(cell.getRow()) + Constants.CELL_SIZE / 2;
        }
        int centerX = cells.isEmpty() ? Constants.WINDOW_WIDTH / 2 : sumX / cells.size();
        int centerY = cells.isEmpty() ? GameLayout.BOARD_Y : sumY / cells.size();
        return new ScorePopupEffect("+" + result.getPointsEarned(), centerX, centerY, popupFont);
    }

    private List<Integer> singleColor(PlacementResult result) {
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < result.getPlacedCells().size(); i++) {
            colors.add(result.getColorIndex());
        }
        return colors;
    }

    private List<EffectCell> toEffectCells(List<CellPosition> positions, List<Integer> colors) {
        List<EffectCell> cells = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            CellPosition position = positions.get(i);
            int colorIndex = colors.get(i);
            cells.add(new EffectCell(
                    GameLayout.getCellX(position.getCol()),
                    GameLayout.getCellY(position.getRow()),
                    Constants.CELL_SIZE,
                    boardRenderer.getBlockImage(colorIndex),
                    boardRenderer.getBlockColor(colorIndex)));
        }
        return cells;
    }
}
