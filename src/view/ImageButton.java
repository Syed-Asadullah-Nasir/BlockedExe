package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// A clickable button. Its image is only the BACKGROUND, so the label is drawn on top of it.
// Pass showLabelOnImage = false for an image that already shows what it is (like the play icon).
// The button is exactly as big as its visible image (fitted into the box given to the constructor),
// so the label, the hover effect and the click area all match what the player sees.
// If the image could not be loaded (null), a plain 3D-looking button with the label is drawn instead.
public class ImageButton {

    private static final Color DEFAULT_FALLBACK_COLOR = new Color(70, 140, 235);
    // Proportions of the buttons in the asset pack (width : height), used when the image is missing.
    private static final int FALLBACK_WIDTH = 340;
    private static final int FALLBACK_HEIGHT = 100;
    // The text height is a part of the button height, so big and small buttons both look right.
    private static final float LABEL_SIZE_RATIO = 0.4f;
    private static final float MAX_LABEL_SIZE = 36f;
    // The buttons have a thick lip at the bottom, so the text is lifted a little to sit on the face.
    private static final int LIP_DIVISOR = 20;

    private final BufferedImage image;
    private final String label;
    private final Rectangle bounds;
    private final boolean showLabelOnImage;
    private final boolean darkLabel;   // dark text on a light image, white text on a dark one
    private Color fallbackColor = DEFAULT_FALLBACK_COLOR;
    private BufferedImage icon;   // a small picture drawn on the button instead of the label
    private boolean hovered;

    public ImageButton(BufferedImage image, String label, int x, int y, int width, int height) {
        this(image, label, x, y, width, height, true);
    }

    public ImageButton(BufferedImage image, String label, int x, int y, int width, int height,
                       boolean showLabelOnImage) {
        this.image = image;
        this.label = label;
        this.bounds = createBounds(image, new Rectangle(x, y, width, height));
        this.showLabelOnImage = showLabelOnImage;
        this.darkLabel = DrawUtils.isBright(image);
        this.hovered = false;
    }

    // The space the button really takes: the image (or a button-shaped box) fitted into the given box.
    // A square box (like the play button) stays as it is.
    private static Rectangle createBounds(BufferedImage image, Rectangle box) {
        if (image != null) {
            return DrawUtils.fitRect(box, image.getWidth(), image.getHeight());
        }
        if (box.width > 2 * box.height) {
            return DrawUtils.fitRect(box, FALLBACK_WIDTH, FALLBACK_HEIGHT);
        }
        return box;
    }

    // The colour of the plain button that is drawn when the image is missing.
    public void setFallbackColor(Color color) {
        this.fallbackColor = color;
    }

    // Shows this picture (for example the play triangle) on the button instead of the label.
    // If the picture is null, the label is shown as usual.
    public void setIcon(BufferedImage icon) {
        this.icon = icon;
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
            g2.drawImage(image, area.x, area.y, area.width, area.height, null);
        } else {
            drawFallbackShape(g2, area);
        }

        if (icon != null) {
            drawIcon(g2, area);
        } else if (image == null || showLabelOnImage) {
            Font font = labelFont.deriveFont(Math.min(MAX_LABEL_SIZE, area.height * LABEL_SIZE_RATIO));
            boolean dark = image != null && darkLabel;
            DrawUtils.drawButtonLabel(g2, label, font, dark,
                    area.x, area.y - area.height / LIP_DIVISOR, area.width, area.height);
        }
    }

    // The icon in the middle of the button face (the button without its bottom lip), with a soft shadow.
    private void drawIcon(Graphics2D g2, Rectangle area) {
        int lip = image == null ? Math.max(4, area.height / 10) : area.height / 7;
        int faceHeight = area.height - lip;
        int size = Math.min(area.width, faceHeight) * 45 / 100;
        Rectangle box = new Rectangle(area.x + (area.width - size) / 2,
                area.y + (faceHeight - size) / 2, size, size);
        Rectangle iconRect = DrawUtils.fitRect(box, icon.getWidth(), icon.getHeight());
        // The play triangle looks centred when it is moved a little to the right.
        int nudge = iconRect.width / 10;

        Object oldHint = g2.getRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        java.awt.Composite oldComposite = g2.getComposite();
        g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.25f));
        g2.drawImage(icon, iconRect.x + nudge + 3, iconRect.y + 3, iconRect.width, iconRect.height, null);
        g2.setComposite(oldComposite);
        g2.drawImage(icon, iconRect.x + nudge, iconRect.y, iconRect.width, iconRect.height, null);
        if (oldHint != null) {
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, oldHint);
        }
    }

    // A rounded button with a darker lip at the bottom, like the buttons of the asset pack.
    private void drawFallbackShape(Graphics2D g2, Rectangle area) {
        int arc = Math.min(area.height / 3, 28);
        int lip = Math.max(4, area.height / 10);
        Color face = hovered ? fallbackColor.brighter() : fallbackColor;

        g2.setColor(fallbackColor.darker().darker());
        g2.fillRoundRect(area.x, area.y + lip, area.width, area.height - lip, arc, arc);
        g2.setColor(face);
        g2.fillRoundRect(area.x, area.y, area.width, area.height - lip, arc, arc);
        g2.setColor(new Color(255, 255, 255, 170));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(area.x + 1, area.y + 1, area.width - 3, area.height - lip - 3, arc, arc);
        g2.setStroke(new BasicStroke(1f));
    }
}
