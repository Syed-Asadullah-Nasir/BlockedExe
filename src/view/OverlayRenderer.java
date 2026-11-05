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

    private static final int WINDOW_WIDTH = Constants.WINDOW_WIDTH;
    private static final int CARD_X = 70;
    private static final int CARD_WIDTH = 460;
    private static final int CARD_TEXT_PADDING = 28;   // space between the card edge and the text
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

    // Colours of the result icons: dark outline, main colour and a light highlight.
    private static final Color WIN_OUTLINE = new Color(15, 110, 60);
    private static final Color WIN_FILL = new Color(60, 215, 125);
    private static final Color WIN_SHINE = new Color(185, 255, 215);
    private static final Color LOSE_OUTLINE = new Color(150, 20, 50);
    private static final Color LOSE_FILL = new Color(255, 90, 120);
    private static final Color LOSE_SHINE = new Color(255, 175, 190);

    // Colours of the plain buttons that are drawn when a button image is missing.
    private static final Color GREEN = new Color(70, 185, 90);
    private static final Color ORANGE = new Color(240, 150, 40);
    private static final Color RED = new Color(235, 70, 100);

    public OverlayRenderer(Font baseFont) {
        titleFont = baseFont.deriveFont(54f);
        textFont = baseFont.deriveFont(32f);
        hintFont = baseFont.deriveFont(22f);
        buttonFont = baseFont.deriveFont(26f);

        // restart_icon.png is an icon, not a button background, so it is not used here.
        // If a button image is missing, a plain button in the given colour is drawn instead.
        BufferedImage resumeImage = ResourceLoader.loadFirstImage(Constants.IMAGE_RESUME_BUTTON_PATHS);
        BufferedImage restartImage = ResourceLoader.loadFirstImage(Constants.IMAGE_RESTART_BUTTON_PATHS);
        BufferedImage menuImage = ResourceLoader.loadImage(Constants.IMAGE_MENU_BUTTON);

        pauseButtons = new ImageButton[]{
                createButton(resumeImage, "RESUME", PAUSE_BUTTON_Y, GREEN),
                createButton(restartImage, "RESTART", PAUSE_BUTTON_Y + PAUSE_BUTTON_STEP, ORANGE),
                createButton(menuImage, "MAIN MENU", PAUSE_BUTTON_Y + 2 * PAUSE_BUTTON_STEP, RED)
        };
        endButtons = new ImageButton[]{
                createButton(restartImage, "RESTART", END_BUTTON_Y, ORANGE),
                createButton(menuImage, "MAIN MENU", END_BUTTON_Y + END_BUTTON_STEP, RED)
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
        drawCardText(g2, "PAUSED", titleFont, PAUSE_CARD_Y + 25, 70);
        g2.setColor(LIGHT_TEXT);
        drawCardText(g2, "Press P to resume", hintFont, PAUSE_CARD_Y + 100, 32);

        drawButtons(g2, pauseButtons);
    }

    private void drawEndCard(Graphics2D g2, Game game) {
        boolean won = game.getState() == GameState.WON;
        drawCard(g2, END_CARD_Y, END_CARD_HEIGHT);

        drawResultIcon(g2, won, (WINDOW_WIDTH - END_ICON_SIZE) / 2, END_CARD_Y + 25, END_ICON_SIZE);

        g2.setColor(Color.WHITE);
        drawCardText(g2, getEndTitle(game), titleFont, END_CARD_Y + 125, 70);

        g2.setColor(Color.WHITE);
        drawCardText(g2, "SCORE  " + game.getScore(), textFont, END_CARD_Y + 205, 40);
        g2.setColor(GOLD);
        drawCardText(g2, "BEST  " + game.getBestScore(), textFont, END_CARD_Y + 250, 40);
        g2.setColor(LIGHT_TEXT);
        drawCardText(g2, "TIME  " + TimeFormat.formatElapsed(game.getElapsedMillis()), hintFont, END_CARD_Y + 295, 36);
        if (game.isNewBestReached()) {
            g2.setColor(GOLD);
            drawCardText(g2, "NEW BEST!", textFont, END_CARD_Y + 333, 40);
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

    // Centered text that is never wider than the card: a long text gets smaller instead of overflowing.
    private void drawCardText(Graphics2D g2, String text, Font font, int y, int height) {
        DrawUtils.drawCenteredText(g2, text, font, CARD_X + CARD_TEXT_PADDING, y,
                CARD_WIDTH - 2 * CARD_TEXT_PADDING, height);
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
    // The cross (game over) and the check mark (win) are drawn with lines instead of a small picture,
    // so they stay sharp at any size. Each is drawn three times: a dark outline, the colour, a highlight.
    private void drawResultIcon(Graphics2D g2, boolean won, int x, int y, int size) {
        Color outline = won ? WIN_OUTLINE : LOSE_OUTLINE;
        Color fill = won ? WIN_FILL : LOSE_FILL;
        Color shine = won ? WIN_SHINE : LOSE_SHINE;

        drawIconStrokes(g2, won, x, y, size, outline, size * 34 / 100, 0);
        drawIconStrokes(g2, won, x, y, size, fill, size * 23 / 100, 0);
        drawIconStrokes(g2, won, x, y, size, shine, size * 5 / 100, -size * 4 / 100);
        g2.setStroke(new BasicStroke(1f));
    }

    // The lines of the icon. offset moves them up and to the left (used for the highlight).
    private void drawIconStrokes(Graphics2D g2, boolean won, int x, int y, int size,
                                 Color color, int width, int offset) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(Math.max(1, width), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int left = x + offset;
        int top = y + offset;
        if (won) {
            int[] xs = {left + size * 23 / 100, left + size * 43 / 100, left + size * 78 / 100};
            int[] ys = {top + size * 54 / 100, top + size * 73 / 100, top + size * 28 / 100};
            g2.drawPolyline(xs, ys, xs.length);
        } else {
            int near = size * 24 / 100;
            int far = size * 76 / 100;
            g2.drawLine(left + near, top + near, left + far, top + far);
            g2.drawLine(left + far, top + near, left + near, top + far);
        }
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

    private ImageButton createButton(BufferedImage image, String label, int y, Color fallbackColor) {
        ImageButton button = new ImageButton(image, label, BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setFallbackColor(fallbackColor);
        return button;
    }
}
