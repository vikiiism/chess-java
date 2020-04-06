package Pieces;

import Management.Board;
import Management.Square;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

public abstract class Piece {
    private final int COLOR;
    private Square currentSquare;
    private BufferedImage img;

    public Piece(int color, Square initSq, String img_file) {
        this.COLOR = color;
        this.currentSquare = initSq;

        try {
            this.img = ImageIO.read(getClass().getResource(img_file));
        } catch (IOException e) {
            System.out.println("File not found: " + e.getMessage());
        }
    }

    public boolean move(Square fin) {
        Piece occupy = fin.getOccupyingPiece();

        if (occupy != null) {
            if (occupy.getCOLOR() == this.COLOR) return false;
            else fin.capture(this);
        }

        Piece removedPiece = currentSquare.removePiece();
        this.currentSquare = fin;
        currentSquare.put(this);
        return true;
    }

    public Square getPosition() {
        return currentSquare;
    }

    public void setPosition(Square sq) {
        this.currentSquare = sq;
    }

    public int getCOLOR() {
        return COLOR;
    }

    public Image getImage() {
        return img;
    }

    public void draw(Graphics g) {
        int x = currentSquare.getX();
        int y = currentSquare.getY();

        g.drawImage(this.img, x, y, null);
    }

    public int[] getLinearOccupations(Square[][] board, int x, int y) {
        int lastYAbove = 0;
        int lastXRight = 7;
        int lastYBelow = 7;
        int lastXLeft = 0;

        for (int i = 0; i < y; i++) {
            if (board[i][x].isOccupied()) {
                if (board[i][x].getOccupyingPiece().getCOLOR() != this.COLOR) {
                    lastYAbove = i;
                } else {
                    lastYAbove = i + 1;
                }
            }
        }

        for (int i = 7; i > y; i--) {
            if (board[i][x].isOccupied()) {
                if (board[i][x].getOccupyingPiece().getCOLOR() != this.COLOR) {
                    lastYBelow = i;
                } else lastYBelow = i - 1;
            }
        }

        for (int i = 0; i < x; i++) {
            if (board[y][i].isOccupied()) {
                if (board[y][i].getOccupyingPiece().getCOLOR() != this.COLOR) {
                    lastXLeft = i;
                } else lastXLeft = i + 1;
            }
        }

        for (int i = 7; i > x; i--) {
            if (board[y][i].isOccupied()) {
                if (board[y][i].getOccupyingPiece().getCOLOR() != this.COLOR) {
                    lastXRight = i;
                } else lastXRight = i - 1;
            }
        }

        return new int[]{lastYAbove, lastYBelow, lastXLeft, lastXRight};
    }

    public List<Square> getDiagonalOccupations(Square[][] board, int x, int y) {
        LinkedList<Square> diagonalOccupations = new LinkedList<>();

        int xNW = x - 1;
        int xSW = x - 1;
        int xNE = x + 1;
        int xSE = x + 1;
        int yNW = y - 1;
        int ySW = y + 1;
        int yNE = y - 1;
        int ySE = y + 1;

        while (xNW >= 0 && yNW >= 0) {
            if (board[yNW][xNW].isOccupied()) {
                if (board[yNW][xNW].getOccupyingPiece().getCOLOR() != this.COLOR) {
                    diagonalOccupations.add(board[yNW][xNW]);
                }
                break;
            } else {
                diagonalOccupations.add(board[yNW][xNW]);
                yNW--;
                xNW--;
            }
        }

        while (xSW >= 0 && ySW < 8) {

            if (board[ySW][xSW].isOccupied()) {
                if (board[ySW][xSW].getOccupyingPiece().getCOLOR() != this.COLOR) {
                    diagonalOccupations.add(board[ySW][xSW]);
                }
                break;
            } else {
                diagonalOccupations.add(board[ySW][xSW]);
                ySW++;
                xSW--;
            }
        }

        while (xSE < 8 && ySE < 8) {
            if (board[ySE][xSE].isOccupied()) {
                if (board[ySE][xSE].getOccupyingPiece().getCOLOR() != this.COLOR) {
                    diagonalOccupations.add(board[ySE][xSE]);
                }
                break;
            } else {
                diagonalOccupations.add(board[ySE][xSE]);
                ySE++;
                xSE++;
            }
        }

        while (xNE < 8 && yNE >= 0) {
            if (board[yNE][xNE].isOccupied()) {
                if (board[yNE][xNE].getOccupyingPiece().getCOLOR() != this.COLOR) {
                    diagonalOccupations.add(board[yNE][xNE]);
                }
                break;
            } else {
                diagonalOccupations.add(board[yNE][xNE]);
                yNE--;
                xNE++;
            }
        }

        return diagonalOccupations;
    }

    // No implementation, to be implemented by each subclass
    public abstract List<Square> getLegalMoves(Board b);
}