package view;

import util.Constants;

import java.awt.Rectangle;

// All positions of the gameplay screen in one place. The drawing code and the mouse code
// both use these numbers, so a click and a drawn cell always agree.
public final class GameLayout {

    // Board: 8 x 8 cells
    public static final int BOARD_X = 76;
    public static final int BOARD_Y = 150;
    public static final int BOARD_PIXELS = Constants.BOARD_SIZE * Constants.CELL_SIZE;

    // Tray: three slots below the board. Pieces are drawn smaller in the tray.
    public static final int TRAY_CELL_SIZE = 30;
    public static final int SLOT_WIDTH = 170;
    public static final int SLOT_HEIGHT = 140;
    public static final int SLOT_GAP = 15;
    public static final int SLOT_Y = 630;

    // Pause and mute buttons in the top right corner, one above the other.
    // They have the same 3 : 1 shape as the button images, so the image fills the whole
    // click area (a squarer box made the image a thin strip much smaller than the click area).
    private static final int BUTTON_WIDTH = 102;
    private static final int BUTTON_HEIGHT = 34;
    private static final int BUTTON_Y = 14;
    private static final int BUTTON_GAP = 8;
    private static final int SCREEN_MARGIN = 16;

    private GameLayout() {
    }

    public static Rectangle getBoardRect() {
        return new Rectangle(BOARD_X, BOARD_Y, BOARD_PIXELS, BOARD_PIXELS);
    }

    // Pixel position of the top-left corner of a board cell.
    public static int getCellX(int col) {
        return BOARD_X + col * Constants.CELL_SIZE;
    }

    public static int getCellY(int row) {
        return BOARD_Y + row * Constants.CELL_SIZE;
    }

    // The column whose left edge is closest to this pixel. It can be outside the board
    // (negative or 8 and above); the board's canPlace() rejects those positions.
    // Rounding is used instead of cutting off, so a piece snaps to the nearest cell.
    public static int nearestColumn(int pixelX) {
        return (int) Math.round((pixelX - BOARD_X) / (double) Constants.CELL_SIZE);
    }

    public static int nearestRow(int pixelY) {
        return (int) Math.round((pixelY - BOARD_Y) / (double) Constants.CELL_SIZE);
    }

    public static Rectangle getSlotRect(int slotIndex) {
        int totalWidth = Constants.TRAY_SIZE * SLOT_WIDTH + (Constants.TRAY_SIZE - 1) * SLOT_GAP;
        int startX = (Constants.WINDOW_WIDTH - totalWidth) / 2;
        int x = startX + slotIndex * (SLOT_WIDTH + SLOT_GAP);
        return new Rectangle(x, SLOT_Y, SLOT_WIDTH, SLOT_HEIGHT);
    }

    // Returns the slot under this point, or -1 if the point is not on a slot.
    public static int findSlotAt(int pixelX, int pixelY) {
        for (int slot = 0; slot < Constants.TRAY_SIZE; slot++) {
            if (getSlotRect(slot).contains(pixelX, pixelY)) {
                return slot;
            }
        }
        return -1;
    }

    public static Rectangle getPauseButtonRect() {
        int x = Constants.WINDOW_WIDTH - SCREEN_MARGIN - BUTTON_WIDTH;
        return new Rectangle(x, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public static Rectangle getMuteButtonRect() {
        Rectangle pause = getPauseButtonRect();
        return new Rectangle(pause.x, pause.y + BUTTON_HEIGHT + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
}
