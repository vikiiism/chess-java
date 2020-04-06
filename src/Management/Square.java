package Management;

import Pieces.Piece;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.*;

@SuppressWarnings("serial")
public class Square extends JComponent {

    private Board board;
    private Piece occupyingPiece;
    private boolean displayedPiece;
    private int xCoordinate;
    private int yCoordinate;
    private final int COLOR;

    public Square(Board board, int c, int xCoordinate, int yCoordinate) {
        this.board = board;
        this.COLOR = c;
        this.displayedPiece = true;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;

        this.setBorder(BorderFactory.createEmptyBorder());
    }

    public int getCOLOR() {
        return this.COLOR;
    }

    public Piece getOccupyingPiece() {
        return occupyingPiece;
    }

    public boolean isOccupied() {
        return (this.occupyingPiece != null);
    }

    public int getXCoordinate() {
        return this.xCoordinate;
    }

    public int getYCoordinate() {
        return this.yCoordinate;
    }

    public void setDisplay(boolean v) {
        this.displayedPiece = v;
    }

    public void put(Piece p) {
        this.occupyingPiece = p;
        p.setPosition(this);
    }

    public Piece removePiece() {
        Piece occupyingPiece = this.occupyingPiece;
        this.occupyingPiece = null;
        return occupyingPiece;
    }

    public void capture(Piece piece) {

        Piece occupyingPiece = getOccupyingPiece();

        if (occupyingPiece.getCOLOR() == 0) {
            board.blackPieces.remove(occupyingPiece);
        } else if (occupyingPiece.getCOLOR() == 1) {
            board.whitePieces.remove(occupyingPiece);
        }

        this.occupyingPiece = piece;
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (this.COLOR == 1) {
            graphics.setColor(new Color(221, 192, 127));
        } else {
            graphics.setColor(new Color(101, 67, 33));
        }

        graphics.fillRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());

        if (occupyingPiece != null && displayedPiece) {
            occupyingPiece.draw(graphics);
        }
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + xCoordinate;
        result = prime * result + yCoordinate;
        return result;
    }

}
