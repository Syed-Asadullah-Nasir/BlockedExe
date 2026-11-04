package view;

import audio.SoundEffect;
import audio.SoundManager;
import effects.Drawable;
import effects.VisualEffect;
import input.KeyInputHandler;
import input.MouseInputHandler;
import logic.Game;
import logic.PlacementResult;
import logic.ScoreManager;
import model.BlockPiece;
import model.CellPosition;
import model.GameMode;
import model.GameState;
import util.Constants;
import util.TimeFormat;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

// The gameplay screen. A Swing Timer calls actionPerformed about 60 times per second:
// first update() changes the state, then repaint() asks Swing to draw it.
// paintComponent() only draws, it never changes the game.
public class GamePanel extends JPanel implements ActionListener {

    private static final int TIMER_DELAY_MS = 16;
    // One tick can never add more than this. If the computer sleeps or the window freezes,
    // the player does not lose a whole Time Attack round in a single tick.
    private static final long MAX_TICK_NANOS = 250000000L;
    private static final int NO_SLOT = -1;
    private static final int START_PREVIEW_POSITION = 3;

    private final GameFrame gameFrame;
    private final BoardRenderer boardRenderer;
    private final HudRenderer hudRenderer;
    private final OverlayRenderer overlayRenderer;
    private final Timer timer;
    private final ScoreManager scoreManager;   // one for the whole program: keeps the best scores
    private final SoundManager soundManager;   // one for the whole program: plays the sounds
    private final EffectFactory effectFactory;
    private final List<VisualEffect> effects = new ArrayList<>();   // animations that are playing now

    private Game game;
    private long lastTickNanos;
    private boolean endHandled;   // the end of the current game (save + sound) was already dealt with

    // Mouse drag state: which tray slot is being dragged and where the pointer is.
    private int dragSlot = NO_SLOT;
    private int dragX;
    private int dragY;

    // Keyboard state: the selected tray slot and the board cell where its preview sits.
    private int selectedSlot = NO_SLOT;
    private int previewRow = START_PREVIEW_POSITION;
    private int previewCol = START_PREVIEW_POSITION;

