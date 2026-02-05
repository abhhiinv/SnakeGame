import javax.swing.*;
public class SnakeGame extends JFrame{
    SnakeGame(){
        super("Snake Xenia");
        add(new Board());
        pack(); //to refresh frame

        setLocationRelativeTo(null);
        setResizable(false);
    }
    public static void main(String[] args) {
        new SnakeGame().setVisible(true);
    }
}