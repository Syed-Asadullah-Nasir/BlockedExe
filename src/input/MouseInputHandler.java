package input;

import view.GameLayout;
import view.GamePanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Turns mouse events into calls on the GamePanel. It decides WHAT was clicked;
// the panel decides what that means for the game. It never changes the board itself.
public class MouseInputHandler extends MouseAdapter {

    private final GamePanel gamePanel;

    public MouseInputHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;   // only the left button is used
        }
        gamePanel.requestFocusInWindow();   // keeps the keyboard working

        int slot = GameLayout.findSlotAt(e.getX(), e.getY());
        if (slot >= 0) {
            gamePanel.startDrag(slot, e.getX(), e.getY());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        gamePanel.moveDrag(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        if (gamePanel.isDragging()) {
            gamePanel.dropDrag(e.getX(), e.getY());
            return;
        }

        if (GameLayout.getPauseButtonRect().contains(e.getX(), e.getY())) {
            gamePanel.togglePause();
        } else if (GameLayout.getMuteButtonRect().contains(e.getX(), e.getY())) {
            gamePanel.toggleMute();
        }
    }
}
