
import java.awt.Color;
import java.awt.Image;
import javax.swing.*;

public class Board extends JPanel{
    private Image apple;
    private Image dot;
    private Image head;

    private final int ALL_DOTS = 900;
    private final int DOT_SIZE = 10;

    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[DOT_SIZE];

    private int dots;
    Board() {
        setBackground(new Color(149,209,1));//bg color
        setFocusable(true);

        loadImages();
        //initGame();
    }

    public void loadImages(){
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/apple.png"));
        apple = i1.getImage();
        ImageIcon i2 = new ImageIcon(ClassLoader.getSystemResource("icons/dot.png"));
        dot = i2.getImage();
        ImageIcon i3 = new ImageIcon(ClassLoader.getSystemResource("icons/head.png"));
        head = i3.getImage();
    }
    public void initGame(){
        dots = 3;

        for(int i=0; i<dots; i++){
            y[i]=50;
            x[i]=50;
        }
    }
}
