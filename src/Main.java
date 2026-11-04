import util.UncaughtErrorLogger;
import view.GameFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Any error nobody catches is written to logs/game.log instead of being lost.
        UncaughtErrorLogger.install();

        // Swing windows must be created on the Swing event thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GameFrame().setVisible(true);
            }
        });
    }
}
