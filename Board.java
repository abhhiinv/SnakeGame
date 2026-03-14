import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

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
    private final int ALL_DOTS    = 900;  // maximum number of snake segments
    private final int DOT_SIZE    = 20;   // size of each segment and apple in pixels
    private final int RANDOM_POSITION = 29; // used to randomly place apples in a 30x30 grid
    private final int BOARD_SIZE  = 600;  // width and height of the square board

    // Normal apple position (x, y) on the grid
    private int apple_x, apple_y;

    // Special apple related state
    private boolean specialAppleActive = false; // true when special apple is visible
    private int special_x, special_y;           // coordinates of special apple
    private int specialAppleTimer = 0;          // counts number of timer ticks since spawn
    private final int SPECIAL_APPLE_DURATION = 71; // default (unused now, kept for reference)
    private int specialAppleDuration;           // recalculated so special apple lasts ~10s
    private int normalApplesEaten = 0;          // how many normal apples eaten since last special
    private final int APPLES_FOR_SPECIAL = 5;   // spawn special apple after this many normal ones
    private final int SPECIAL_POINTS = 5;       // how many points special apple gives

    // Arrays to store the x and y coordinates of every snake segment
    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];

    // Direction flags that tell where the snake is currently moving
    private boolean leftDirection  = false;
    private boolean rightDirection = true;
    private boolean upDirection    = false;
    private boolean downDirection  = false;

    // True while the game is running; set to false on collision / game over
    private boolean inGame = true;

    // Current length of the snake, current score and timer that drives the game
    private int dots;
    private int score;
    private int timerDelay;
    private Timer timer;

    /**
     * Listener interface so the board can notify the main window when
     * the game ends (to show the "Play again" dialog / menu).
     */
    public interface GameOverListener {
        void onGameOver();
    }
    private GameOverListener gameOverListener;

    /**
     * Constructs the board.
     * @param timerDelay      speed of the game (smaller = faster).
     * @param gameOverListener callback used when the player loses.
     */
    Board(int timerDelay, GameOverListener gameOverListener) {
        this.timerDelay       = timerDelay;
        this.gameOverListener = gameOverListener;
        // Compute a duration so the special apple lasts ~10 seconds
        this.specialAppleDuration = (int)(10000.0 / timerDelay);

        // This panel listens for arrow keys
        addKeyListener(new TAdapter());
        setBackground(new Color(149, 209, 1));
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setFocusable(true);

        loadImages(); // load images from resources folder
        initGame();   // start initial snake position and timer
    }

    /**
     * Loads all image resources for the snake and apples.
     */
    public void loadImages() {
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/apple.png"));
        apple = i1.getImage();
        ImageIcon i2 = new ImageIcon(ClassLoader.getSystemResource("icons/dot.png"));
        dot = i2.getImage();
        ImageIcon i3 = new ImageIcon(ClassLoader.getSystemResource("icons/head.png"));
        head = i3.getImage();
    }

    /**
     * Resets and starts a new game.
     * - Sets starting snake length and position.
     * - Resets score and special-apple state.
     * - Starts the Swing timer (the game loop).
     */
    public void initGame() {
        dots  = 3;   // initial snake size
        score = 0;
        normalApplesEaten  = 0;
        specialAppleActive = false;
        specialAppleTimer  = 0;

        // Start by moving to the right
        leftDirection  = false;
        rightDirection = true;
        upDirection    = false;
        downDirection  = false;
        inGame = true;

        // Place the initial snake horizontally
        for (int i = 0; i < dots; i++) {
            y[i] = 100;
            x[i] = 100 - (i * DOT_SIZE);
        }

        locateApple(); // place first normal apple

        // Timer drives the game: every delay ms, actionPerformed() is called
        timer = new Timer(timerDelay, this);
        timer.start();
    }

    /**
     * Places the normal apple at a random coordinate on the grid.
     */
    public void locateApple() {
        int r = (int)(Math.random() * RANDOM_POSITION);
        apple_x = r * DOT_SIZE;
        r = (int)(Math.random() * RANDOM_POSITION);
        apple_y = r * DOT_SIZE;
    }

    /**
     * Places the special apple randomly on the board.
     * Ensures it does not overlap with the normal apple.
     */
    public void locateSpecialApple() {
        int r;
        do {
            r = (int)(Math.random() * RANDOM_POSITION);
            special_x = r * DOT_SIZE;
            r = (int)(Math.random() * RANDOM_POSITION);
            special_y = r * DOT_SIZE;
        } while (special_x == apple_x && special_y == apple_y); // avoid overlap
        specialAppleActive = true;
        specialAppleTimer  = 0; // reset timer when special apple appears
    }

    /**
     * Standard Swing painting hook.
     * Delegates the drawing work to the draw() method.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draws everything on the board:
     * - score text
     * - normal apple
     * - special apple (if active)
     * - snake body and head
     * - special apple timer bar
     * or the game over screen if the player has lost.
     */
    public void draw(Graphics g) {
        if (inGame) {
            // Draw current score in the top-left corner
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            g.drawString("Score: " + score, 20, 40);

            // Draw normal apple
            g.drawImage(apple, apple_x, apple_y, this);

            // Draw special apple when active
            if (specialAppleActive) {
                drawSpecialApple(g);
            }

            // Draw the snake; index 0 is the head, others are body segments
            for (int i = 0; i < dots; i++) {
                if (i == 0) {
                    g.drawImage(head, x[i], y[i], this);
                } else {
                    g.drawImage(dot, x[i], y[i], this);
                }
            }

            // Draw countdown bar only while special apple exists
            if (specialAppleActive) {
                drawTimerBar(g);
            }

            Toolkit.getDefaultToolkit().sync(); // smooth animation on some systems
        } else {
            gameOver(g);
        }
    }

    /**
     * Visually represents the special apple (blue circle here).
     */
    private void drawSpecialApple(Graphics g) {
        // Gold / blue circle so it stands out from normal apple
        g.setColor(new Color(3, 150, 255));
        g.fillOval(special_x, special_y, DOT_SIZE, DOT_SIZE);
        g.setColor(new Color(34, 97, 224));
        g.drawOval(special_x, special_y, DOT_SIZE, DOT_SIZE);
    }

    /**
     * Draws a bar at the bottom of the screen that shrinks as
     * the special apple is about to disappear.
     */
    private void drawTimerBar(Graphics g) {
        int barWidth = BOARD_SIZE - 40;
        int remaining = specialAppleDuration - specialAppleTimer;
        int filledWidth = (int)((double) remaining / specialAppleDuration * barWidth);

        // Background of the bar
        g.setColor(Color.DARK_GRAY);
        g.fillRect(20, BOARD_SIZE - 24, barWidth, 12);

        // Foreground showing remaining time
        g.setColor(new Color(3, 150, 255));
        g.fillRect(20, BOARD_SIZE - 24, filledWidth, 12);
    }

    /**
     * Draws the game over overlay text and background.
     */
    public void gameOver(Graphics g) {
        // Dim background with a transparent black rectangle
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);

        String msg = "Game Over!  Score: " + score;
        Font font = new Font("SansSerif", Font.BOLD, 28);
        FontMetrics fm = getFontMetrics(font);

        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString(msg, (BOARD_SIZE - fm.stringWidth(msg)) / 2, BOARD_SIZE / 2 - 40);

        // The "Play again" choice is handled by the main window using a dialog.
    }

    /**
     * Checks if the head of the snake touches either a normal
     * or special apple and updates length, score and state.
     */
    public void checkApple() {
        // Normal apple eaten
        if (x[0] == apple_x && y[0] == apple_y) {
            dots++;            // snake grows
            score++;           // +1 point
            normalApplesEaten++;
            locateApple();     // place another normal apple

            // Spawn special apple after every APPLES_FOR_SPECIAL normal apples
            if (normalApplesEaten % APPLES_FOR_SPECIAL == 0) {
                locateSpecialApple();
            }
        }

        // Special apple eaten
        if (specialAppleActive && x[0] == special_x && y[0] == special_y) {
            dots++;                     // grow for special apple too
            score += SPECIAL_POINTS;    // bonus points
            specialAppleActive = false; // remove special apple from board
        }
    }

    /**
     * Detects collisions with the snake itself or with the walls.
     * If a collision happens, ends the game and informs the listener.
     */
    public void checkCollision() {
        // Check if head (index 0) runs into its own body (index >= 4)
        for (int i = dots; i > 0; i--) {
            if ((i > 4) && (x[0] == x[i]) && (y[0] == y[i])) {
                inGame = false;
            }
        }
        // Check boundaries of the board
        if (y[0] >= BOARD_SIZE) inGame = false;
        if (x[0] >= BOARD_SIZE) inGame = false;
        if (y[0] < 0)           inGame = false;
        if (x[0] < 0)           inGame = false;

        // If we lost, stop timer and notify the main window
        if (!inGame) {
            timer.stop();
            if (gameOverListener != null) {
                gameOverListener.onGameOver();
            }
        }
    }

    /**
     * Moves the snake forward one "step":
     * - each segment takes the position of the segment in front of it
     * - the head moves one DOT_SIZE in the current direction.
     */
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

    /**
     * Called automatically by the Swing Timer every timerDelay milliseconds.
     * This method represents one frame of the game loop:
     * - check apple collisions
     * - check wall / self collisions
     * - move the snake
     * - update special apple timer
     * - repaint the board.
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();

            // Increase special apple lifetime counter if active
            if (specialAppleActive) {
                specialAppleTimer++;
                if (specialAppleTimer >= specialAppleDuration) {
                    specialAppleActive = false; // special apple disappeared
                }
            }
        }
        repaint();
    }

    /**
     * Inner class that handles keyboard events.
     * It ensures you cannot instantly reverse direction (no 180° turns).
     */
    public class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            // Move left, unless we are currently moving right
            if (key == KeyEvent.VK_LEFT && !rightDirection) {
                leftDirection  = true;
                upDirection    = false;
                downDirection  = false;
            }
            // Move right, unless we are currently moving left
            if (key == KeyEvent.VK_RIGHT && !leftDirection) {
                rightDirection = true;
                upDirection    = false;
                downDirection  = false;
            }
            // Move up, unless we are currently moving down
            if (key == KeyEvent.VK_UP && !downDirection) {
                upDirection    = true;
                leftDirection  = false;
                rightDirection = false;
            }
            // Move down, unless we are currently moving up
            if (key == KeyEvent.VK_DOWN && !upDirection) {
                downDirection  = true;
                leftDirection  = false;
                rightDirection = false;
            }
        }
    }
}