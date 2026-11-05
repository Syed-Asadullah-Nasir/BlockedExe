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
        drawPauseButton(g2, GameLayout.getPauseButtonRect());
        drawMuteButton(g2, GameLayout.getMuteButtonRect(), muted);
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

    // The images of the pause and mute buttons are only backgrounds, so the icons are drawn here.
    private void drawPauseButton(Graphics2D g2, Rectangle area) {
        Color iconColor = drawButtonBackground(g2, pauseImage, area);
        g2.setColor(iconColor);
        int barWidth = area.width * 15 / 100;
        int barHeight = area.height * 44 / 100;
        int gap = area.width * 14 / 100;
        int top = area.y + (area.height - barHeight) / 2;
        int left = area.x + (area.width - (2 * barWidth + gap)) / 2;
        g2.fillRoundRect(left, top, barWidth, barHeight, 4, 4);
        g2.fillRoundRect(left + barWidth + gap, top, barWidth, barHeight, 4, 4);
    }

    // A loudspeaker. Sound on: two sound waves next to it. Sound off: a cross next to it.
    private void drawMuteButton(Graphics2D g2, Rectangle area, boolean muted) {
        Color iconColor = drawButtonBackground(g2, muteImage, area);
        g2.setColor(iconColor);
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int centerY = area.y + area.height / 2;
        int left = area.x + area.width * 22 / 100;
        int boxWidth = area.width * 12 / 100;
        int boxHalf = area.height * 11 / 100;
        int coneWidth = area.width * 16 / 100;
        int coneHalf = area.height * 24 / 100;

        int[] xs = {left, left + boxWidth, left + boxWidth + coneWidth, left + boxWidth + coneWidth, left + boxWidth, left};
        int[] ys = {centerY - boxHalf, centerY - boxHalf, centerY - coneHalf, centerY + coneHalf, centerY + boxHalf, centerY + boxHalf};
        g2.fillPolygon(xs, ys, xs.length);

        int waveX = left + boxWidth + coneWidth + area.width * 6 / 100;
        if (muted) {
            int size = area.height * 10 / 100;
            int crossX = waveX + size;
            g2.drawLine(crossX - size, centerY - size, crossX + size, centerY + size);
            g2.drawLine(crossX - size, centerY + size, crossX + size, centerY - size);
        } else {
            int small = area.height * 14 / 100;
            int big = area.height * 26 / 100;
            g2.drawArc(waveX - small, centerY - small, 2 * small, 2 * small, -50, 100);
            g2.drawArc(waveX - big, centerY - big, 2 * big, 2 * big, -50, 100);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    // Draws the button background (or a plain rounded box if the image is missing).
    // Returns the colour the icon should have: dark on a light button, white on a dark one.
    private Color drawButtonBackground(Graphics2D g2, BufferedImage image, Rectangle area) {
        if (image != null) {
            DrawUtils.drawFitted(g2, image, area.x, area.y, area.width, area.height);
            return DrawUtils.isBright(image) ? new Color(25, 30, 70) : Color.WHITE;
        }
        g2.setColor(FALLBACK_PANEL);
        g2.fillRoundRect(area.x, area.y, area.width, area.height, 14, 14);
        return Color.WHITE;
    }
}
