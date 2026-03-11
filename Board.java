
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Board.java — The main game panel for the Snake Game.
 *
 * This class extends JPanel to serve as the drawing surface and
 * implements ActionListener (for the game timer tick) and
 * MouseListener (for detecting "Play Again" button clicks).
 */
public class Board extends JPanel implements ActionListener, MouseListener {

    private Image apple;
    private Image dot;
    private Image head;

    // ---------------------------------------------------------------
    // GAME CONSTANTS — fixed values that define board dimensions
    // ---------------------------------------------------------------
    private final int ALL_DOTS = 900;          // Maximum possible number of snake segments (300x300 board / 10px each = 900 slots)
    private final int DOT_SIZE = 10;           // Size (in pixels) of each snake segment and the apple
    private final int RANDOM_POSITION = 29;    // Upper bound for random apple placement (0 to 28 → 0 to 280px in steps of 10)

    private int apple_x;  // X-coordinate of the apple
    private int apple_y;  // Y-coordinate of the apple

    // ---------------------------------------------------------------
    // SNAKE POSITION ARRAYS — parallel arrays storing each segment's coordinates
    // Index 0 is always the head; higher indices are body segments
    // ---------------------------------------------------------------
    private final int x[] = new int[ALL_DOTS];  // X-coordinates of all snake segments
    private final int y[] = new int[ALL_DOTS];  // Y-coordinates of all snake segments

    // ---------------------------------------------------------------
    // MOVEMENT FLAGS — only one can be true at a time (current direction)
    // Prevents the snake from instantly reversing into itself
    // ---------------------------------------------------------------
    private boolean leftDirection  = false;  // Snake moving left
    private boolean rightDirection = true;   // Snake starts moving right by default
    private boolean upDirection    = false;  // Snake moving up
    private boolean downDirection  = false;  // Snake moving down

    // ---------------------------------------------------------------
    // GAME STATE
    // ---------------------------------------------------------------
    private boolean inGame = true;  // true → game is running; false → game over

    // ---------------------------------------------------------------
    // GAME VARIABLES
    // ---------------------------------------------------------------
    private int dots;                    // Current length (number of segments) of the snake
    private int score;                   // Player's current score (increments on each apple eaten)
    private Timer timer;                 // Swing Timer that drives the game loop (fires actionPerformed repeatedly)
    private Rectangle playAgainButton;   // Bounding box of the "Play Again" button (used for click detection on game over)

    // ---------------------------------------------------------------
    // CONSTRUCTOR — sets up the game panel
    // ---------------------------------------------------------------
    Board() {
        addKeyListener(new TAdapter());              // Register keyboard listener for arrow key input
        addMouseListener(this);                     // Register mouse listener for "Play Again" button clicks
        setBackground(new Color(168, 230, 14));     // Set the board background to a bright green colour
        setPreferredSize(new Dimension(300, 300));  // Fix the panel size to 300×300 pixels
        setFocusable(true);                         // Allow this panel to receive keyboard focus

        loadImages();  // Load all sprite images from resources
        initGame();    // Set up the initial game state and start the timer
    }

    // ---------------------------------------------------------------
    // loadImages() — loads the sprite images from the icons/ folder
    // Called once during construction
    // ---------------------------------------------------------------
    public void loadImages() {
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/apple.png"));
        apple = i1.getImage();  // Store the apple sprite

        ImageIcon i2 = new ImageIcon(ClassLoader.getSystemResource("icons/dot.png"));
        dot = i2.getImage();    // Store the body-segment sprite

        ImageIcon i3 = new ImageIcon(ClassLoader.getSystemResource("icons/head.png"));
        head = i3.getImage();   // Store the snake-head sprite
    }

    // ---------------------------------------------------------------
    // initGame() — resets all game variables and starts/restarts the timer
    // Called at construction and when the player clicks "Play Again"
    // ---------------------------------------------------------------
    public void initGame() {
        dots  = 3;   // Snake starts with 3 segments
        score = 0;   // Reset score to zero

        // Place the initial snake segments horizontally at y=50,
        // starting at x=50 for the head and stepping back by DOT_SIZE for each body segment
        for (int i = 0; i < dots; i++) {
            y[i] = 50;
            x[i] = 50 - (i * DOT_SIZE);
        }

        locateApple();  // Randomly place the first apple on the board

        // Create (or restart) the Swing Timer with a 140ms delay between ticks
        // Each tick triggers actionPerformed(), which advances the game by one step
        timer = new Timer(140, this);
        timer.start();

        // Reset direction flags so the snake starts moving right
        inGame         = true;
        leftDirection  = false;
        rightDirection = true;
        upDirection    = false;
        downDirection  = false;
    }

