
import GameSettings.StartMenuEngine;

import javax.swing.*;

public class Game implements Runnable {
    public void run() {
        SwingUtilities.invokeLater(new StartMenuEngine());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Game());
    }
}
