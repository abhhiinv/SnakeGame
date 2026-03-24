import java.awt.*;
import javax.swing.*;

/**
 * Main application window for the Snake game.
 * Uses a CardLayout to switch between the menu screen and the game board.
 */
public class SnakeGame extends JFrame {

    private CardLayout cardLayout;
    private JPanel container;

    private MenuPanel menuPanel;
    private Board boardPanel;

    private final String MENU = "MENU";
    private final String GAME = "GAME";

    SnakeGame() {
        super("Snake Xenia");

        cardLayout = new CardLayout();
        container  = new JPanel(cardLayout);

        menuPanel = new MenuPanel(this::startGame);
        container.add(menuPanel, MENU);

        add(container);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        showMenu();
    }

    private void showMenu() {
        cardLayout.show(container, MENU);
    }

    /**
     * Converts a timer delay back to a human-readable difficulty label.
     * Must match the values set in MenuPanel.
     */
    private String difficultyLabel(int timerDelay) {
        if (timerDelay >= 200) return "Easy";
        if (timerDelay >= 140) return "Medium";
        return "Hard";
    }

    /**
     * Starts a new game.
     * MODIFIED: passes difficulty label to Board so it can save it with the score.
     */
    private void startGame(int timerDelay) {
        if (boardPanel != null) {
            container.remove(boardPanel);
        }

        String label = difficultyLabel(timerDelay); // ADDED

        boardPanel = new Board(timerDelay, label, () -> showPlayAgainOverlay(timerDelay)); // MODIFIED
        container.add(boardPanel, GAME);

        cardLayout.show(container, GAME);
        boardPanel.requestFocusInWindow();
    }

    private void showPlayAgainOverlay(int lastDelay) {
        Timer t = new Timer(600, e -> {
            // Build dialog manually so we can position it at the top of the window
            JOptionPane pane = new JOptionPane(
                "Would you like to play again?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                new String[]{"Play Again", "Quit"},
                "Play Again"
            );
            JDialog dialog = pane.createDialog(this, "Game Over");

            // Horizontally center over the frame, 10px from the top
            java.awt.Point frameLoc = this.getLocationOnScreen();
            int dialogX = frameLoc.x + (this.getWidth() - dialog.getWidth()) / 2;
            int dialogY = frameLoc.y + 450;
            dialog.setLocation(dialogX, dialogY);
            dialog.setVisible(true);

            // Read result — null means the dialog was closed without choosing
            Object value = pane.getValue();
            int choice = (value == null || value.equals("Quit"))
                    ? JOptionPane.NO_OPTION : JOptionPane.YES_OPTION;

            if (choice == JOptionPane.YES_OPTION) {
                showMenu();
            } else {
                System.exit(0);
            }
        });
        t.setRepeats(false);
        t.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame().setVisible(true));
    }
}