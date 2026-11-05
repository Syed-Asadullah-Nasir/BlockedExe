package view;

import model.GameMode;
import util.Constants;
import util.ResourceLoader;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class ModePanel extends ScreenPanel {

    private final ImageButton classicButton;
    private final ImageButton timeAttackButton;
    private final ImageButton backButton;
    private final Font titleFont;
    private final Font captionFont;

    public ModePanel(GameFrame gameFrame) {
        super(gameFrame);
        titleFont = gameFrame.createUiFont(44f);
        captionFont = gameFrame.createUiFont(20f);

        classicButton = new ImageButton(
                ResourceLoader.loadFirstImage(Constants.IMAGE_CLASSIC_BUTTON_PATHS), "CLASSIC", 140, 230, 320, 90);
        timeAttackButton = new ImageButton(
                ResourceLoader.loadFirstImage(Constants.IMAGE_TIME_ATTACK_BUTTON_PATHS), "TIME ATTACK", 140, 390, 320, 90);
        backButton = new ImageButton(
                ResourceLoader.loadFirstImage(Constants.IMAGE_BACK_BUTTON_PATHS), "BACK", 210, 580, 180, 70);

        addButton(classicButton);
        addButton(timeAttackButton);
        addButton(backButton);
    }

    @Override
    protected void paintContent(Graphics2D g2) {
        drawTitle(g2, "SELECT MODE", titleFont, 110, 70);

        g2.setColor(new Color(220, 230, 255));
        DrawUtils.drawCenteredText(g2, "Play until no piece fits", captionFont,
                0, 328, getWidth(), 26);
        String timeAttackText = "Reach " + Constants.TARGET_SCORE + " points in "
                + Constants.TIME_ATTACK_SECONDS + " seconds";
        DrawUtils.drawCenteredText(g2, timeAttackText, captionFont,
                0, 488, getWidth(), 26);
    }

    @Override
    protected void onButtonClicked(ImageButton button) {
        if (button == classicButton) {
            gameFrame.startGame(GameMode.CLASSIC);
        } else if (button == timeAttackButton) {
            gameFrame.startGame(GameMode.TIME_ATTACK);
        } else if (button == backButton) {
            gameFrame.showScreen(GameFrame.SCREEN_MENU);
        }
    }
}
