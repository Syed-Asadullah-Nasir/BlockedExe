package view;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// Small drawing helpers shared by all screens.
public final class DrawUtils {

    private DrawUtils() {
    }

    // Smooth lines, text and scaled images.
    public static void enableQuality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    // Draws the image inside the box, keeping its proportions and centering it.
    public static void drawFitted(Graphics2D g2, BufferedImage image, int x, int y, int width, int height) {
        double scale = Math.min((double) width / image.getWidth(), (double) height / image.getHeight());
        int drawWidth = (int) Math.round(image.getWidth() * scale);
        int drawHeight = (int) Math.round(image.getHeight() * scale);
        int drawX = x + (width - drawWidth) / 2;
        int drawY = y + (height - drawHeight) / 2;
        g2.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
    }

    // Draws the image so it fills the whole box (it may be stretched).
    public static void drawStretched(Graphics2D g2, BufferedImage image, int x, int y, int width, int height) {
        g2.drawImage(image, x, y, width, height, null);
    }

    // Centers the text horizontally and vertically inside the box.
    public static void drawCenteredText(Graphics2D g2, String text, Font font,
                                        int x, int y, int width, int height) {
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(text)) / 2;
        int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(text, textX, textY);
    }

    // Starts the text at x and centers it vertically inside the given height.
    public static void drawTextLeft(Graphics2D g2, String text, Font font, int x, int y, int height) {
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(text, x, textY);
    }
}
