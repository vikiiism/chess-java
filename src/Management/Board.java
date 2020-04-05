package Management;

import Pieces.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

@SuppressWarnings("serial")
public class Board extends JPanel implements MouseListener, MouseMotionListener {

    public List<Square> movable;
    // List of pieces and whether they are movable
    public final LinkedList<Piece> blackPieces;
    public final LinkedList<Piece> whitePieces;

    private boolean whiteTurn;
    private Piece currentPiece;
    private int currentX;
    private int currentY;

    private CheckmateDetector checkmateDetector;

    // Logical and graphical representations of board
    private final Square[][] board;
    private final GameWindow gameWindow;

    // Resource location constants for piece images
    private static final String RESOURCES_WHITE_BISHOP_PNG = "wbishop.png";
    private static final String RESOURCES_BLACK_BISHOP_PNG = "bbishop.png";
    private static final String RESOURCES_WHITE_KNIGHT_PNG = "wknight.png";
    private static final String RESOURCES_BLACK_KNIGHT_PNG = "bknight.png";
    private static final String RESOURCES_WHITE_ROOK_PNG = "wrook.png";
    private static final String RESOURCES_BLACK_ROOK_PNG = "brook.png";
    private static final String RESOURCES_WHITE_KING_PNG = "wking.png";
    private static final String RESOURCES_BLACK_KING_PNG = "bking.png";
    private static final String RESOURCES_BLACK_QUEEN_PNG = "bqueen.png";
    private static final String RESOURCES_WHITE_QUEEN_PNG = "wqueen.png";
    private static final String RESOURCES_WHITE_PAWN_PNG = "wpawn.png";
    private static final String RESOURCES_BLACK_PAWN_PNG = "bpawn.png";

