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

        playButton = new ImageButton(
                ResourceLoader.loadImage(Constants.IMAGE_PLAY_ICON), "PLAY", 210, 270, 180, 180);
        instructionsButton = new ImageButton(
                ResourceLoader.loadImage(Constants.IMAGE_INSTRUCTIONS_BUTTON), "INSTRUCTIONS", 150, 500, 300, 80);
        exitButton = new ImageButton(
                ResourceLoader.loadImage(Constants.IMAGE_EXIT_BUTTON), "EXIT", 150, 600, 300, 80);

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
