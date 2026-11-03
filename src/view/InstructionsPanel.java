package view;

import util.Constants;
import util.ResourceLoader;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class InstructionsPanel extends ScreenPanel {

    private static final int TEXT_X = 50;
    private static final int HEADING_HEIGHT = 40;
    private static final int LINE_HEIGHT = 30;

    private final ImageButton backButton;
    private final Font titleFont;
    private final Font headingFont;
    private final Font lineFont;

    public InstructionsPanel(GameFrame gameFrame) {
        super(gameFrame);
        titleFont = gameFrame.createUiFont(44f);
        headingFont = gameFrame.createUiFont(26f);
        lineFont = gameFrame.createUiFont(20f);

        backButton = new ImageButton(
                ResourceLoader.loadImage(Constants.IMAGE_BACK_BUTTON), "BACK", 210, 690, 180, 70);
        addButton(backButton);
    }

    @Override
    protected void paintContent(Graphics2D g2) {
        drawTitle(g2, "HOW TO PLAY", titleFont, 40, 70);

        int y = 130;
        y = drawHeading(g2, "Goal", y);
        y = drawLine(g2, "Place the 3 pieces on the 8x8 board.", y);
        y = drawLine(g2, "Fill a full row or column to clear it.", y);
        y = drawLine(g2, "Clearing several lines gives bonus points.", y);

        y = drawHeading(g2, "Mouse", y);
        y = drawLine(g2, "Drag a piece onto the board and release.", y);

        y = drawHeading(g2, "Keyboard", y);
        y = drawLine(g2, "1, 2, 3: select a piece", y);
        y = drawLine(g2, "Arrow keys: move   Enter / Space: place", y);
        y = drawLine(g2, "P: pause   R: restart   M: mute", y);

        y = drawHeading(g2, "Modes", y);
        y = drawLine(g2, "Classic: play until no piece fits.", y);
        drawLine(g2, "Time Attack: reach " + Constants.TARGET_SCORE + " points in "
                + Constants.TIME_ATTACK_SECONDS + " s.", y);
    }

    // Both helpers return the y position where the next line should start.
    private int drawHeading(Graphics2D g2, String text, int y) {
        g2.setColor(new Color(255, 215, 90));
        DrawUtils.drawTextLeft(g2, text, headingFont, TEXT_X, y, HEADING_HEIGHT);
        return y + HEADING_HEIGHT;
    }

    private int drawLine(Graphics2D g2, String text, int y) {
        g2.setColor(Color.WHITE);
        DrawUtils.drawTextLeft(g2, text, lineFont, TEXT_X, y, LINE_HEIGHT);
        return y + LINE_HEIGHT;
    }

    @Override
    protected void onButtonClicked(ImageButton button) {
        if (button == backButton) {
            gameFrame.showScreen(GameFrame.SCREEN_MENU);
        }
    }
}
