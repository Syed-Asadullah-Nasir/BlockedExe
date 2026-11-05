package view;

import logic.Game;
import model.GameState;
import util.Constants;
import util.ResourceLoader;
import util.TimeFormat;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// Draws the pause screen and the end-of-game screens (game over, win, fail) on top of the
// game, and knows where their buttons are. It only draws; GamePanel decides what a click does.
public class OverlayRenderer {

    private static final Color DIM_COLOR = new Color(8, 12, 36, 200);
    private static final Color CARD_COLOR = new Color(34, 44, 104);
    private static final Color CARD_BORDER_COLOR = new Color(255, 255, 255, 190);
    private static final Color LIGHT_TEXT = new Color(220, 228, 255);
    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color WIN_COLOR = new Color(70, 190, 90);
    private static final Color LOSE_COLOR = new Color(220, 60, 60);

    private static final int WINDOW_WIDTH = Constants.WINDOW_WIDTH;
    private static final int CARD_X = 70;
    private static final int CARD_WIDTH = 460;
    private static final int BUTTON_WIDTH = 280;
    private static final int BUTTON_HEIGHT = 70;
    private static final int BUTTON_X = (WINDOW_WIDTH - BUTTON_WIDTH) / 2;

    // Pause card
    private static final int PAUSE_CARD_Y = 190;
    private static final int PAUSE_CARD_HEIGHT = 430;
    private static final int PAUSE_BUTTON_Y = 345;
    private static final int PAUSE_BUTTON_STEP = 85;

    // End-of-game card
    private static final int END_CARD_Y = 130;
    private static final int END_CARD_HEIGHT = 560;
    private static final int END_ICON_SIZE = 90;
    private static final int END_BUTTON_Y = 520;
    private static final int END_BUTTON_STEP = 85;

    private final BufferedImage crossImage;
    private final BufferedImage winImage;
    private final Font titleFont;
    private final Font textFont;
    private final Font hintFont;
    private final Font buttonFont;

    // The buttons of each screen. actions[i] belongs to buttons[i].
    private final ImageButton[] pauseButtons;
    private final OverlayAction[] pauseActions = {
            OverlayAction.RESUME, OverlayAction.RESTART, OverlayAction.MAIN_MENU};
    private final ImageButton[] endButtons;
    private final OverlayAction[] endActions = {
            OverlayAction.RESTART, OverlayAction.MAIN_MENU};

    public OverlayRenderer(Font baseFont) {
        titleFont = baseFont.deriveFont(54f);
        textFont = baseFont.deriveFont(32f);
        hintFont = baseFont.deriveFont(22f);
        buttonFont = baseFont.deriveFont(26f);

        crossImage = ResourceLoader.loadImage(Constants.IMAGE_GAME_OVER_CROSS);
        winImage = ResourceLoader.loadImage(Constants.IMAGE_WIN_CHECK);

        BufferedImage resumeImage = ResourceLoader.loadImage(Constants.IMAGE_RESUME_BUTTON);
        // restart_icon.png is an icon, not a button background, so Restart uses the Resume background.
        BufferedImage restartImage = resumeImage;
        BufferedImage menuImage = ResourceLoader.loadImage(Constants.IMAGE_MENU_BUTTON);

        pauseButtons = new ImageButton[]{
                createButton(resumeImage, "RESUME", PAUSE_BUTTON_Y),
                createButton(restartImage, "RESTART", PAUSE_BUTTON_Y + PAUSE_BUTTON_STEP),
                createButton(menuImage, "MAIN MENU", PAUSE_BUTTON_Y + 2 * PAUSE_BUTTON_STEP)
        };
        endButtons = new ImageButton[]{
                createButton(restartImage, "RESTART", END_BUTTON_Y),
                createButton(menuImage, "MAIN MENU", END_BUTTON_Y + END_BUTTON_STEP)
        };
    }

    // ----- drawing -----

    // Draws nothing while the game is running.
    public void draw(Graphics2D g2, Game game) {
        GameState state = game.getState();
        if (state == GameState.PLAYING || state == GameState.MENU) {
            return;
        }

        g2.setColor(DIM_COLOR);
        g2.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        if (state == GameState.PAUSED) {
            drawPauseCard(g2);
        } else {
            drawEndCard(g2, game);
        }
    }

    private void drawPauseCard(Graphics2D g2) {
        drawCard(g2, PAUSE_CARD_Y, PAUSE_CARD_HEIGHT);

        g2.setColor(Color.WHITE);
        DrawUtils.drawCenteredText(g2, "PAUSED", titleFont, 0, PAUSE_CARD_Y + 25, WINDOW_WIDTH, 70);
        g2.setColor(LIGHT_TEXT);
        DrawUtils.drawCenteredText(g2, "Press P to resume", hintFont, 0, PAUSE_CARD_Y + 100, WINDOW_WIDTH, 32);

        drawButtons(g2, pauseButtons);
    }

