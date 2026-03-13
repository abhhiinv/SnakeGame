import java.awt.*;
import javax.swing.*;

public class SnakeGame extends JFrame {

    private CardLayout cardLayout;
    private JPanel container;

    private MenuPanel menuPanel;
    private Board boardPanel;

    private final String MENU  = "MENU";
    private final String GAME  = "GAME";

    SnakeGame() {
        super("Snake Xenia");

        cardLayout = new CardLayout();
        container  = new JPanel(cardLayout);

        // Build menu — on difficulty selected, start game
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

    private void startGame(int timerDelay) {
        // Remove old board if exists
        if (boardPanel != null) {
            container.remove(boardPanel);
        }

        // Create board; on game over show "Play Again" button overlay
        boardPanel = new Board(timerDelay, () -> showPlayAgainOverlay(timerDelay));
        container.add(boardPanel, GAME);

        cardLayout.show(container, GAME);
        boardPanel.requestFocusInWindow();
    }

    private void showPlayAgainOverlay(int lastDelay) {
        // After a short delay so "Game Over" text is visible, show dialog
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