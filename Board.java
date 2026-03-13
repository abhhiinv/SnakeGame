import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Board extends JPanel implements ActionListener {

    private Image apple;
    private Image dot;
    private Image head;

    private final int ALL_DOTS    = 900;
    private final int DOT_SIZE    = 20;
    private final int RANDOM_POSITION = 29;
    private final int BOARD_SIZE  = 600;

    // Normal apple
    private int apple_x, apple_y;

    // Special apple
    private boolean specialAppleActive = false;
    private int special_x, special_y;
    private int specialAppleTimer = 0;          // counts actionPerformed ticks
    private final int SPECIAL_APPLE_DURATION = 71; // ~10 sec at 140ms; recalculated per difficulty
    private int specialAppleDuration;
    private int normalApplesEaten = 0;          // resets after spawning special
    private final int APPLES_FOR_SPECIAL = 5;
    private final int SPECIAL_POINTS = 5;

    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];

    private boolean leftDirection  = false;
    private boolean rightDirection = true;
    private boolean upDirection    = false;
    private boolean downDirection  = false;

    private boolean inGame = true;

    private int dots;
    private int score;
    private int timerDelay;
    private Timer timer;

    // Callback to show menu again after game over + Play Again
    public interface GameOverListener {
        void onGameOver();
    }
    private GameOverListener gameOverListener;

    Board(int timerDelay, GameOverListener gameOverListener) {
        this.timerDelay       = timerDelay;
        this.gameOverListener = gameOverListener;
        // Special apple lasts 10 seconds regardless of speed
        this.specialAppleDuration = (int)(10000.0 / timerDelay);

        addKeyListener(new TAdapter());
        setBackground(new Color(149, 209, 1));
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setFocusable(true);

        loadImages();
        initGame();
    }

    public void loadImages() {
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/apple.png"));
        apple = i1.getImage();
        ImageIcon i2 = new ImageIcon(ClassLoader.getSystemResource("icons/dot.png"));
        dot = i2.getImage();
        ImageIcon i3 = new ImageIcon(ClassLoader.getSystemResource("icons/head.png"));
        head = i3.getImage();
    }

    public void initGame() {
        dots  = 3;
        score = 0;
        normalApplesEaten  = 0;
        specialAppleActive = false;
        specialAppleTimer  = 0;

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
        } while (special_x == apple_x && special_y == apple_y); // avoid overlap
        specialAppleActive = true;
        specialAppleTimer  = 0;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (inGame) {
            // Score
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            g.drawString("Score: " + score, 20, 40);

            // Normal apple
            g.drawImage(apple, apple_x, apple_y, this);

            // Special apple — draw as glowing gold circle if no custom icon
            if (specialAppleActive) {
                drawSpecialApple(g);
            }

            // Snake
            for (int i = 0; i < dots; i++) {
                if (i == 0) {
                    g.drawImage(head, x[i], y[i], this);
                } else {
                    g.drawImage(dot, x[i], y[i], this);
                }
            }

            // Special apple countdown bar
            if (specialAppleActive) {
                drawTimerBar(g);
            }

            Toolkit.getDefaultToolkit().sync();
        } else {
            gameOver(g);
        }
    }

    private void drawSpecialApple(Graphics g) {
        // Gold pulsing circle to stand out from normal apple
        g.setColor(new Color(3, 150, 255));
        g.fillOval(special_x, special_y, DOT_SIZE, DOT_SIZE);
        g.setColor(new Color(34, 97, 224));
        g.drawOval(special_x, special_y, DOT_SIZE, DOT_SIZE);
    }

    private void drawTimerBar(Graphics g) {
        int barWidth = BOARD_SIZE - 40;
        int remaining = specialAppleDuration - specialAppleTimer;
        int filledWidth = (int)((double) remaining / specialAppleDuration * barWidth);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(20, BOARD_SIZE - 24, barWidth, 12);

        g.setColor(new Color(3, 150, 255));
        g.fillRect(20, BOARD_SIZE - 24, filledWidth, 12);
    }

    public void gameOver(Graphics g) {
        // Dim background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);

        String msg = "Game Over!  Score: " + score;
        Font font = new Font("SansSerif", Font.BOLD, 28);
        FontMetrics fm = getFontMetrics(font);

        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString(msg, (BOARD_SIZE - fm.stringWidth(msg)) / 2, BOARD_SIZE / 2 - 40);

        // Play Again button drawn as text — actual button added below
    }

    public void checkApple() {
        // Normal apple
        if (x[0] == apple_x && y[0] == apple_y) {
            dots++;
            score++;
            normalApplesEaten++;
            locateApple();

            // Spawn special apple every 5 normal apples
            if (normalApplesEaten % APPLES_FOR_SPECIAL == 0) {
                locateSpecialApple();
            }
        }

        // Special apple
        if (specialAppleActive && x[0] == special_x && y[0] == special_y) {
            dots++;
            score += SPECIAL_POINTS;
            specialAppleActive = false;
        }
    }

    public void checkCollision() {
        for (int i = dots; i > 0; i--) {
            if ((i > 4) && (x[0] == x[i]) && (y[0] == y[i])) {
                inGame = false;
            }
        }
        if (y[0] >= BOARD_SIZE) inGame = false;
        if (x[0] >= BOARD_SIZE) inGame = false;
        if (y[0] < 0)           inGame = false;
        if (x[0] < 0)           inGame = false;

        if (!inGame) {
            timer.stop();
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

            // Tick special apple timer
            if (specialAppleActive) {
                specialAppleTimer++;
                if (specialAppleTimer >= specialAppleDuration) {
                    specialAppleActive = false; // expired
                }
            }
        }
        repaint();
    }

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