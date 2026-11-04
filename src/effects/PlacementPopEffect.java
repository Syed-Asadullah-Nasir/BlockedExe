package effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.List;

// A short white glow that appears on the cells of a piece the moment it is placed,
// grows a little and fades away.
public class PlacementPopEffect extends VisualEffect {

    private static final long DURATION_MILLIS = 260;
    private static final int GROW_PIXELS = 8;

    private final List<EffectCell> cells;

    public PlacementPopEffect(List<EffectCell> cells) {
        super(DURATION_MILLIS);
        this.cells = cells;
    }

    @Override
    public void draw(Graphics2D g2) {
        double progress = getProgress();
        float alpha = (float) (0.7 * (1.0 - progress));
        int grow = (int) (GROW_PIXELS * progress);

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(Color.WHITE);
        for (EffectCell cell : cells) {
            g2.fillRoundRect(cell.getX() - grow, cell.getY() - grow,
                    cell.getSize() + 2 * grow, cell.getSize() + 2 * grow, 12, 12);
        }
        g2.setComposite(oldComposite);
    }
}
