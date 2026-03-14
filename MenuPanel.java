import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Menu panel shown at the beginning of the game.
 * Lets the player choose the difficulty (which controls the snake speed).
 */
public class MenuPanel extends JPanel {

    /**
     * Listener so this panel can report which difficulty
     * (timer delay) the player picked back to the main window.
     */
    public interface DifficultyListener {
        void onDifficultySelected(int timerDelay);
    }

    private DifficultyListener listener;

    /**
     * Constructs the menu panel and sets a dark background + layout.
     */
    MenuPanel(DifficultyListener listener) {
        this.listener = listener;
        setBackground(new Color(30, 30, 30));
        setPreferredSize(new Dimension(600, 600));
        setLayout(new GridBagLayout()); // center everything nicely
        buildUI();
    }

    /**
     * Builds the visible user interface:
     * - a big title
     * - a subtitle
     * - three buttons for Easy / Medium / Hard.
     */
    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Title label at the top
        JLabel title = new JLabel("SNAKE XENIA", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 44));
        title.setForeground(new Color(149, 209, 1));
        gbc.gridy = 0;
        gbc.insets = new Insets(60, 40, 8, 40);
        add(title, gbc);

        // Subtitle below the title
        JLabel sub = new JLabel("Select Difficulty", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 26));
        sub.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 40, 40);
        add(sub, gbc);

        // Area where the difficulty buttons live
        gbc.insets = new Insets(12, 80, 12, 80);

        // Easy button (slow speed)
        JButton easy = makeButton("Easy", new Color(80, 180, 80));
        gbc.gridy = 2;
        add(easy, gbc);

        // Medium button (normal speed)
        JButton medium = makeButton("Medium", new Color(220, 160, 0));
        gbc.gridy = 3;
        add(medium, gbc);

        // Hard button (fast snake)
        JButton hard = makeButton("Hard", new Color(200, 60, 60));
        gbc.gridy = 4;
        add(hard, gbc);

        // Timer delays in milliseconds: bigger delay = slower game
        easy.addActionListener(e -> listener.onDifficultySelected(200));
        medium.addActionListener(e -> listener.onDifficultySelected(140));
        hard.addActionListener(e -> listener.onDifficultySelected(80));
    }

    /**
     * Creates a nicely styled button with a hover effect.
     */
    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 28));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        // Simple hover effect that darkens the button color on mouse over
        Color hoverColor = color.darker();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(color); }
        });

        return btn;
    }
}