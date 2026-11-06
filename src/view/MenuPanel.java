package view;

import util.Constants;
import util.ResourceLoader;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class MenuPanel extends ScreenPanel {

    private final ImageButton playButton;
    private final ImageButton instructionsButton;
    private final ImageButton exitButton;
    private final Font titleFont;
    private final Font subtitleFont;

    public MenuPanel(GameFrame gameFrame) {
        super(gameFrame);
        titleFont = gameFrame.createUiFont(56f);
        subtitleFont = gameFrame.createUiFont(22f);

        // play/icon.png is only a small white triangle, not a button background. So a plain
        // button is drawn and the triangle is put on it (the label "PLAY" is shown if it is missing).
        playButton = new ImageButton(null, "PLAY", 210, 270, 180, 180);
        playButton.setIcon(ResourceLoader.loadFirstImage(Constants.IMAGE_PLAY_ICON_PATHS));
        instructionsButton = new ImageButton(
                ResourceLoader.loadFirstImage(Constants.IMAGE_INSTRUCTIONS_BUTTON_PATHS), "INSTRUCTIONS", 150, 500, 300, 80);
        exitButton = new ImageButton(
                ResourceLoader.loadFirstImage(Constants.IMAGE_EXIT_BUTTON_PATHS), "EXIT", 150, 600, 300, 80);

        addButton(playButton);
        addButton(instructionsButton);
        addButton(exitButton);
    }

    @Override
    protected void paintContent(Graphics2D g2) {
        drawTitle(g2, "BLOCKED.exe", titleFont, 120, 90);
        g2.setColor(new Color(220, 230, 255));
        DrawUtils.drawCenteredText(g2, "The Block Burst Challenge", subtitleFont,
                0, 215, getWidth(), 30);
    }

    @Override
    protected void onButtonClicked(ImageButton button) {
        if (button == playButton) {
            gameFrame.showScreen(GameFrame.SCREEN_MODE);
        } else if (button == instructionsButton) {
            gameFrame.showScreen(GameFrame.SCREEN_INSTRUCTIONS);
        } else if (button == exitButton) {
            gameFrame.exitGame();
        }
    }
}
