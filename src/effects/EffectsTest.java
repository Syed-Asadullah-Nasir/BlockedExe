package effects;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

// Tests the animations without a window: time handling, and that drawing really changes pixels.
public class EffectsTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testStartsAtZero();
        testProgressAndFinish();
        testNeverGoesPastTheEnd();
        testNegativeTimeIgnored();
        testBadDurationRejected();
        testPopDrawsAndFades();
        testClearShrinks();
        testPopupFloatsAndFades();
        testEffectsActAsDrawables();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    private static void testStartsAtZero() {
        VisualEffect effect = new PlacementPopEffect(cells());
        check("a new effect has progress 0", effect.getProgress() == 0.0);
        check("a new effect is not finished", !effect.isFinished());
    }

    private static void testProgressAndFinish() {
        VisualEffect effect = new PlacementPopEffect(cells());
        long half = effect.getDurationMillis() / 2;
        effect.update(half);
        check("half the time = about half the progress", Math.abs(effect.getProgress() - 0.5) < 0.01);
        check("still running at half time", !effect.isFinished());
        effect.update(effect.getDurationMillis());
        check("finished after the full duration", effect.isFinished());
    }

    private static void testNeverGoesPastTheEnd() {
        VisualEffect effect = new ClearFlashEffect(cells());
        effect.update(1000000);
        check("progress is capped at 1.0", effect.getProgress() == 1.0);
    }

    private static void testNegativeTimeIgnored() {
        VisualEffect effect = new ClearFlashEffect(cells());
        effect.update(-500);
        effect.update(0);
        check("negative or zero time changes nothing", effect.getProgress() == 0.0);
    }

    private static void testBadDurationRejected() {
        boolean rejected = false;
        try {
            new VisualEffect(0) {
                @Override
                public void draw(Graphics2D g2) {
                }
            };
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check("a zero duration is rejected", rejected);
    }

    private static void testPopDrawsAndFades() {
        VisualEffect effect = new PlacementPopEffect(cells());
        int start = changedPixels(effect);
        effect.update(effect.getDurationMillis() - 10);
        int late = changedPixels(effect);
        effect.update(10);
        check("the pop is visible at the start", start > 0);
        check("the pop is weaker at the end than at the start", late < start * 2 && changedPixels(effect) == 0);
    }

    private static void testClearShrinks() {
        VisualEffect effect = new ClearFlashEffect(cells());
        int start = changedPixels(effect);
        effect.update(effect.getDurationMillis() * 3 / 4);
        int later = changedPixels(effect);
        effect.update(effect.getDurationMillis());
        int end = changedPixels(effect);
        check("the clear effect is visible at the start", start > 0);
        check("the cleared cell gets smaller", later < start);
        check("nothing is left at the end", end == 0);
    }

    private static void testPopupFloatsAndFades() {
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 22);
        ScorePopupEffect popup = new ScorePopupEffect("+25", 100, 100, font);
        check("the popup keeps its text", popup.getText().equals("+25"));
        int start = changedPixels(popup);
        popup.update(popup.getDurationMillis() / 2);
        int[] box = boundsOfChange(popup);
        popup.update(popup.getDurationMillis());
        int end = changedPixels(popup);
        check("the popup is visible at the start", start > 0);
        check("the popup has moved up by the middle", box != null && box[1] < 100);
        check("the popup is gone at the end", end == 0);
    }

    private static void testEffectsActAsDrawables() {
        List<Drawable> list = new ArrayList<>();
        list.add(new PlacementPopEffect(cells()));
        list.add(new ClearFlashEffect(cells()));
        list.add(new ScorePopupEffect("+1", 50, 50, new Font(Font.SANS_SERIF, Font.BOLD, 20)));
        boolean noCrash = true;
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            for (Drawable drawable : list) {
                drawable.draw(g2);
            }
        } catch (RuntimeException e) {
            noCrash = false;
        }
        g2.dispose();
        check("different effects can be drawn through the Drawable interface", noCrash);
    }

    // ----- helpers -----

    private static List<EffectCell> cells() {
        List<EffectCell> list = new ArrayList<>();
        list.add(new EffectCell(60, 60, 40, null, new Color(200, 50, 50)));
        return list;
    }

    // Draws the effect on a transparent 200x200 image and counts the pixels that are not empty.
    private static int changedPixels(Drawable effect) {
        BufferedImage image = render(effect);
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) > 8) {
                    count++;
                }
            }
        }
        return count;
    }

    // {minX, minY, maxX, maxY} of the drawn pixels, or null if nothing was drawn.
    private static int[] boundsOfChange(Drawable effect) {
        BufferedImage image = render(effect);
        int[] box = {Integer.MAX_VALUE, Integer.MAX_VALUE, -1, -1};
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) > 8) {
                    box[0] = Math.min(box[0], x);
                    box[1] = Math.min(box[1], y);
                    box[2] = Math.max(box[2], x);
                    box[3] = Math.max(box[3], y);
                }
            }
        }
        return box[2] < 0 ? null : box;
    }

    private static BufferedImage render(Drawable effect) {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        effect.draw(g2);
        g2.dispose();
        return image;
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }
}
