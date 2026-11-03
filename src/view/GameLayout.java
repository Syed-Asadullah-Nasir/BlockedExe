package view;

import util.Constants;

import java.awt.Rectangle;

// All positions of the gameplay screen in one place. The drawing code uses them now,
// and the mouse code (Phase 7) uses the same numbers to find out what was clicked.
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

    // Pause and mute buttons in the top right corner
    private static final int BUTTON_SIZE = 50;
    private static final int BUTTON_Y = 20;
    private static final int BUTTON_GAP = 8;
    private static final int SCREEN_MARGIN = 16;

    private GameLayout() {
    }

    public static Rectangle getBoardRect() {
        return new Rectangle(BOARD_X, BOARD_Y, BOARD_PIXELS, BOARD_PIXELS);
    }

    public static Rectangle getSlotRect(int slotIndex) {
        int totalWidth = Constants.TRAY_SIZE * SLOT_WIDTH + (Constants.TRAY_SIZE - 1) * SLOT_GAP;
        int startX = (Constants.WINDOW_WIDTH - totalWidth) / 2;
        int x = startX + slotIndex * (SLOT_WIDTH + SLOT_GAP);
        return new Rectangle(x, SLOT_Y, SLOT_WIDTH, SLOT_HEIGHT);
    }

    public static Rectangle getMuteButtonRect() {
        int x = Constants.WINDOW_WIDTH - SCREEN_MARGIN - BUTTON_SIZE;
        return new Rectangle(x, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE);
    }

    public static Rectangle getPauseButtonRect() {
        int x = getMuteButtonRect().x - BUTTON_GAP - BUTTON_SIZE;
        return new Rectangle(x, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE);
    }
}
