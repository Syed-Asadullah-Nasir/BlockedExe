package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// A clickable button. Its image is only the BACKGROUND, so the label is drawn on top of it.
// Pass showLabelOnImage = false for an image that already shows what it is (like the play icon).
// It grows a little when the mouse is over it.
// If the image could not be loaded (null), a plain rounded button with the label is drawn instead.
public class ImageButton {

    private static final Color NORMAL_COLOR = new Color(70, 105, 210);
    private static final Color HOVER_COLOR = new Color(100, 140, 240);

    // The text height is a part of the button height, so big and small buttons both look right.
    private static final float LABEL_SIZE_RATIO = 0.4f;
    // The buttons have a thick lip at the bottom, so the text is lifted a little to sit on the face.
    private static final int LIP_DIVISOR = 20;

    private final BufferedImage image;
    private final String label;
    private final Rectangle bounds;
    private final boolean showLabelOnImage;
    private final boolean darkLabel;   // dark text on a light image, white text on a dark one
    private boolean hovered;

    public ImageButton(BufferedImage image, String label, int x, int y, int width, int height) {
        this(image, label, x, y, width, height, true);
    }

    public ImageButton(BufferedImage image, String label, int x, int y, int width, int height,
                       boolean showLabelOnImage) {
        this.image = image;
        this.label = label;
        this.bounds = new Rectangle(x, y, width, height);
        this.showLabelOnImage = showLabelOnImage;
        this.darkLabel = DrawUtils.isBright(image);
        this.hovered = false;
    }

    public boolean contains(int pointX, int pointY) {
        return bounds.contains(pointX, pointY);
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public void draw(Graphics2D g2, Font labelFont) {
        Rectangle area = new Rectangle(bounds);
        if (hovered) {
            area.grow(5, 5);
        }

        if (image != null) {
            DrawUtils.drawFitted(g2, image, area.x, area.y, area.width, area.height);
            if (showLabelOnImage) {
                DrawUtils.drawButtonLabel(g2, label, labelFont.deriveFont(area.height * LABEL_SIZE_RATIO),
                        darkLabel, area.x, area.y - area.height / LIP_DIVISOR, area.width, area.height);
            }
        } else {
            drawFallback(g2, area, labelFont);
        }
    }

    private void drawFallback(Graphics2D g2, Rectangle area, Font labelFont) {
        g2.setColor(hovered ? HOVER_COLOR : NORMAL_COLOR);
        g2.fillRoundRect(area.x, area.y, area.width, area.height, 24, 24);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(area.x, area.y, area.width, area.height, 24, 24);
        g2.setStroke(new BasicStroke(1f));

        DrawUtils.drawButtonLabel(g2, label, labelFont.deriveFont(area.height * LABEL_SIZE_RATIO),
                false, area.x, area.y, area.width, area.height);
    }
}
