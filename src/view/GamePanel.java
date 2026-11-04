package view;

import input.MouseInputHandler;
import logic.Game;
import logic.PlacementResult;
import model.BlockPiece;
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

// The gameplay screen. A Swing Timer calls actionPerformed about 60 times per second:
// first update() changes the state, then repaint() asks Swing to draw it.
// paintComponent() only draws, it never changes the game.
public class GamePanel extends JPanel implements ActionListener {

    private static final int TIMER_DELAY_MS = 16;
    private static final int NO_SLOT = -1;

    private final GameFrame gameFrame;
    private final BoardRenderer boardRenderer;
    private final HudRenderer hudRenderer;
    private final Timer timer;

    private Game game;
    private long elapsedNanos;
    private long lastTickNanos;
    private boolean soundMuted;   // Placeholder: SoundManager takes over in Phase 11

    // Drag and drop state: which tray slot is being dragged and where the pointer is.
    private int dragSlot = NO_SLOT;
    private int dragX;
    private int dragY;

    public GamePanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.boardRenderer = new BoardRenderer();
        this.hudRenderer = new HudRenderer(gameFrame.createUiFont(12f));
        this.timer = new Timer(TIMER_DELAY_MS, this);
        this.soundMuted = false;

        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);

        MouseInputHandler mouseHandler = new MouseInputHandler(this);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void startGame(GameMode mode) {
        game = new Game(mode);
        elapsedNanos = 0;
        lastTickNanos = System.nanoTime();
        dragSlot = NO_SLOT;
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public void stopGame() {
        timer.stop();
        dragSlot = NO_SLOT;
    }

    // ----- called by the mouse handler -----

    public boolean isDragging() {
        return dragSlot != NO_SLOT;
    }

    // Starts dragging the piece in this tray slot. Ignored if the slot is empty or the game is not running.
    public void startDrag(int slotIndex, int mouseX, int mouseY) {
        if (!canPlay() || game.getPiece(slotIndex) == null) {
            return;
        }
        dragSlot = slotIndex;
        dragX = mouseX;
        dragY = mouseY;
    }

    public void moveDrag(int mouseX, int mouseY) {
        if (isDragging()) {
            dragX = mouseX;
            dragY = mouseY;
        }
    }

    // Releases the piece. If the spot is valid it is placed; otherwise nothing changes
    // and the piece simply stays in its tray slot.
    public void dropDrag(int mouseX, int mouseY) {
        if (!isDragging()) {
            return;
        }
        moveDrag(mouseX, mouseY);

        int slot = dragSlot;
        BlockPiece piece = game.getPiece(slot);
        dragSlot = NO_SLOT;

        if (piece != null) {
            tryPlacePiece(slot, getDragRow(piece), getDragCol(piece));
        }
    }

    public void cancelDrag() {
        dragSlot = NO_SLOT;
    }

    public void toggleMute() {
        soundMuted = !soundMuted;
    }

    // TEMPORARY: lets you leave the game screen until the real pause button arrives in Phase 8.
    public void leaveToMenu() {
        gameFrame.showScreen(GameFrame.SCREEN_MENU);
    }

    // ----- game loop -----

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    // All changes happen here, never in paintComponent().
    // Phase 9 turns this simple time counting into the Time Attack countdown.
    private void update() {
        long now = System.nanoTime();
        long delta = now - lastTickNanos;
        lastTickNanos = now;

        if (canPlay()) {
            elapsedNanos += delta;
        }
    }

    // The one place where a move is made. Sounds (Phase 11) and animations (Phase 12)
    // will be started from here, using the PlacementResult.
    private PlacementResult tryPlacePiece(int slotIndex, int row, int col) {
        return game.tryPlace(slotIndex, row, col);
    }

    private boolean canPlay() {
        return game != null && game.getState() == GameState.PLAYING;
    }

    // ----- drawing -----

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
        boardRenderer.drawTray(g2, game, dragSlot);
        drawDragPreview(g2);
    }

    // Draws the piece following the pointer and, on top of it, the green or red preview
    // of the board cells it would fill. It only asks canPlace() a question; it changes nothing.
    private void drawDragPreview(Graphics2D g2) {
        if (!isDragging()) {
            return;
        }
        BlockPiece piece = game.getPiece(dragSlot);
        if (piece == null) {
            return;
        }

        int left = dragX - piece.getWidth() * Constants.CELL_SIZE / 2;
        int top = dragY - piece.getHeight() * Constants.CELL_SIZE / 2;
        boardRenderer.drawPiece(g2, piece, left, top, Constants.CELL_SIZE);

        int row = getDragRow(piece);
        int col = getDragCol(piece);
        boolean valid = game.getBoard().canPlace(piece, row, col);
        boardRenderer.drawGhost(g2, game.getBoard(), piece, row, col, valid);
    }

    // The piece is held by its center. Its top-left corner decides the board cell.
    private int getDragCol(BlockPiece piece) {
        int left = dragX - piece.getWidth() * Constants.CELL_SIZE / 2;
        return GameLayout.nearestColumn(left);
    }

    private int getDragRow(BlockPiece piece) {
        int top = dragY - piece.getHeight() * Constants.CELL_SIZE / 2;
        return GameLayout.nearestRow(top);
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
}
