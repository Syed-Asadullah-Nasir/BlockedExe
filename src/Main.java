import view.GameFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Swing windows must be created on the Swing event thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GameFrame().setVisible(true);
            }
        });
    }
}
