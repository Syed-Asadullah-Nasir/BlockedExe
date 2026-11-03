package view;

import model.GameMode;
import util.Constants;
import util.ResourceLoader;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

// PLACEHOLDER for Phase 5 only. Phase 6 replaces this whole file with the real gameplay screen.
// It already has the two methods the frame calls: startGame and stopGame.
public class GamePanel extends ScreenPanel {

    private final ImageButton backButton;
    private final Font textFont;
    private GameMode mode = GameMode.CLASSIC;

    public GamePanel(GameFrame gameFrame) {
        super(gameFrame);
        textFont = gameFrame.createUiFont(30f);
        backButton = new ImageButton(
                ResourceLoader.loadImage(Constants.IMAGE_BACK_BUTTON), "BACK", 210, 600, 180, 70);
        addButton(backButton);
    }

    public void startGame(GameMode mode) {
        this.mode = mode;
        repaint();
    }

    public void stopGame() {
        // Nothing to stop yet. Phase 6 stops the game timer here.
    }

    @Override
    protected void paintContent(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        DrawUtils.drawCenteredText(g2, "GAME SCREEN", textFont, 0, 250, getWidth(), 50);
        DrawUtils.drawCenteredText(g2, "Mode: " + mode, textFont, 0, 310, getWidth(), 50);
        DrawUtils.drawCenteredText(g2, "(drawing comes in Phase 6)", textFont, 0, 370, getWidth(), 50);
    }

    @Override
    protected void onButtonClicked(ImageButton button) {
        if (button == backButton) {
            gameFrame.showScreen(GameFrame.SCREEN_MENU);
        }
    }
}