    private void drawEndCard(Graphics2D g2, Game game) {
        boolean won = game.getState() == GameState.WON;
        drawCard(g2, END_CARD_Y, END_CARD_HEIGHT);

        drawResultIcon(g2, won, (WINDOW_WIDTH - END_ICON_SIZE) / 2, END_CARD_Y + 25, END_ICON_SIZE);

        g2.setColor(Color.WHITE);
        DrawUtils.drawCenteredText(g2, getEndTitle(game), titleFont, 0, END_CARD_Y + 125, WINDOW_WIDTH, 70);

        g2.setColor(Color.WHITE);
        DrawUtils.drawCenteredText(g2, "SCORE  " + game.getScore(), textFont,
                0, END_CARD_Y + 205, WINDOW_WIDTH, 40);
        g2.setColor(GOLD);
        DrawUtils.drawCenteredText(g2, "BEST  " + game.getBestScore(), textFont,
                0, END_CARD_Y + 250, WINDOW_WIDTH, 40);
        g2.setColor(LIGHT_TEXT);
        DrawUtils.drawCenteredText(g2, "TIME  " + TimeFormat.formatElapsed(game.getElapsedMillis()), hintFont,
                0, END_CARD_Y + 295, WINDOW_WIDTH, 36);
        if (game.isNewBestReached()) {
            g2.setColor(GOLD);
            DrawUtils.drawCenteredText(g2, "NEW BEST!", textFont, 0, END_CARD_Y + 333, WINDOW_WIDTH, 40);
        }

        drawButtons(g2, endButtons);
    }

    private String getEndTitle(Game game) {
        switch (game.getState()) {
            case WON:
                return "YOU WIN!";
            case FAILED:
                return game.isTimeUp() ? "TIME'S UP!" : "NO MOVES LEFT";
            default:
                return "GAME OVER";
        }
    }

    private void drawCard(Graphics2D g2, int y, int height) {
        g2.setColor(CARD_COLOR);
        g2.fillRoundRect(CARD_X, y, CARD_WIDTH, height, 40, 40);
        g2.setColor(CARD_BORDER_COLOR);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(CARD_X, y, CARD_WIDTH, height, 40, 40);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawButtons(Graphics2D g2, ImageButton[] buttons) {
        for (ImageButton button : buttons) {
            button.draw(g2, buttonFont);
        }
    }

    // Uses the provided image. If it is missing, a coloured circle with a check mark or a cross is drawn.
    private void drawResultIcon(Graphics2D g2, boolean won, int x, int y, int size) {
        BufferedImage image = won ? winImage : crossImage;
        if (image != null) {
            DrawUtils.drawFitted(g2, image, x, y, size, size);
            return;
        }

        g2.setColor(won ? WIN_COLOR : LOSE_COLOR);
        g2.fillOval(x, y, size, size);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (won) {
            g2.drawLine(x + size * 27 / 100, y + size * 52 / 100, x + size * 43 / 100, y + size * 68 / 100);
            g2.drawLine(x + size * 43 / 100, y + size * 68 / 100, x + size * 74 / 100, y + size * 32 / 100);
        } else {
            int pad = size * 3 / 10;
            g2.drawLine(x + pad, y + pad, x + size - pad, y + size - pad);
            g2.drawLine(x + size - pad, y + pad, x + pad, y + size - pad);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    // ----- buttons -----

    // Returns the action of the button under this point, or null if there is none.
    public OverlayAction actionAt(GameState state, int pointX, int pointY) {
        ImageButton[] buttons = getButtons(state);
        OverlayAction[] actions = getActions(state);
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].contains(pointX, pointY)) {
                return actions[i];
            }
        }
        return null;
    }

    // Highlights the button under the pointer. Returns true if the pointer is over a button.
    public boolean updateHover(GameState state, int pointX, int pointY) {
        clearHover();
        boolean overAny = false;
        for (ImageButton button : getButtons(state)) {
            boolean over = button.contains(pointX, pointY);
            button.setHovered(over);
            if (over) {
                overAny = true;
            }
        }
        return overAny;
    }

    public void clearHover() {
        for (ImageButton button : pauseButtons) {
            button.setHovered(false);
        }
        for (ImageButton button : endButtons) {
            button.setHovered(false);
        }
    }

    private ImageButton[] getButtons(GameState state) {
        switch (state) {
            case PAUSED:
                return pauseButtons;
            case GAME_OVER:
            case WON:
            case FAILED:
                return endButtons;
            default:
                return new ImageButton[0];
        }
    }

    private OverlayAction[] getActions(GameState state) {
        return state == GameState.PAUSED ? pauseActions : endActions;
    }

    private ImageButton createButton(BufferedImage image, String label, int y) {
        return new ImageButton(image, label, BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
}
