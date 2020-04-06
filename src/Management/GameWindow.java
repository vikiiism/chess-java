package Management;

import GameSettings.Clock;
import GameSettings.StartMenuEngine;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.*;


public class GameWindow {
    public Clock blackClock;
    public Clock whiteClock;
    private Board board;

    private Timer timer;
    private JFrame gameWindow;

    public GameWindow(String blackName, String whiteName, int hh, int mm, int ss) {

        blackClock = new Clock(hh, ss, mm);
        whiteClock = new Clock(hh, ss, mm);

        gameWindow = new JFrame("Chess");


        try {
            Image whiteImg = ImageIO.read(getClass().getResource("wp.png"));
            gameWindow.setIconImage(whiteImg);
        } catch (Exception e) {
            System.out.println("Game file wp.png not found");
        }

        gameWindow.setLocation(100, 100);
        gameWindow.setLayout(new BorderLayout(20, 20));

        // Game Data window
        JPanel gameData = gameDataPanel(blackName, whiteName, hh, mm, ss);
        gameData.setSize(gameData.getPreferredSize());
        gameWindow.add(gameData, BorderLayout.NORTH);

        this.board = new Board(this);

        gameWindow.add(board, BorderLayout.CENTER);
        gameWindow.add(buttons(), BorderLayout.SOUTH);
        gameWindow.setMinimumSize(gameWindow.getPreferredSize());
        gameWindow.setSize(gameWindow.getPreferredSize());
        gameWindow.setResizable(false);
        gameWindow.pack();
        gameWindow.setVisible(true);
        gameWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void checkmateOccurred(int c) {
        String winner;
        if (c == 0) {
            winner = "White";
        } else {
            winner = "Black";
        }

        if (timer != null) timer.stop();
        int n = JOptionPane.showConfirmDialog(
                gameWindow, String.format(
                        "%s wins by checkmate! Set up a new game? \n", winner) +
                        "Choosing \"No\" lets you look at the final situation.",
                String.format("%s wins!", winner),
                JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {
            SwingUtilities.invokeLater(new StartMenuEngine());
            gameWindow.dispose();
        }
    }

// Helper function to create data panel

    private JPanel gameDataPanel(final String blackName, final String whiteName, final int hh, final int mm, final int ss) {

        JPanel gameData = new JPanel();
        gameData.setLayout(new GridLayout(3, 2, 0, 0));


        // PLAYER NAMES
        JLabel w = new JLabel(whiteName);
        JLabel b = new JLabel(blackName);

        w.setHorizontalAlignment(JLabel.CENTER);
        w.setVerticalAlignment(JLabel.CENTER);
        b.setHorizontalAlignment(JLabel.CENTER);
        b.setVerticalAlignment(JLabel.CENTER);

        w.setSize(w.getMinimumSize());
        b.setSize(b.getMinimumSize());

        gameData.add(w);
        gameData.add(b);

        // CLOCKS
        final JLabel bTime = new JLabel(blackClock.getTime());
        final JLabel wTime = new JLabel(whiteClock.getTime());

        bTime.setHorizontalAlignment(JLabel.CENTER);
        bTime.setVerticalAlignment(JLabel.CENTER);
        wTime.setHorizontalAlignment(JLabel.CENTER);
        wTime.setVerticalAlignment(JLabel.CENTER);

        if (!(hh == 0 && mm == 0 && ss == 0)) {
            timer = new Timer(1000, null);
            timer.addActionListener(e -> adjustClock(blackName, whiteName, hh, mm, ss, wTime));
            timer.start();
        } else {
            wTime.setText("Out of time game");
            bTime.setText("Out of time game");
        }

        gameData.add(wTime);
        gameData.add(bTime);

        gameData.setPreferredSize(gameData.getMinimumSize());

        return gameData;
    }

    private void adjustClock(String blackName, String whiteName, int hh, int mm, int ss, JLabel wTime) {
        boolean turn = board.getTurn();
        Clock movingPlayerClock;
        String otherPlayerName;

        if (turn) {
            movingPlayerClock = whiteClock;
            otherPlayerName = blackName;

        } else {
            movingPlayerClock = blackClock;
            otherPlayerName = whiteName;
        }

        movingPlayerClock.decr();
        wTime.setText(movingPlayerClock.getTime());

        if (movingPlayerClock.outOfTime()) {
            timer.stop();
            int answer = JOptionPane.showConfirmDialog(
                    gameWindow, otherPlayerName + " wins by time! Play a new game? \n" +
                            "Choosing \"No\" quits the game.",
                    otherPlayerName + " wins!",
                    JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.YES_OPTION) {
                new GameWindow(blackName, whiteName, hh, mm, ss);
            }
            gameWindow.dispose();
        }
    }

    private JPanel buttons() {
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1, 3, 10, 0));

        final JButton quit = new JButton("Quit");

        quit.addActionListener(e -> {
            int n = JOptionPane.showConfirmDialog(
                    gameWindow, "Are you sure you want to quit?", "Confirm quit", JOptionPane.YES_NO_OPTION);

            if (n == JOptionPane.YES_OPTION) {
                if (timer != null) timer.stop();
                gameWindow.dispose();
            }
        });

        final JButton nGame = new JButton("New game");

        nGame.addActionListener(e -> {
            int n = JOptionPane.showConfirmDialog(
                    gameWindow, "Are you sure you want to begin a new game?", "Confirm new game",
                    JOptionPane.YES_NO_OPTION);

            if (n == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(new StartMenuEngine());
                gameWindow.dispose();
            }
        });

        final JButton instr = new JButton("How to play");

        instr.addActionListener(e -> JOptionPane.showMessageDialog(gameWindow,
                "Move the chess pieces on the board by clicking\n"
                        + "and dragging. The game will watch out for illegal\n"
                        + "moves. You can win either by your opponent running\n"
                        + "out of time or by checkmating your opponent.\n"
                        + "\nGood luck, hope you enjoy the game!",
                "How to play",
                JOptionPane.PLAIN_MESSAGE));

        buttons.add(instr);
        buttons.add(nGame);
        buttons.add(quit);

        buttons.setPreferredSize(buttons.getMinimumSize());

        return buttons;
    }

}
