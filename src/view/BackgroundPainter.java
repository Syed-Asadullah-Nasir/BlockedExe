package view;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

// Draws the blue background used by every screen.
public final class BackgroundPainter {

    private static final Color TOP_COLOR = new Color(78, 118, 205);
    private static final Color BOTTOM_COLOR = new Color(38, 48, 110);

    private BackgroundPainter() {
    }

    public static void draw(Graphics2D g2, int width, int height) {
        g2.setPaint(new GradientPaint(0, 0, TOP_COLOR, 0, height, BOTTOM_COLOR));
        g2.fillRect(0, 0, width, height);
    }
}
