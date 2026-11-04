package effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

// A text like "+25" that floats upwards from a point and fades out.
public class ScorePopupEffect extends VisualEffect {

    private static final long DURATION_MILLIS = 900;
    private static final int RISE_PIXELS = 70;

    private final String text;
    private final int centerX;
    private final int startY;
    private final Font font;

    public ScorePopupEffect(String text, int centerX, int startY, Font font) {
        super(DURATION_MILLIS);
        this.text = text;
        this.centerX = centerX;
        this.startY = startY;
        this.font = font;
    }

    public String getText() {
        return text;
    }

    @Override
    public void draw(Graphics2D g2) {
        double progress = getProgress();
        // Fully visible for the first half, then fades.
        float alpha = (float) Math.min(1.0, 2.0 * (1.0 - progress));
        int y = startY - (int) (RISE_PIXELS * progress);

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        int x = centerX - metrics.stringWidth(text) / 2;

        g2.setColor(new Color(20, 20, 40));          // dark outline so it is readable on any block
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(new Color(255, 230, 90));
        g2.drawString(text, x, y);
        g2.setComposite(oldComposite);
    }
}
