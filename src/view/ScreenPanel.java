package view;

import util.Constants;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// Base class for the menu-style screens (menu, mode selection, instructions).
// It draws the background and the buttons and handles hover and clicks.
// Each screen only adds its own buttons, text and what happens on a click.
public abstract class ScreenPanel extends JPanel {

    protected final GameFrame gameFrame;
    protected final Font buttonFont;
    private final List<ImageButton> buttons = new ArrayList<>();

    protected ScreenPanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.buttonFont = gameFrame.createUiFont(26f);

        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));

        ButtonMouseListener mouseListener = new ButtonMouseListener();
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
    }

    protected void addButton(ImageButton button) {
        buttons.add(button);
    }

    // Called when the player clicks a button of this screen.
    protected abstract void onButtonClicked(ImageButton button);

    // Screens override this to draw their title and text. Buttons are drawn afterwards.
    protected void paintContent(Graphics2D g2) {
    }

    // Title with a soft shadow, centered across the screen.
    protected void drawTitle(Graphics2D g2, String text, Font font, int y, int height) {
        g2.setColor(new Color(0, 0, 0, 90));
        DrawUtils.drawCenteredText(g2, text, font, 3, y + 3, getWidth(), height);
        g2.setColor(Color.WHITE);
        DrawUtils.drawCenteredText(g2, text, font, 0, y, getWidth(), height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        DrawUtils.enableQuality(g2);

        BackgroundPainter.draw(g2, getWidth(), getHeight());
        paintContent(g2);
        for (ImageButton button : buttons) {
            button.draw(g2, buttonFont);
        }
    }

    private void updateHover(int mouseX, int mouseY) {
        boolean changed = false;
        boolean overAnyButton = false;

        for (ImageButton button : buttons) {
            boolean over = button.contains(mouseX, mouseY);
            if (over != button.isHovered()) {
                button.setHovered(over);
                changed = true;
            }
            if (over) {
                overAnyButton = true;
            }
        }

        setCursor(Cursor.getPredefinedCursor(overAnyButton ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        if (changed) {
            repaint();
        }
    }

    private ImageButton findButtonAt(int mouseX, int mouseY) {
        for (ImageButton button : buttons) {
            if (button.contains(mouseX, mouseY)) {
                return button;
            }
        }
        return null;
    }

    private class ButtonMouseListener extends MouseAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            updateHover(e.getX(), e.getY());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            updateHover(e.getX(), e.getY());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            updateHover(-1, -1);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            ImageButton clicked = findButtonAt(e.getX(), e.getY());
            if (clicked != null) {
                // Clear the hover first, so the button is not stuck "hovered" when we come back.
                updateHover(-1, -1);
                onButtonClicked(clicked);
            }
        }
    }
}
