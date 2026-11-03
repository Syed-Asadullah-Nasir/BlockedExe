package view;

import logic.Game;
import model.GameMode;
import model.GameState;
import util.Constants;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// The gameplay screen. A Swing Timer calls actionPerformed about 60 times per second:
// first update() changes the state, then repaint() asks Swing to draw it.
// paintComponent() only draws, it never changes the game.
public class GamePanel extends JPanel implements ActionListener {

    private static final int TIMER_DELAY_MS = 16;

    private final GameFrame gameFrame;
    private final BoardRenderer boardRenderer;
    private final HudRenderer hudRenderer;
    private final Timer timer;

    private Game game;
    private long elapsedNanos;
    private long lastTickNanos;
    private boolean soundMuted;   // Placeholder: SoundManager takes over in Phase 11

    public GamePanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.boardRenderer = new BoardRenderer();
        this.hudRenderer = new HudRenderer(gameFrame.createUiFont(12f));
        this.timer = new Timer(TIMER_DELAY_MS, this);
        this.soundMuted = false;

        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);
        addMouseListener(new TemporaryMouseHandler());
    }

    public void startGame(GameMode mode) {
        game = new Game(mode);
        elapsedNanos = 0;
        lastTickNanos = System.nanoTime();
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public void stopGame() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    // All changes happen here, never in paintComponent().
    // Phase 9 turns this simple time counting into pause-safe timing and the Time Attack countdown.
    private void update() {
        long now = System.nanoTime();
        long delta = now - lastTickNanos;
        lastTickNanos = now;

        if (game != null && game.getState() == GameState.PLAYING) {
            elapsedNanos += delta;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        DrawUtils.enableQuality(g2);

        BackgroundPainter.draw(g2, getWidth(), getHeight());
        if (game == null) {
            return;
        }

        hudRenderer.draw(g2, game.getScore(), game.getBestScore(), getTimeText(), soundMuted);
        boardRenderer.drawBoard(g2, game.getBoard());
        boardRenderer.drawTray(g2, game);
    }

    // Classic shows the time played, Time Attack shows the time left.
    private String getTimeText() {
        long elapsedMillis = elapsedNanos / 1000000L;
        if (game.getMode() == GameMode.TIME_ATTACK) {
            long totalMillis = Constants.TIME_ATTACK_SECONDS * 1000L;
            long remainingMillis = Math.max(0, totalMillis - elapsedMillis);
            return "TIME LEFT " + formatTime(remainingMillis);
        }
        return "TIME " + formatTime(elapsedMillis);
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // TEMPORARY: lets you leave the game screen until Phase 7 adds the real mouse handling.
    // Clicking the pause button goes back to the main menu.
    private class TemporaryMouseHandler extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1
                    && GameLayout.getPauseButtonRect().contains(e.getX(), e.getY())) {
                gameFrame.showScreen(GameFrame.SCREEN_MENU);
            }
        }
    }
}
