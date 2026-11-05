package view;

import model.GameMode;
import util.Constants;
import util.ResourceLoader;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// The window. It holds all screens on top of each other (CardLayout) and shows one at a time.
// Screens ask the frame to switch, so they never need to know about each other.
public class GameFrame extends JFrame {

    public static final String SCREEN_MENU = "menu";
    public static final String SCREEN_MODE = "mode";
    public static final String SCREEN_INSTRUCTIONS = "instructions";
    public static final String SCREEN_GAME = "game";

    // The custom font is loaded once here. If it is missing, a default font is used.
    private final Font uiBaseFont = ResourceLoader.loadFirstFont(Constants.FONT_PATHS, 12f);
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screens = new JPanel(cardLayout);
    private final GamePanel gamePanel;

    public GameFrame() {
        super("BLOCKED.exe");
        gamePanel = new GamePanel(this);

        screens.add(new MenuPanel(this), SCREEN_MENU);
        screens.add(new ModePanel(this), SCREEN_MODE);
        screens.add(new InstructionsPanel(this), SCREEN_INSTRUCTIONS);
        screens.add(gamePanel, SCREEN_GAME);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Save the best score when the player closes the window with the X button.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gamePanel.saveBestScores();
            }
        });
        setResizable(false);
        add(screens);
        pack();
        setLocationRelativeTo(null);

        showScreen(SCREEN_MENU);
    }

    public void showScreen(String screenName) {
        if (!screenName.equals(SCREEN_GAME)) {
            gamePanel.stopGame();
        }
        cardLayout.show(screens, screenName);
        if (screenName.equals(SCREEN_GAME)) {
            gamePanel.requestFocusInWindow();
        }
    }

    public void startGame(GameMode mode) {
        gamePanel.startGame(mode);
        showScreen(SCREEN_GAME);
    }

    public void exitGame() {
        gamePanel.saveBestScores();
        System.exit(0);
    }

    // Returns the custom font in the given size.
    public Font createUiFont(float size) {
        return uiBaseFont.deriveFont(size);
    }
}
