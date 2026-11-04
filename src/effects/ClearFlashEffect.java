package effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.List;

// The cleared cells flash white and shrink to nothing. The board has already removed them,
// so this effect draws the old blocks on top of the now empty board cells.
public class ClearFlashEffect extends VisualEffect {

    private static final long DURATION_MILLIS = 420;

    private final List<EffectCell> cells;

    public ClearFlashEffect(List<EffectCell> cells) {
        super(DURATION_MILLIS);
        this.cells = cells;
    }

    @Override
    public void draw(Graphics2D g2) {
        double progress = getProgress();
        double scale = 1.0 - progress * progress;       // shrinks slowly first, then fast
        float flash = (float) Math.max(0.0, 0.85 * (1.0 - progress * 1.5));

        Composite oldComposite = g2.getComposite();
        for (EffectCell cell : cells) {
            cell.drawScaled(g2, scale);
            if (flash > 0f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flash));
                g2.setColor(Color.WHITE);
                int size = (int) Math.round(cell.getSize() * scale);
                int left = cell.getX() + (cell.getSize() - size) / 2;
                int top = cell.getY() + (cell.getSize() - size) / 2;
                g2.fillRoundRect(left, top, size, size, 10, 10);
                g2.setComposite(oldComposite);
            }
        }
    }
}
