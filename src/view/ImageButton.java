package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// A clickable button drawn from an image. It grows a little when the mouse is over it.
// If the image could not be loaded (null), a plain rounded button with the label is drawn instead.
public class ImageButton {

    private static final Color NORMAL_COLOR = new Color(70, 105, 210);
    private static final Color HOVER_COLOR = new Color(100, 140, 240);

    private final BufferedImage image;
    private final String label;
    private final Rectangle bounds;
    private boolean hovered;

    public ImageButton(BufferedImage image, String label, int x, int y, int width, int height) {
        this.image = image;
        this.label = label;
        this.bounds = new Rectangle(x, y, width, height);
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

        DrawUtils.drawCenteredText(g2, label, labelFont, area.x, area.y, area.width, area.height);
    }
}
