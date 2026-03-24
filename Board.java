import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Board panel where the whole Snake game is drawn and updated.
 * - Stores snake body, apples, score and timer.
 * - Contains the main game loop (via Swing Timer and ActionListener).
 */
public class Board extends JPanel implements ActionListener {

    // Images for normal apple, snake body segment and snake head
    private Image apple;
    private Image dot;
    private Image head;

    // Basic board configuration
    private final int ALL_DOTS       = 900;  // maximum number of snake segments
    private final int DOT_SIZE       = 20;   // size of each segment and apple in pixels
    private final int RANDOM_POSITION = 29;  // used to randomly place apples in a 30x30 grid
    private final int BOARD_SIZE     = 600;  // width and height of the square board

    // Normal apple position (x, y) on the grid
    private int apple_x, apple_y;

    // Special apple related state
    private boolean specialAppleActive = false;
    private int special_x, special_y;
    private int specialAppleTimer = 0;
    private final int SPECIAL_APPLE_DURATION = 71; // kept for reference
    private int specialAppleDuration;
    private int normalApplesEaten = 0;
    private final int APPLES_FOR_SPECIAL = 5;
    private final int SPECIAL_POINTS     = 5;

    // Arrays to store the x and y coordinates of every snake segment
    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];

    // Direction flags
    private boolean leftDirection  = false;
    private boolean rightDirection = true;
    private boolean upDirection    = false;
    private boolean downDirection  = false;

    // True while the game is running
    private boolean inGame = true;

    // Game state
    private int dots;
    private int score;
    private int timerDelay;
    private Timer timer;

    // --- ADDED: difficulty label and whether this run made the leaderboard ---
    private String difficulty;
    private boolean madeHighScore = false;

    // -------------------------------------------------------------------------
    // Listener interface for game-over callback
    // -------------------------------------------------------------------------
    public interface GameOverListener {
        void onGameOver();
    }
    private GameOverListener gameOverListener;

    // =========================================================================
    // HighScoreManager — reads and writes highscores.txt
    // File format (one entry per line):  score,difficulty
    // Example:  42,Hard
    // =========================================================================
    public static class HighScoreManager {

        private static final String FILE_NAME = "highscores.txt";
        private static final int    MAX_SCORES = 5;

        /** One leaderboard entry. */
        public static class Entry implements Comparable<Entry> {
            public final int    score;
            public final String difficulty;

            Entry(int score, String difficulty) {
                this.score      = score;
                this.difficulty = difficulty;
            }

            /** Descending order by score. */
            @Override
            public int compareTo(Entry other) {
                return Integer.compare(other.score, this.score);
            }
        }

        /**
         * Reads the top-5 entries from highscores.txt.
         * Creates the file if it does not exist.
         * Returns an empty list on any read error.
         */
        public static List<Entry> load() {
            List<Entry> entries = new ArrayList<>();
            File file = new File(FILE_NAME);

            // Create an empty file if it doesn't exist yet
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return entries; // nothing to read from a brand-new file
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split(",", 2);
                    if (parts.length != 2) continue;
                    try {
                        int    s    = Integer.parseInt(parts[0].trim());
                        String diff = parts[1].trim();
                        entries.add(new Entry(s, diff));
                    } catch (NumberFormatException ignored) {
                        // skip malformed lines
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            Collections.sort(entries);          // highest first
            return entries.subList(0, Math.min(entries.size(), MAX_SCORES));
        }

        /**
         * Adds a new entry, keeps only top-5, and saves back to disk.
         * Returns true if the new score is among the saved entries.
         */
        public static boolean save(int newScore, String difficulty) {
            // Load current scores (uncapped so we can decide position)
            List<HighScoreManager.Entry> all = loadAll();
            all.add(new Entry(newScore, difficulty));
            Collections.sort(all);

            // Keep only top 5
            List<Entry> top = all.subList(0, Math.min(all.size(), MAX_SCORES));

            // Write back
            File file = new File(FILE_NAME);
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
                for (Entry e : top) {
                    pw.println(e.score + "," + e.difficulty);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Did the new score make the list?
            for (Entry e : top) {
                if (e.score == newScore && e.difficulty.equals(difficulty)) {
                    return true;
                }
            }
            return false;
        }

        /** Like load() but without the 5-entry cap, used internally for saving. */
        private static List<Entry> loadAll() {
            List<Entry> entries = new ArrayList<>();
            File file = new File(FILE_NAME);
            if (!file.exists()) return entries;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split(",", 2);
                    if (parts.length != 2) continue;
                    try {
                        int    s    = Integer.parseInt(parts[0].trim());
                        String diff = parts[1].trim();
                        entries.add(new Entry(s, diff));
                    } catch (NumberFormatException ignored) {}
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return entries;
        }
    }

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Constructs the board.
     * @param timerDelay       speed of the game (smaller = faster).
     * @param difficulty       human-readable label: "Easy", "Medium" or "Hard".
     * @param gameOverListener callback used when the player loses.
     */
    Board(int timerDelay, String difficulty, GameOverListener gameOverListener) {
        this.timerDelay        = timerDelay;
        this.difficulty        = difficulty;        // ADDED
        this.gameOverListener  = gameOverListener;
        this.specialAppleDuration = (int)(10000.0 / timerDelay);

        addKeyListener(new TAdapter());
        setBackground(new Color(149, 209, 1));
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setFocusable(true);

        loadImages();
        initGame();
    }

    // =========================================================================
    // Image loading
    // =========================================================================
    public void loadImages() {
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/apple.png"));
        apple = i1.getImage();
        ImageIcon i2 = new ImageIcon(ClassLoader.getSystemResource("icons/dot.png"));
        dot = i2.getImage();
        ImageIcon i3 = new ImageIcon(ClassLoader.getSystemResource("icons/head.png"));
        head = i3.getImage();
    }

    // =========================================================================
    // Game initialisation
    // =========================================================================
    public void initGame() {
        dots  = 3;
        score = 0;
        normalApplesEaten  = 0;
        specialAppleActive = false;
        specialAppleTimer  = 0;
        madeHighScore      = false;   // ADDED: reset flag for new game

        leftDirection  = false;
        rightDirection = true;
        upDirection    = false;
        downDirection  = false;
        inGame = true;

        for (int i = 0; i < dots; i++) {
            y[i] = 100;
            x[i] = 100 - (i * DOT_SIZE);
        }

        locateApple();

        timer = new Timer(timerDelay, this);
        timer.start();
    }

    // =========================================================================
    // Apple placement
    // =========================================================================
    public void locateApple() {
        int r = (int)(Math.random() * RANDOM_POSITION);
        apple_x = r * DOT_SIZE;
        r = (int)(Math.random() * RANDOM_POSITION);
        apple_y = r * DOT_SIZE;
    }

    public void locateSpecialApple() {
        int r;
        do {
            r = (int)(Math.random() * RANDOM_POSITION);
            special_x = r * DOT_SIZE;
            r = (int)(Math.random() * RANDOM_POSITION);
            special_y = r * DOT_SIZE;
        } while (special_x == apple_x && special_y == apple_y);
        specialAppleActive = true;
        specialAppleTimer  = 0;
    }

    // =========================================================================
    // Painting
    // =========================================================================
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (inGame) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            g.drawString("Score: " + score, 20, 40);

            g.drawImage(apple, apple_x, apple_y, this);

            if (specialAppleActive) {
                drawSpecialApple(g);
            }

            for (int i = 0; i < dots; i++) {
                if (i == 0) {
                    g.drawImage(head, x[i], y[i], this);
                } else {
                    g.drawImage(dot, x[i], y[i], this);
                }
            }

            if (specialAppleActive) {
                drawTimerBar(g);
            }

            Toolkit.getDefaultToolkit().sync();
        } else {
            gameOver(g);
        }
    }

    private void drawSpecialApple(Graphics g) {
        g.setColor(new Color(3, 150, 255));
        g.fillOval(special_x, special_y, DOT_SIZE, DOT_SIZE);
        g.setColor(new Color(34, 97, 224));
        g.drawOval(special_x, special_y, DOT_SIZE, DOT_SIZE);
    }

    private void drawTimerBar(Graphics g) {
        int barWidth   = BOARD_SIZE - 40;
        int remaining  = specialAppleDuration - specialAppleTimer;
        int filledWidth = (int)((double) remaining / specialAppleDuration * barWidth);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(20, BOARD_SIZE - 24, barWidth, 12);
        g.setColor(new Color(3, 150, 255));
        g.fillRect(20, BOARD_SIZE - 24, filledWidth, 12);
    }

    // =========================================================================
    // Game-over screen — MODIFIED to show leaderboard
    // =========================================================================
    public void gameOver(Graphics g) {
        // Dim the board
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);

        int centerX = BOARD_SIZE / 2;
        int y = 110; // vertical cursor

        // --- "Game Over" heading ---
        Font bigFont = new Font("SansSerif", Font.BOLD, 32);
        FontMetrics bigFm = getFontMetrics(bigFont);
        g.setFont(bigFont);
        g.setColor(Color.WHITE);
        String title = "Game Over!";
        g.drawString(title, centerX - bigFm.stringWidth(title) / 2, y);
        y += 36;

        // --- Current score + difficulty ---
        Font midFont = new Font("SansSerif", Font.PLAIN, 22);
        FontMetrics midFm = getFontMetrics(midFont);
        g.setFont(midFont);
        String scoreLine = "Score: " + score + "  [" + difficulty + "]";
        g.drawString(scoreLine, centerX - midFm.stringWidth(scoreLine) / 2, y);
        y += 28;

        // --- "New High Score!" banner (shown only when deserved) ---
        if (madeHighScore) {
            Font starFont = new Font("SansSerif", Font.BOLD, 20);
            FontMetrics starFm = getFontMetrics(starFont);
            g.setFont(starFont);
            g.setColor(new Color(255, 215, 0)); // gold
            String banner = "\u2605  New High Score!  \u2605";
            g.drawString(banner, centerX - starFm.stringWidth(banner) / 2, y);
            g.setColor(Color.WHITE);
        }
        y += 36;

        // --- Leaderboard heading ---
        Font hdrFont = new Font("SansSerif", Font.BOLD, 20);
        FontMetrics hdrFm = getFontMetrics(hdrFont);
        g.setFont(hdrFont);
        g.setColor(new Color(149, 209, 1)); // game's green accent
        String lbTitle = "— Top 5 High Scores —";
        g.drawString(lbTitle, centerX - hdrFm.stringWidth(lbTitle) / 2, y);
        y += 30;

        // --- Leaderboard rows ---
        List<HighScoreManager.Entry> entries = HighScoreManager.load();
        Font rowFont = new Font("SansSerif", Font.PLAIN, 19);
        FontMetrics rowFm = getFontMetrics(rowFont);
        g.setFont(rowFont);

        if (entries.isEmpty()) {
            g.setColor(Color.LIGHT_GRAY);
            String none = "No scores yet.";
            g.drawString(none, centerX - rowFm.stringWidth(none) / 2, y);
        } else {
            //String[] medals = {"\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49", "4.", "5."};
            // Note: medal emoji may not render on all JVM fonts; plain numbers are the fallback
            for (int i = 0; i < entries.size(); i++) {
                HighScoreManager.Entry e = entries.get(i);
                // Highlight this run's entry if it's on the board
                boolean isThisRun = madeHighScore && e.score == score
                        && e.difficulty.equals(difficulty);
                g.setColor(isThisRun ? new Color(255, 215, 0) : Color.WHITE);
                String rank  = (i + 1) + ".";
                String entry = rank + "  " + e.score + "  [" + e.difficulty + "]";
                g.drawString(entry, centerX - rowFm.stringWidth(entry) / 2, y);
                y += 28;
            }
        }
    }

    // =========================================================================
    // Game logic
    // =========================================================================
    public void checkApple() {
        if (x[0] == apple_x && y[0] == apple_y) {
            dots++;
            score++;
            normalApplesEaten++;
            locateApple();
            if (normalApplesEaten % APPLES_FOR_SPECIAL == 0) {
                locateSpecialApple();
            }
        }
        if (specialAppleActive && x[0] == special_x && y[0] == special_y) {
            dots++;
            score += SPECIAL_POINTS;
            specialAppleActive = false;
        }
    }

    public void checkCollision() {
        for (int i = dots - 1; i > 3; i--) {  // corrected bounds
            if (x[0] == x[i] && y[0] == y[i]) {
                inGame = false;
            }
        }
        if (y[0] >= BOARD_SIZE) inGame = false;
        if (x[0] >= BOARD_SIZE) inGame = false;
        if (y[0] < 0)           inGame = false;
        if (x[0] < 0)           inGame = false;

        if (!inGame) {
            timer.stop();
            // ADDED: save score and record whether it made the top 5
            madeHighScore = HighScoreManager.save(score, difficulty);
            if (gameOverListener != null) {
                gameOverListener.onGameOver();
            }
        }
    }

    public void move() {
        for (int i = dots; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        if (leftDirection)  x[0] -= DOT_SIZE;
        if (rightDirection) x[0] += DOT_SIZE;
        if (upDirection)    y[0] -= DOT_SIZE;
        if (downDirection)  y[0] += DOT_SIZE;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();

            if (specialAppleActive) {
                specialAppleTimer++;
                if (specialAppleTimer >= specialAppleDuration) {
                    specialAppleActive = false;
                }
            }
        }
        repaint();
    }

    // =========================================================================
    // Keyboard input
    // =========================================================================
    public class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_LEFT && !rightDirection) {
                leftDirection  = true;
                upDirection    = false;
                downDirection  = false;
            }
            if (key == KeyEvent.VK_RIGHT && !leftDirection) {
                rightDirection = true;
                upDirection    = false;
                downDirection  = false;
            }
            if (key == KeyEvent.VK_UP && !downDirection) {
                upDirection    = true;
                leftDirection  = false;
                rightDirection = false;
            }
            if (key == KeyEvent.VK_DOWN && !upDirection) {
                downDirection  = true;
                leftDirection  = false;
                rightDirection = false;
            }
        }
    }
}