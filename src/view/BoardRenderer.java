package view;

import logic.Game;
import model.BlockPiece;
import model.Board;
import model.CellPosition;
import util.Constants;
import util.ResourceLoader;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// Draws the board, the cells, the tray slots and the pieces. It only draws; it never changes the game.
public class BoardRenderer {

    private static final Color BOARD_COLOR = new Color(38, 40, 72);
    private static final Color EMPTY_CELL_COLOR = new Color(55, 58, 100);
    private static final Color SLOT_FALLBACK_COLOR = new Color(30, 38, 85, 200);
    private static final Color SLOT_HIGHLIGHT_COLOR = new Color(255, 200, 40);
    private static final Color GHOST_VALID_COLOR = new Color(90, 230, 120, 150);
    private static final Color GHOST_INVALID_COLOR = new Color(235, 70, 70, 150);

    // Used when a block image is missing. Index 0 (empty) is unused.
    private static final Color[] FALLBACK_COLORS = {
            null,
            new Color(60, 120, 230),   // 1 blue
            new Color(70, 190, 90),    // 2 green
            new Color(240, 140, 40),   // 3 orange
            new Color(240, 100, 170),  // 4 pink
            new Color(150, 90, 210),   // 5 purple
            new Color(220, 60, 60),    // 6 red
            new Color(240, 200, 50)    // 7 yellow
    };

    private final BufferedImage[] blockImages = new BufferedImage[Constants.COLOR_COUNT + 1];
    private final BufferedImage slotImage;

    public BoardRenderer() {
        for (int colorIndex = 1; colorIndex <= Constants.COLOR_COUNT; colorIndex++) {
            blockImages[colorIndex] = ResourceLoader.loadImage(Constants.BLOCK_IMAGE_PATHS[colorIndex]);
        }
        slotImage = ResourceLoader.loadImage(Constants.IMAGE_SLOT_BACKGROUND);
    }

    public void drawBoard(Graphics2D g2, Board board) {
        Rectangle area = GameLayout.getBoardRect();

        g2.setColor(BOARD_COLOR);
        g2.fillRoundRect(area.x - 8, area.y - 8, area.width + 16, area.height + 16, 18, 18);

        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                int x = GameLayout.getCellX(col);
                int y = GameLayout.getCellY(row);
                int colorIndex = board.getCell(row, col);

                if (colorIndex == Constants.EMPTY_CELL) {
                    drawEmptyCell(g2, x, y, Constants.CELL_SIZE);
                } else {
                    drawCell(g2, colorIndex, x, y, Constants.CELL_SIZE);
                }
            }
        }
    }

    // Draws the three slots and the pieces that are still in the tray.
    // The piece in hiddenSlot is not drawn, because the player is dragging it (use -1 for none).
    public void drawTray(Graphics2D g2, Game game, int hiddenSlot) {
        for (int slot = 0; slot < Constants.TRAY_SIZE; slot++) {
            Rectangle area = GameLayout.getSlotRect(slot);
            drawSlotBackground(g2, area);

            BlockPiece piece = game.getPiece(slot);
            if (piece != null && slot != hiddenSlot) {
                int pieceWidth = piece.getWidth() * GameLayout.TRAY_CELL_SIZE;
                int pieceHeight = piece.getHeight() * GameLayout.TRAY_CELL_SIZE;
                int x = area.x + (area.width - pieceWidth) / 2;
                int y = area.y + (area.height - pieceHeight) / 2;
                drawPiece(g2, piece, x, y, GameLayout.TRAY_CELL_SIZE);
            }
        }
    }

    // A gold outline around the slot the player selected with the keyboard.
    // Gold stays visible on both the light slot image and the dark fallback slot.
    public void drawSlotHighlight(Graphics2D g2, int slotIndex) {
        Rectangle area = GameLayout.getSlotRect(slotIndex);
        g2.setColor(SLOT_HIGHLIGHT_COLOR);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(area.x - 3, area.y - 3, area.width + 6, area.height + 6, 24, 24);
        g2.setStroke(new BasicStroke(1f));
    }

    // The placement preview: the board cells the piece would fill, green if the move is
    // allowed and red if it is not. Cells outside the board are not drawn.
    public void drawGhost(Graphics2D g2, Board board, BlockPiece piece, int row, int col, boolean valid) {
        g2.setColor(valid ? GHOST_VALID_COLOR : GHOST_INVALID_COLOR);
        for (CellPosition offset : piece.getCells()) {
            int cellRow = row + offset.getRow();
            int cellCol = col + offset.getCol();
            if (board.isInside(cellRow, cellCol)) {
                int x = GameLayout.getCellX(cellCol);
                int y = GameLayout.getCellY(cellRow);
                g2.fillRoundRect(x + 1, y + 1, Constants.CELL_SIZE - 2, Constants.CELL_SIZE - 2, 8, 8);
            }
        }
    }

    // x and y are the top-left corner of the piece in pixels.
    public void drawPiece(Graphics2D g2, BlockPiece piece, int x, int y, int cellSize) {
        for (CellPosition offset : piece.getCells()) {
            int cellX = x + offset.getCol() * cellSize;
            int cellY = y + offset.getRow() * cellSize;
            drawCell(g2, piece.getColorIndex(), cellX, cellY, cellSize);
        }
    }

    public void drawCell(Graphics2D g2, int colorIndex, int x, int y, int size) {
        if (colorIndex < 1 || colorIndex > Constants.COLOR_COUNT) {
            return;
        }

        BufferedImage image = blockImages[colorIndex];
        if (image != null) {
            DrawUtils.drawStretched(g2, image, x, y, size, size);
        } else {
            Color base = FALLBACK_COLORS[colorIndex];
            g2.setColor(base);
            g2.fillRoundRect(x + 1, y + 1, size - 2, size - 2, 8, 8);
            g2.setColor(base.brighter());
            g2.drawRoundRect(x + 2, y + 2, size - 5, size - 5, 6, 6);
        }
    }

    // The block picture of a colour, or null if it could not be loaded. Used by the effects.
    public BufferedImage getBlockImage(int colorIndex) {
        if (colorIndex < 1 || colorIndex > Constants.COLOR_COUNT) {
            return null;
        }
        return blockImages[colorIndex];
    }

    // The plain colour used when the block picture is missing. Used by the effects.
    public Color getBlockColor(int colorIndex) {
        if (colorIndex < 1 || colorIndex > Constants.COLOR_COUNT) {
            return null;
        }
        return FALLBACK_COLORS[colorIndex];
    }

    private void drawEmptyCell(Graphics2D g2, int x, int y, int size) {
        g2.setColor(EMPTY_CELL_COLOR);
        g2.fillRoundRect(x + 1, y + 1, size - 2, size - 2, 8, 8);
    }

    private void drawSlotBackground(Graphics2D g2, Rectangle area) {
        if (slotImage != null) {
            DrawUtils.drawNineSlice(g2, slotImage, area.x, area.y, area.width, area.height);
        } else {
            g2.setColor(SLOT_FALLBACK_COLOR);
            g2.fillRoundRect(area.x, area.y, area.width, area.height, 20, 20);
        }
    }
}
