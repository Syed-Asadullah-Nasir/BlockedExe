package input;

import view.GamePanel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// Turns key presses into calls on the GamePanel.
// 1, 2, 3 select a piece, the arrow keys move its preview, Enter or Space places it.
// P pauses, R restarts and M mutes.
public class KeyInputHandler extends KeyAdapter {

    private final GamePanel gamePanel;

    public KeyInputHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1:
            case KeyEvent.VK_NUMPAD1:
                gamePanel.selectSlot(0);
                break;
            case KeyEvent.VK_2:
            case KeyEvent.VK_NUMPAD2:
                gamePanel.selectSlot(1);
                break;
            case KeyEvent.VK_3:
            case KeyEvent.VK_NUMPAD3:
                gamePanel.selectSlot(2);
                break;
            case KeyEvent.VK_UP:
                gamePanel.movePreview(-1, 0);
                break;
            case KeyEvent.VK_DOWN:
                gamePanel.movePreview(1, 0);
                break;
            case KeyEvent.VK_LEFT:
                gamePanel.movePreview(0, -1);
                break;
            case KeyEvent.VK_RIGHT:
                gamePanel.movePreview(0, 1);
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                gamePanel.placeSelectedPiece();
                break;
            default:
                break;
        }
    }

    // P, R and M act when the key is RELEASED. A key that is held down sends many
    // keyPressed events, and pausing 30 times a second would make the game flicker.
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_P:
                gamePanel.togglePause();
                break;
            case KeyEvent.VK_R:
                gamePanel.restartGame();
                break;
            case KeyEvent.VK_M:
                gamePanel.toggleMute();
                break;
            default:
                break;
        }
    }
}
