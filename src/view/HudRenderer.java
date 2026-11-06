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
    // The thick lip at the bottom of a button image takes about this much of its height.
    private static final int FACE_LIP_PERCENT = 14;

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
            DrawUtils.drawNineSlice(g2, scoreBackground,
                    SCORE_AREA.x, SCORE_AREA.y, SCORE_AREA.width, SCORE_AREA.height);
        } else {
            g2.setColor(FALLBACK_PANEL);
            g2.fillRoundRect(SCORE_AREA.x, SCORE_AREA.y, SCORE_AREA.width, SCORE_AREA.height, 24, 24);
        }
        // The score panel image is light, so the number must be dark on it to be readable.
        g2.setColor(iconColor(scoreBackground));
        DrawUtils.drawCenteredText(g2, String.valueOf(score), scoreFont,
                SCORE_AREA.x, SCORE_AREA.y, SCORE_AREA.width, SCORE_AREA.height);
    }

    private void drawTime(Graphics2D g2, String timeText) {
        g2.setColor(LIGHT_TEXT);
        DrawUtils.drawCenteredText(g2, timeText, timeFont, 0, TIME_Y, Constants.WINDOW_WIDTH, TIME_HEIGHT);
    }

    // The images of the pause and mute buttons are only backgrounds, so the icons are drawn here.
    // The icon is drawn on the "face" of the button: the visible image without its thick bottom lip.
    private void drawPauseButton(Graphics2D g2, Rectangle area) {
        Rectangle face = drawButtonBackground(g2, pauseImage, area);
        g2.setColor(iconColor(pauseImage));

        int barHeight = face.height * 60 / 100;
        int barWidth = Math.max(4, barHeight * 28 / 100);
        int gap = barWidth;
        int top = face.y + (face.height - barHeight) / 2;
        int left = face.x + (face.width - (2 * barWidth + gap)) / 2;
        g2.fillRoundRect(left, top, barWidth, barHeight, 4, 4);
        g2.fillRoundRect(left + barWidth + gap, top, barWidth, barHeight, 4, 4);
    }

    // A loudspeaker. Sound on: two sound waves next to it. Sound off: a cross next to it.
    private void drawMuteButton(Graphics2D g2, Rectangle area, boolean muted) {
        Rectangle face = drawButtonBackground(g2, muteImage, area);
        g2.setColor(iconColor(muteImage));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int unit = face.height * 110 / 100;   // everything is measured in parts of this size
        int centerY = face.y + face.height / 2;
        int boxWidth = unit * 16 / 100;
        int boxHalf = unit * 14 / 100;
        int coneWidth = unit * 20 / 100;
        int coneHalf = unit * 30 / 100;
        int iconWidth = boxWidth + coneWidth + unit * 40 / 100;   // speaker + waves
        int left = face.x + (face.width - iconWidth) / 2;

        int[] xs = {left, left + boxWidth, left + boxWidth + coneWidth, left + boxWidth + coneWidth, left + boxWidth, left};
        int[] ys = {centerY - boxHalf, centerY - boxHalf, centerY - coneHalf, centerY + coneHalf, centerY + boxHalf, centerY + boxHalf};
        g2.fillPolygon(xs, ys, xs.length);

        int waveX = left + boxWidth + coneWidth + unit * 8 / 100;
        if (muted) {
            int size = unit * 13 / 100;
            int crossX = waveX + size;
            g2.drawLine(crossX - size, centerY - size, crossX + size, centerY + size);
            g2.drawLine(crossX - size, centerY + size, crossX + size, centerY - size);
        } else {
            int small = unit * 16 / 100;
            int big = unit * 30 / 100;
            g2.drawArc(waveX - small, centerY - small, 2 * small, 2 * small, -50, 100);
            g2.drawArc(waveX - big, centerY - big, 2 * big, 2 * big, -50, 100);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    // Draws the button background (or a plain rounded box if the image is missing).
    // Returns the face of the button: where the icon belongs.
    private Rectangle drawButtonBackground(Graphics2D g2, BufferedImage image, Rectangle area) {
        Rectangle visible = area;
        if (image != null) {
            visible = DrawUtils.fitRect(area, image.getWidth(), image.getHeight());
            g2.drawImage(image, visible.x, visible.y, visible.width, visible.height, null);
        } else {
            g2.setColor(FALLBACK_PANEL);
            g2.fillRoundRect(area.x, area.y, area.width, area.height, 14, 14);
        }
        int lip = visible.height * FACE_LIP_PERCENT / 100;
        return new Rectangle(visible.x, visible.y, visible.width, visible.height - lip);
    }

    // Dark icon on a light button, white icon on a dark one.
    private Color iconColor(BufferedImage image) {
        return DrawUtils.isBright(image) ? new Color(25, 30, 70) : Color.WHITE;
    }
}
