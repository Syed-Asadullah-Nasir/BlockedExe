package view;

import java.awt.Font;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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

    // The rectangle an image of this size takes when it is fitted inside the box:
    // same proportions, centered. This is where drawFitted really draws.
    public static Rectangle fitRect(Rectangle box, int imageWidth, int imageHeight) {
        double scale = Math.min((double) box.width / imageWidth, (double) box.height / imageHeight);
        int width = (int) Math.round(imageWidth * scale);
        int height = (int) Math.round(imageHeight * scale);
        return new Rectangle(box.x + (box.width - width) / 2, box.y + (box.height - height) / 2, width, height);
    }

    // Returns the font, made smaller if needed, so the text is not wider than maxWidth.
    public static Font fitFont(Graphics2D g2, String text, Font font, int maxWidth) {
        Font result = font;
        while (result.getSize() > 8 && g2.getFontMetrics(result).stringWidth(text) > maxWidth) {
            result = result.deriveFont(result.getSize2D() - 1f);
        }
        return result;
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
        g2.setFont(fitFont(g2, text, font, width));   // a text wider than its box gets smaller
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

    // True if the middle of the image is light. Button images are only backgrounds, so the
    // text or icon drawn on top must be dark on a light button and white on a dark one.
    public static boolean isBright(BufferedImage image) {
        if (image == null) {
            return false;
        }
        long total = 0;
        int samples = 0;
        for (int i = 3; i <= 7; i += 2) {
            for (int j = 3; j <= 7; j += 2) {
                int pixel = image.getRGB(image.getWidth() * i / 10, image.getHeight() * j / 10);
                if ((pixel >>> 24) < 128) {
                    continue;   // transparent: not part of the button
                }
                int red = (pixel >> 16) & 255;
                int green = (pixel >> 8) & 255;
                int blue = pixel & 255;
                total += (299 * red + 587 * green + 114 * blue) / 1000;
                samples++;
            }
        }
        return samples > 0 && total / samples > 170;
    }

    // Draws a label in the middle of a box. The text shrinks if it is too wide for the box.
    // White text gets a soft shadow so it stays readable on any button colour.
    public static void drawButtonLabel(Graphics2D g2, String text, Font font, boolean darkText,
                                       int x, int y, int width, int height) {
        Font useFont = fitFont(g2, text, font, width * 85 / 100);

        if (!darkText) {
            g2.setColor(new Color(0, 0, 0, 110));
            drawCenteredText(g2, text, useFont, x + 2, y + 2, width, height);
        }
        g2.setColor(darkText ? new Color(25, 30, 70) : Color.WHITE);
        drawCenteredText(g2, text, useFont, x, y, width, height);
    }
}