    public Board(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        board = new Square[8][8];
        blackPieces = new LinkedList<>();
        whitePieces = new LinkedList<>();
        setLayout(new GridLayout(8, 8, 0, 0));

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int xMod = x % 2;
                int yMod = y % 2;

                if ((xMod == 0 && yMod == 0) || (xMod == 1 && yMod == 1)) {
                    board[x][y] = new Square(this, 1, y, x);
                } else {
                    board[x][y] = new Square(this, 0, y, x);
                }
                this.add(board[x][y]);
            }
        }

        initializePieces();

        this.setPreferredSize(new Dimension(400, 400));
        this.setMaximumSize(new Dimension(400, 400));
        this.setMinimumSize(this.getPreferredSize());
        this.setSize(new Dimension(400, 400));
        whiteTurn = true;

    }

    public Square[][] getBoard() {
        return this.board;
    }

    public boolean getTurn() {
        return whiteTurn;
    }

    public Piece getCurrentPiece() {
        return this.currentPiece;
    }

    public void setCurrentPiece(Piece p) {
        this.currentPiece = p;
    }

    @Override
    public void paintComponent(Graphics graphics) {
//         super.paintComponent(g);

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Square square = board[y][x];
                square.paintComponent(graphics);
            }
        }

        if (currentPiece != null) {
            if ((currentPiece.getCOLOR() == 1 && whiteTurn)
                    || (currentPiece.getCOLOR() == 0 && !whiteTurn)) {
                final Image i = currentPiece.getImage();
                graphics.drawImage(i, currentX, currentY, null);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        currentX = e.getX();
        currentY = e.getY();

        Square square = (Square) this.getComponentAt(new Point(e.getX(), e.getY()));

        if (square.isOccupied()) {
            currentPiece = square.getOccupyingPiece();
            if (currentPiece.getCOLOR() == 0 && whiteTurn)
                return;
            if (currentPiece.getCOLOR() == 1 && !whiteTurn)
                return;
            square.setDisplay(false);
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Square square = (Square) this.getComponentAt(new Point(e.getX(), e.getY()));

        if (currentPiece != null) {
            if (currentPiece.getCOLOR() == 0 && whiteTurn)
                return;
            if (currentPiece.getCOLOR() == 1 && !whiteTurn)
                return;

            List<Square> legalMoves = currentPiece.getLegalMoves(this);
            movable = checkmateDetector.getAllowableSquares(whiteTurn);

            if (legalMoves.contains(square) && movable.contains(square)
                    && checkmateDetector.testMove(currentPiece, square)) {
                square.setDisplay(true);
                currentPiece.move(square);
                checkmateDetector.update();

                if (checkmateDetector.blackCheckMated()) {
                    currentPiece = null;
                    repaint();
                    this.removeMouseListener(this);
                    this.removeMouseMotionListener(this);
                    gameWindow.checkmateOccurred(0);
                } else if (checkmateDetector.whiteCheckMated()) {
                    currentPiece = null;
                    repaint();
                    this.removeMouseListener(this);
                    this.removeMouseMotionListener(this);
                    gameWindow.checkmateOccurred(1);
                } else {
                    currentPiece = null;
                    whiteTurn = !whiteTurn;
                    movable = checkmateDetector.getAllowableSquares(whiteTurn);
                }

            } else {
                currentPiece.getPosition().setDisplay(true);
                currentPiece = null;
            }
        }

        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentX = e.getX() - 24;
        currentY = e.getY() - 24;

        repaint();
    }

    // Irrelevant methods, do nothing for these mouse behaviors
    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }


    private void initializePieces() {

        putPawns();
        putRooks();
        putKnights();
        putBishops();

        board[7][3].put(new Queen(1, board[7][3], RESOURCES_WHITE_QUEEN_PNG));
        board[0][3].put(new Queen(0, board[0][3], RESOURCES_BLACK_QUEEN_PNG));

        King blackKing = new King(0, board[0][4], RESOURCES_BLACK_KING_PNG);
        King whiteKing = new King(1, board[7][4], RESOURCES_WHITE_KING_PNG);
        board[0][4].put(blackKing);
        board[7][4].put(whiteKing);

        setOccupyingPieces();

        checkmateDetector = new CheckmateDetector(this, whitePieces, blackPieces, whiteKing, blackKing);
    }

    private void setOccupyingPieces() {
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 8; x++) {
                blackPieces.add(board[y][x].getOccupyingPiece());
                whitePieces.add(board[7 - y][x].getOccupyingPiece());
            }
        }
    }

    private void putPawns() {
        for (int x = 0; x < 8; x++) {
            board[1][x].put(new Pawn(0, board[1][x], RESOURCES_BLACK_PAWN_PNG));
            board[6][x].put(new Pawn(1, board[6][x], RESOURCES_WHITE_PAWN_PNG));
        }
    }

    private void putBishops() {
        board[0][2].put(new Bishop(0, board[0][2], RESOURCES_BLACK_BISHOP_PNG));
        board[0][5].put(new Bishop(0, board[0][5], RESOURCES_BLACK_BISHOP_PNG));
        board[7][2].put(new Bishop(1, board[7][2], RESOURCES_WHITE_BISHOP_PNG));
        board[7][5].put(new Bishop(1, board[7][5], RESOURCES_WHITE_BISHOP_PNG));
    }

    private void putKnights() {
        board[0][1].put(new Knight(0, board[0][1], RESOURCES_BLACK_KNIGHT_PNG));
        board[0][6].put(new Knight(0, board[0][6], RESOURCES_BLACK_KNIGHT_PNG));
        board[7][1].put(new Knight(1, board[7][1], RESOURCES_WHITE_KNIGHT_PNG));
        board[7][6].put(new Knight(1, board[7][6], RESOURCES_WHITE_KNIGHT_PNG));
    }

    private void putRooks() {
        board[0][0].put(new Rook(0, board[0][0], RESOURCES_BLACK_ROOK_PNG));
        board[0][7].put(new Rook(0, board[0][7], RESOURCES_BLACK_ROOK_PNG));
        board[7][0].put(new Rook(1, board[7][0], RESOURCES_WHITE_ROOK_PNG));
        board[7][7].put(new Rook(1, board[7][7], RESOURCES_WHITE_ROOK_PNG));
    }

}