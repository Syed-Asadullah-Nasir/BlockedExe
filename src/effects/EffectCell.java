package effects;

import java.awt.Color;
import java.awt.image.BufferedImage;

// One square an effect works on: where it is on screen (pixels) and how it looks.
// The image is the block picture; if it is missing the colour is used instead.
public class EffectCell {

    private final int x;
    private final int y;
    private final int size;
    private final BufferedImage image;
    private final Color color;

    public EffectCell(int x, int y, int size, BufferedImage image, Color color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.image = image;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    // Draws the cell scaled around its own center. scale 1.0 = normal size.
    public void drawScaled(java.awt.Graphics2D g2, double scale) {
        int scaled = (int) Math.round(size * scale);
        if (scaled < 1) {
            return;
        }
        int left = x + (size - scaled) / 2;
        int top = y + (size - scaled) / 2;
        if (image != null) {
            g2.drawImage(image, left, top, scaled, scaled, null);
        } else if (color != null) {
            g2.setColor(color);
            g2.fillRoundRect(left + 1, top + 1, scaled - 2, scaled - 2, 8, 8);
        }
    }
}
