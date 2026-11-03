package util;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ResourceTestWindow extends JFrame {

    private final BufferedImage blockImage;
    private final BufferedImage missingImage;
    private final Font customFont;

    public ResourceTestWindow() {
        blockImage = ResourceLoader.loadImage(Constants.BLOCK_IMAGE_PATHS[Constants.COLOR_BLUE]);
        // This file does not exist on purpose, to test the error handling.
        missingImage = ResourceLoader.loadImage("images/blocks/doesNotExist.png");
        customFont = ResourceLoader.loadFont(Constants.FONT_PATH, 28f);

        setTitle("Resource test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new TestPanel());
        pack();
        setLocationRelativeTo(null);
    }

    private class TestPanel extends JPanel {

        TestPanel() {
            setPreferredSize(new Dimension(520, 260));
            setBackground(new Color(30, 30, 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            drawImageOrPlaceholder(g2, blockImage, 30, 30, "block missing");
            drawImageOrPlaceholder(g2, missingImage, 140, 30, "missing (expected)");

            g2.setColor(Color.WHITE);
            g2.setFont(customFont);
            g2.drawString("BLOCKED.exe font test", 30, 190);
        }

        private void drawImageOrPlaceholder(Graphics2D g2, BufferedImage image,
                                            int x, int y, String label) {
            if (image != null) {
                g2.drawImage(image, x, y, 64, 64, null);
            } else {
                g2.setColor(Color.GRAY);
                g2.fillRect(x, y, 64, 64);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.drawString(label, x, y + 80);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ResourceTestWindow().setVisible(true);
            }
        });
    }
}