    // ---------------------------------------------------------------
    // locateApple() — places the apple at a new random position on the grid
    // Both x and y are snapped to the DOT_SIZE grid (multiples of 10)
    // ---------------------------------------------------------------
    public void locateApple() {
        int r  = (int) (Math.random() * RANDOM_POSITION);  // Random column index (0–28)
        apple_x = r * DOT_SIZE;                            // Convert to pixel x-coordinate

        r = (int) (Math.random() * RANDOM_POSITION);       // Random row index (0–28)
        apple_y = r * DOT_SIZE;                            // Convert to pixel y-coordinate
    }

    // ---------------------------------------------------------------
    // paintComponent() — called automatically by Swing whenever the panel needs repainting
    // Delegates all actual drawing to draw()
    // ---------------------------------------------------------------
    public void paintComponent(Graphics g) {
        super.paintComponent(g);  // Paint the background (calls JPanel's default paint)
        draw(g);                  // Draw game elements on top
    }

    // ---------------------------------------------------------------
    // draw() — renders the game when running, or triggers the game-over screen
    // ---------------------------------------------------------------
    public void draw(Graphics g) {
        if (inGame) {
            // Draw the current score in the top-left corner
            g.setColor(Color.BLACK);
            g.setFont(new Font("SAN_SERIF", Font.BOLD, 12));
            g.drawString("Score: " + score, 10, 20);

            // Draw the apple at its current position
            g.drawImage(apple, apple_x, apple_y, this);

            // Draw each snake segment; index 0 uses the head image, all others use the dot (body) image
            for (int i = 0; i < dots; i++) {
                if (i == 0) {
                    g.drawImage(head, x[i], y[i], this);  // Head
                } else {
                    g.drawImage(dot, x[i], y[i], this);   // Body segment
                }
            }

            // Sync the display for smoother rendering on some platforms (Linux fix)
            Toolkit.getDefaultToolkit().sync();
        } else {
            // Game is over — show the game-over screen
            gameOver(g);
        }
    }

    // ---------------------------------------------------------------
    // gameOver() — renders the game-over message and a "Play Again" button
    // Called by draw() when inGame is false
    // ---------------------------------------------------------------
    public void gameOver(Graphics g) {
        // Build and centre the "Game Over" message string
        String msg = "Game Over! Score : " + score;
        Font font = new Font("SAN_SERIF", Font.BOLD, 14);
        FontMetrics metrices = getFontMetrics(font);

        g.setColor(Color.BLACK);
        g.setFont(font);
        // Centre the message horizontally and place it at the vertical midpoint
        g.drawString(msg, (300 - metrices.stringWidth(msg)) / 2, 300 / 2);

        // --- "Play Again" button layout calculations ---
        String buttonText = "Play Again";
        Font buttonFont = new Font("SAN_SERIF", Font.BOLD, 12);
        FontMetrics buttonMetrics = getFontMetrics(buttonFont);

        int buttonWidth  = buttonMetrics.stringWidth(buttonText) + 20; // Add horizontal padding
        int buttonHeight = 30;
        int buttonX      = (300 - buttonWidth) / 2;   // Horizontally centred
        int buttonY      = 300 / 2 + 10;              // Just below the "Game Over" text

        // Save button bounds so mouseClicked() can detect when the user clicks it
        playAgainButton = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);

