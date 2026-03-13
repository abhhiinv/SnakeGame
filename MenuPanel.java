import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MenuPanel extends JPanel {

    public interface DifficultyListener {
        void onDifficultySelected(int timerDelay);
    }

    private DifficultyListener listener;

    MenuPanel(DifficultyListener listener) {
        this.listener = listener;
        setBackground(new Color(30, 30, 30));
        setPreferredSize(new Dimension(600, 600));
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Title
        JLabel title = new JLabel("SNAKE XENIA", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 44));
        title.setForeground(new Color(149, 209, 1));
        gbc.gridy = 0;
        gbc.insets = new Insets(60, 40, 8, 40);
        add(title, gbc);

        // Subtitle
        JLabel sub = new JLabel("Select Difficulty", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 26));
        sub.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 40, 40);
        add(sub, gbc);

        // Buttons
        gbc.insets = new Insets(12, 80, 12, 80);

        JButton easy = makeButton("Easy", new Color(80, 180, 80));
        gbc.gridy = 2;
        add(easy, gbc);

        JButton medium = makeButton("Medium", new Color(220, 160, 0));
        gbc.gridy = 3;
        add(medium, gbc);

        JButton hard = makeButton("Hard", new Color(200, 60, 60));
        gbc.gridy = 4;
        add(hard, gbc);

        // Timer delays: higher = slower
        easy.addActionListener(e -> listener.onDifficultySelected(200));
        medium.addActionListener(e -> listener.onDifficultySelected(140));
        hard.addActionListener(e -> listener.onDifficultySelected(80));
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 28));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        // Hover effect
        Color hoverColor = color.darker();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(color); }
        });

        return btn;
    }
}