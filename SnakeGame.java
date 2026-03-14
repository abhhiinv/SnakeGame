import java.awt.*;
import javax.swing.*;

/**
 * Main application window for the Snake game.
 * Uses a CardLayout to switch between the menu screen and the game board.
 */
public class SnakeGame extends JFrame {

    // CardLayout allows swapping between "cards" (MENU panel and GAME panel)
    private CardLayout cardLayout;
    private JPanel container;

    // Panels for the two different screens
    private MenuPanel menuPanel;
    private Board boardPanel;

    // String keys used by CardLayout to identify each screen
    private final String MENU  = "MENU";
    private final String GAME  = "GAME";

    /**
     * Sets up the main window, layout and initial menu screen.
     */
    SnakeGame() {
        super("Snake Xenia");

        cardLayout = new CardLayout();
        container  = new JPanel(cardLayout);

        // Build the menu panel.
        // When a difficulty is chosen, startGame(...) is called with the selected timer delay.
        menuPanel = new MenuPanel(this::startGame);

        // Add the menu as the first screen
        container.add(menuPanel, MENU);

        // Add container to the frame and configure basic window properties
        add(container);
        pack();
        setLocationRelativeTo(null);                 // center on screen
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        showMenu(); // Display the menu card
    }

    /**
     * Shows the menu screen using the CardLayout.
     */
    private void showMenu() {
        cardLayout.show(container, MENU);
    }

    /**
     * Starts a new game with the specific timer delay chosen in the menu.
     * If a game is already running, its board is removed and replaced.
     */
    private void startGame(int timerDelay) {
        // Remove old board if exists so we can create a fresh one
        if (boardPanel != null) {
            container.remove(boardPanel);
        }

        // Create the game board.
        // On game over, we show a "Play Again / Quit" dialog.
        boardPanel = new Board(timerDelay, () -> showPlayAgainOverlay(timerDelay));
        container.add(boardPanel, GAME);

        // Switch to the game screen and give keyboard focus to the board
        cardLayout.show(container, GAME);
        boardPanel.requestFocusInWindow();
    }

    /**
     * Shows a dialog asking the player if they want to play again.
     * It appears shortly after the game over text so the player can see it.
     */
    private void showPlayAgainOverlay(int lastDelay) {
        // Delay dialog slightly so "Game Over" text is visible first
        Timer t = new Timer(600, e -> {
            int choice = JOptionPane.showOptionDialog(
                this,
                "Would you like to play again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Play Again", "Quit"},
                "Play Again"
            );
            if (choice == JOptionPane.YES_OPTION) {
                showMenu(); // Back to difficulty menu
            } else {
                System.exit(0); // Exit the application completely
            }
        });
        t.setRepeats(false); // run only once
        t.start();
    }

    /**
     * Entry point: creates the main window on the Swing event dispatch thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame().setVisible(true));
    }
}