    public GamePanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.boardRenderer = new BoardRenderer();
        this.hudRenderer = new HudRenderer(gameFrame.createUiFont(12f));
        this.overlayRenderer = new OverlayRenderer(gameFrame.createUiFont(12f));
        this.timer = new Timer(TIMER_DELAY_MS, this);
        this.scoreManager = new ScoreManager();
        this.scoreManager.loadBestScores();
        this.soundManager = new SoundManager();
        this.soundManager.loadSounds();
        this.effectFactory = new EffectFactory(boardRenderer, gameFrame.createUiFont(22f));

        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);

        MouseInputHandler mouseHandler = new MouseInputHandler(this);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addKeyListener(new KeyInputHandler(this));
    }

    public void startGame(GameMode mode) {
        game = new Game(mode, scoreManager);
        resetTimeAndInput();
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    // Called whenever the game screen is left (menu, exit).
    public void stopGame() {
        saveBestScores();
        timer.stop();
        effects.clear();
        cancelDrag();
        selectedSlot = NO_SLOT;
    }

    // Counts the score of a game that is still running as a best score and writes the file.
    // Also called when the window is closed. Does nothing if no best score changed.
    public void saveBestScores() {
        if (game != null) {
            game.commitBestScore();
        }
        scoreManager.saveBestScoresIfChanged();
    }

    // R key and the Restart buttons: a fresh game in the same mode.
    public void restartGame() {
        if (game == null) {
            return;
        }
        saveBestScores();   // do not lose the score of the game we are leaving
        game.restart();
        resetTimeAndInput();
    }

    // M key and mute button.
    public void toggleMute() {
        soundManager.toggleMute();
    }

    // P key and pause button. Pausing also drops a piece that is being dragged.
    public void togglePause() {
        if (game == null) {
            return;
        }
        if (game.getState() == GameState.PLAYING) {
            game.pause();
            cancelDrag();
        } else if (game.getState() == GameState.PAUSED) {
            game.resume();
        }
    }

    // ----- overlay screens (called by MouseInputHandler) -----

    // True while the pause screen or an end-of-game screen is on top of the game.
    public boolean isOverlayShowing() {
        return game != null && game.getState() != GameState.PLAYING;
    }

    // Does what the clicked overlay button says. A click anywhere else does nothing.
    public void clickOverlay(int mouseX, int mouseY) {
        if (!isOverlayShowing()) {
            return;
        }
        OverlayAction action = overlayRenderer.actionAt(game.getState(), mouseX, mouseY);
        if (action == null) {
            return;
        }

        overlayRenderer.clearHover();
        setCursor(Cursor.getDefaultCursor());
        switch (action) {
            case RESUME:
                game.resume();
                break;
            case RESTART:
                restartGame();
                break;
            case MAIN_MENU:
                gameFrame.showScreen(GameFrame.SCREEN_MENU);
                break;
            default:
                break;
        }
    }

    // Highlights an overlay button and shows the hand cursor while the pointer is over one.
    public void hoverAt(int mouseX, int mouseY) {
        boolean overButton = false;
        if (isOverlayShowing()) {
            overButton = overlayRenderer.updateHover(game.getState(), mouseX, mouseY);
        } else {
            overlayRenderer.clearHover();
        }
        setCursor(Cursor.getPredefinedCursor(overButton ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    // ----- mouse (called by MouseInputHandler) -----

    public boolean isDragging() {
        return dragSlot != NO_SLOT;
    }

    // Starts dragging the piece in this tray slot. Ignored if the slot is empty or the game is not running.
    public void startDrag(int slotIndex, int mouseX, int mouseY) {
        if (!canPlay() || game.getPiece(slotIndex) == null) {
            return;
        }
        selectedSlot = NO_SLOT;   // the mouse takes over from the keyboard
        dragSlot = slotIndex;
        dragX = mouseX;
        dragY = mouseY;
        soundManager.play(SoundEffect.SELECT);
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

    // ----- keyboard (called by KeyInputHandler) -----

    // Keys 1, 2, 3. Ignored if the slot is empty or the game is not running.
    public void selectSlot(int slotIndex) {
        if (!canPlay() || isDragging()) {
            return;
        }
        BlockPiece piece = game.getPiece(slotIndex);
        if (piece == null) {
            return;
        }
        selectedSlot = slotIndex;
        keepPreviewInsideBoard(piece);
        soundManager.play(SoundEffect.SELECT);
    }

    // Arrow keys. The preview stops at the edges of the board.
    public void movePreview(int rowChange, int colChange) {
        if (!canPlay() || selectedSlot == NO_SLOT) {
            return;
        }
        BlockPiece piece = game.getPiece(selectedSlot);
        if (piece == null) {
            selectedSlot = NO_SLOT;
            return;
        }
        previewRow += rowChange;
        previewCol += colChange;
        keepPreviewInsideBoard(piece);
    }

    // Enter and Space. A rejected move keeps the piece selected.
    public void placeSelectedPiece() {
        if (!canPlay() || selectedSlot == NO_SLOT) {
            return;
        }
        PlacementResult result = tryPlacePiece(selectedSlot, previewRow, previewCol);
        if (result.isPlaced()) {
            selectedSlot = NO_SLOT;
        }
    }

    // ----- game loop -----

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    // All changes happen here, never in paintComponent().
    // The time that passed since the last tick is measured every tick, but Game only counts it
    // while the game is running. So a pause can never skip time, and after Resume the clock
    // simply continues. Time Attack ends inside Game when the time limit is reached.
    private void update() {
        long now = System.nanoTime();
        long delta = Math.min(now - lastTickNanos, MAX_TICK_NANOS);
        lastTickNanos = now;

        if (game == null) {
            return;
        }
        game.addElapsedTime(delta);
        handleGameEnd();
        updateEffects(delta);

        // The game may have just ended or been paused: a piece in the air cannot be dropped any more.
        if (!canPlay()) {
            cancelDrag();
        }
    }

    // The one place where a move is made. The PlacementResult tells what happened, so the
    // sound and the animations are chosen here.
    private PlacementResult tryPlacePiece(int slotIndex, int row, int col) {
        BlockPiece piece = game.getPiece(slotIndex);
        PlacementResult result = game.tryPlace(slotIndex, row, col);
        playMoveSound(result, piece, row, col);
        effects.addAll(effectFactory.createEffects(result));
        return result;
    }

    // A placed piece makes the place sound, or the clear sound if it completed lines.
    // A rejected drop on the board makes the invalid sound. Letting go of the piece somewhere
    // else (for example back over the tray) just cancels the move quietly.
    private void playMoveSound(PlacementResult result, BlockPiece piece, int row, int col) {
        if (result.isPlaced()) {
            soundManager.play(result.getLinesCleared() > 0 ? SoundEffect.CLEAR : SoundEffect.PLACE);
        } else if (piece != null && touchesBoard(piece, row, col)) {
            soundManager.play(SoundEffect.INVALID);
        }
    }

    // True if at least one cell of the piece would be on the board at this position.
    private boolean touchesBoard(BlockPiece piece, int row, int col) {
        for (CellPosition offset : piece.getCells()) {
            if (game.getBoard().isInside(row + offset.getRow(), col + offset.getCol())) {
                return true;
            }
        }
        return false;
    }

    // The game can end at any moment (last move, time out), so every tick checks.
    // The end is dealt with once: the best score is saved and, for a game over or a failure,
    // the invalid sound is played. A win has no sound of its own.
    private void handleGameEnd() {
        if (!game.isFinished() || endHandled) {
            return;
        }
        endHandled = true;
        scoreManager.saveBestScoresIfChanged();

        if (game.getState() != GameState.WON) {
            soundManager.play(SoundEffect.INVALID);
        }
    }

    // Moves every animation forward and removes the finished ones.
    // They stand still while the game is paused, and keep playing after a game ends.
    private void updateEffects(long deltaNanos) {
        if (game.getState() == GameState.PAUSED) {
            return;
        }
        long deltaMillis = deltaNanos / 1000000L;
        for (VisualEffect effect : effects) {
            effect.update(deltaMillis);
        }
        effects.removeIf(VisualEffect::isFinished);
    }

    private boolean canPlay() {
        return game != null && game.getState() == GameState.PLAYING;
    }

    private void resetTimeAndInput() {
        lastTickNanos = System.nanoTime();
        endHandled = false;
        effects.clear();
        cancelDrag();
        selectedSlot = NO_SLOT;
        previewRow = START_PREVIEW_POSITION;
        previewCol = START_PREVIEW_POSITION;
        overlayRenderer.clearHover();
    }

    // Moves the preview back inside the board if the piece would stick out.
    private void keepPreviewInsideBoard(BlockPiece piece) {
        int maxRow = game.getBoard().getSize() - piece.getHeight();
        int maxCol = game.getBoard().getSize() - piece.getWidth();
        previewRow = Math.max(0, Math.min(previewRow, maxRow));
        previewCol = Math.max(0, Math.min(previewCol, maxCol));
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

        // The crown follows the score as soon as it passes the old best score.
        int bestToShow = Math.max(game.getBestScore(), game.getScore());
        hudRenderer.draw(g2, game.getScore(), bestToShow, getTimeText(), soundManager.isMuted());
        boardRenderer.drawBoard(g2, game.getBoard());
        boardRenderer.drawTray(g2, game, dragSlot);

        // Each effect draws itself; GamePanel does not care which kind it is (Drawable).
        for (Drawable effect : effects) {
            effect.draw(g2);
        }

        if (isDragging()) {
            drawDragPreview(g2);
        } else {
            drawKeyboardPreview(g2);
        }

        overlayRenderer.draw(g2, game);
    }

    // Draws the piece following the pointer and, on top of it, the green or red preview
    // of the board cells it would fill. It only asks canPlace() a question; it changes nothing.
    private void drawDragPreview(Graphics2D g2) {
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

    // The same green or red preview for the piece selected with the keyboard.
    private void drawKeyboardPreview(Graphics2D g2) {
        if (selectedSlot == NO_SLOT || !canPlay()) {
            return;
        }
        BlockPiece piece = game.getPiece(selectedSlot);
        if (piece == null) {
            return;
        }

        boolean valid = game.getBoard().canPlace(piece, previewRow, previewCol);
        boardRenderer.drawGhost(g2, game.getBoard(), piece, previewRow, previewCol, valid);
        boardRenderer.drawSlotHighlight(g2, selectedSlot);
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
        if (game.getMode() == GameMode.TIME_ATTACK) {
            return "TIME LEFT " + TimeFormat.formatRemaining(game.getRemainingMillis());
        }
        return "TIME " + TimeFormat.formatElapsed(game.getElapsedMillis());
    }
}