        // Draw button background (green fill)
        g.setColor(new Color(100, 150, 50));
        g.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);

        // Draw button border (black outline)
        g.setColor(Color.BLACK);
        g.drawRect(buttonX, buttonY, buttonWidth, buttonHeight);

        // Draw the button label text
        g.setFont(buttonFont);
        g.drawString(buttonText, buttonX + 10, buttonY + 20);
    }

    // ---------------------------------------------------------------
    // move() — shifts all snake segments forward by one step in the current direction
    // The body "follows" the head by copying each segment's position from the one ahead of it
    // ---------------------------------------------------------------
    public void move() {
        // Shift every body segment one position toward the tail (from back to front)
        // so that segment i takes the previous position of segment i-1
        for (int i = dots; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Move the head (index 0) one DOT_SIZE step in the active direction
        if (leftDirection)  { x[0] = x[0] - DOT_SIZE; }
        if (rightDirection) { x[0] = x[0] + DOT_SIZE; }
        if (upDirection)    { y[0] = y[0] - DOT_SIZE; }
        if (downDirection)  { y[0] = y[0] + DOT_SIZE; }
    }

    // ---------------------------------------------------------------
    // checkApple() — tests whether the snake's head is on the apple
    // If so, grow the snake, increment the score, and move the apple
    // ---------------------------------------------------------------
    public void checkApple() {
        if ((x[0] == apple_x) && (y[0] == apple_y)) {
            dots++;           // Grow the snake by one segment
            score++;          // Increase the player's score
            locateApple();    // Randomly reposition the apple
        }
    }

    // ---------------------------------------------------------------
    // checkCollision() — ends the game if the snake hits itself or a wall
    // ---------------------------------------------------------------
    public void checkCollision() {
        // Check if the head overlaps any body segment beyond the 3rd segment
        // (shorter snakes can't physically overlap themselves)
        for (int i = dots - 1; i > 3; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                inGame = false;  // Head touched a body segment → game over
            }
        }

        // Check if the snake has crossed any of the four board boundaries
        if (y[0] >= 300) { inGame = false; }  // Hit the bottom wall
        if (x[0] >= 300) { inGame = false; }  // Hit the right wall
        if (y[0] < 0)    { inGame = false; }  // Hit the top wall
        if (x[0] < 0)    { inGame = false; }  // Hit the left wall

        // If a collision was detected, stop the game timer
        if (!inGame) {
            timer.stop();
        }
    }

    // ---------------------------------------------------------------
    // actionPerformed() — the game loop, called on every timer tick (every 140ms)
    // Performs one frame: check apple, check collisions, move snake, then repaint
    // ---------------------------------------------------------------
    public void actionPerformed(ActionEvent ae) {
        if (inGame) {
            checkApple();      // Did the snake eat the apple this frame?
            checkCollision();  // Did the snake hit a wall or itself?
            move();            // Advance the snake one step forward
        }

        repaint();  // Trigger paintComponent() to redraw the updated board
    }

    // ---------------------------------------------------------------
    // MOUSE LISTENER — only mouseClicked() is used (for the "Play Again" button)
    // The others are required by the MouseListener interface but left empty
    // ---------------------------------------------------------------

    @Override
    public void mouseClicked(MouseEvent e) {
        // If the game is over and the player clicks inside the "Play Again" button area, restart
        if (!inGame && playAgainButton != null) {
            if (playAgainButton.contains(e.getPoint())) {
                initGame();  // Reset all game variables and restart the timer
                repaint();   // Immediately redraw to show the fresh board
            }
        }
    }

    @Override public void mousePressed(MouseEvent e)  { /* Not used */ }
    @Override public void mouseReleased(MouseEvent e) { /* Not used */ }
    @Override public void mouseEntered(MouseEvent e)  { /* Not used */ }
    @Override public void mouseExited(MouseEvent e)   { /* Not used */ }

    // ---------------------------------------------------------------
    // TAdapter — inner class that handles keyboard input (arrow keys)
    // Prevents the snake from reversing directly into itself by checking
    // that the new direction is not the exact opposite of the current one
    // ---------------------------------------------------------------
    public class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();  // Get the code of the pressed key

            // LEFT arrow — only allowed if currently not moving right
            if (key == KeyEvent.VK_LEFT && (!rightDirection)) {
                leftDirection  = true;
                upDirection    = false;
                downDirection  = false;
            }

            // RIGHT arrow — only allowed if currently not moving left
            if (key == KeyEvent.VK_RIGHT && (!leftDirection)) {
                rightDirection = true;
                upDirection    = false;
                downDirection  = false;
            }

            // UP arrow — only allowed if currently not moving down
            if (key == KeyEvent.VK_UP && (!downDirection)) {
                upDirection    = true;
                leftDirection  = false;
                rightDirection = false;
            }

            // DOWN arrow — only allowed if currently not moving up
            if (key == KeyEvent.VK_DOWN && (!upDirection)) {
                downDirection  = true;
                leftDirection  = false;
                rightDirection = false;
            }
        }
    }
}
