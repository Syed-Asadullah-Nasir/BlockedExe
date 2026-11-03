package view;

import util.Constants;
import util.ResourceLoader;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// Draws the information on top of the gameplay screen:
// best score, score, time, and the pause and mute buttons. It only draws.
public class HudRenderer {

    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color LIGHT_TEXT = new Color(230, 235, 255);
    private static final Color FALLBACK_PANEL = new Color(30, 38, 85, 200);

    private static final Rectangle STAR_AREA = new Rectangle(20, 20, 40, 40);
    private static final Rectangle SCORE_AREA = new Rectangle(190, 14, 220, 70);
    private static final int TIME_Y = 92;
    private static final int TIME_HEIGHT = 36;

    private final BufferedImage starImage;
    private final BufferedImage scoreBackground;
    private final BufferedImage pauseImage;
    private final BufferedImage muteImage;
    private final Font scoreFont;
    private final Font bestFont;
    private final Font timeFont;

    public HudRenderer(Font baseFont) {
        scoreFont = baseFont.deriveFont(40f);
        bestFont = baseFont.deriveFont(26f);
        timeFont = baseFont.deriveFont(24f);

        starImage = ResourceLoader.loadImage(Constants.IMAGE_BEST_STAR);
        scoreBackground = ResourceLoader.loadImage(Constants.IMAGE_SCORE_BACKGROUND);
        pauseImage = ResourceLoader.loadImage(Constants.IMAGE_PAUSE_BUTTON);
        muteImage = ResourceLoader.loadImage(Constants.IMAGE_MUTE_BUTTON);
    }

    public void draw(Graphics2D g2, int score, int bestScore, String timeText, boolean muted) {
        drawBestScore(g2, bestScore);
        drawScore(g2, score);
        drawTime(g2, timeText);
        drawIconButton(g2, pauseImage, GameLayout.getPauseButtonRect(), "II");
        drawIconButton(g2, muteImage, GameLayout.getMuteButtonRect(), "M");
        if (muted) {
            drawMutedCross(g2, GameLayout.getMuteButtonRect());
        }
    }

    private void drawBestScore(Graphics2D g2, int bestScore) {
        if (starImage != null) {
            DrawUtils.drawFitted(g2, starImage, STAR_AREA.x, STAR_AREA.y, STAR_AREA.width, STAR_AREA.height);
        } else {
            g2.setColor(GOLD);
            g2.fillOval(STAR_AREA.x + 4, STAR_AREA.y + 4, STAR_AREA.width - 8, STAR_AREA.height - 8);
        }
        g2.setColor(GOLD);
        DrawUtils.drawTextLeft(g2, String.valueOf(bestScore), bestFont,
                STAR_AREA.x + STAR_AREA.width + 8, STAR_AREA.y, STAR_AREA.height);
    }

    private void drawScore(Graphics2D g2, int score) {
        if (scoreBackground != null) {
            DrawUtils.drawStretched(g2, scoreBackground,
                    SCORE_AREA.x, SCORE_AREA.y, SCORE_AREA.width, SCORE_AREA.height);
        } else {
            g2.setColor(FALLBACK_PANEL);
            g2.fillRoundRect(SCORE_AREA.x, SCORE_AREA.y, SCORE_AREA.width, SCORE_AREA.height, 24, 24);
        }
        g2.setColor(Color.WHITE);
        DrawUtils.drawCenteredText(g2, String.valueOf(score), scoreFont,
                SCORE_AREA.x, SCORE_AREA.y, SCORE_AREA.width, SCORE_AREA.height);
    }

    private void drawTime(Graphics2D g2, String timeText) {
        g2.setColor(LIGHT_TEXT);
        DrawUtils.drawCenteredText(g2, timeText, timeFont, 0, TIME_Y, Constants.WINDOW_WIDTH, TIME_HEIGHT);
    }

    private void drawIconButton(Graphics2D g2, BufferedImage image, Rectangle area, String fallbackLabel) {
        if (image != null) {
            DrawUtils.drawFitted(g2, image, area.x, area.y, area.width, area.height);
            return;
        }
        g2.setColor(FALLBACK_PANEL);
        g2.fillRoundRect(area.x, area.y, area.width, area.height, 14, 14);
        g2.setColor(Color.WHITE);
        DrawUtils.drawCenteredText(g2, fallbackLabel, bestFont, area.x, area.y, area.width, area.height);
    }

    // A red diagonal line over the mute icon shows that the sound is off.
    private void drawMutedCross(Graphics2D g2, Rectangle area) {
        g2.setColor(new Color(230, 60, 60));
        g2.setStroke(new BasicStroke(4f));
        g2.drawLine(area.x + 6, area.y + area.height - 6, area.x + area.width - 6, area.y + 6);
        g2.setStroke(new BasicStroke(1f));
    }
}
