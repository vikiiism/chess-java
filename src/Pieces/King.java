package Pieces;

import Management.Board;
import Management.Square;

import java.util.LinkedList;
import java.util.List;

public class King extends Piece {

    public King(int color, Square initSq, String img_file) {
        super(color, initSq, img_file);
    }

    @Override
    public List<Square> getLegalMoves(Board b) {
        LinkedList<Square> legalMoves = new LinkedList<>();

        Square[][] board = b.getBoard();

        int x = this.getPosition().getXCoordinate();
        int y = this.getPosition().getYCoordinate();

        for (int i = 1; i > -2; i--) {
            for (int k = 1; k > -2; k--) {
                if (!(i == 0 && k == 0)) {
                    try {
                        if (!board[y + k][x + i].isOccupied() ||
                                board[y + k][x + i].getOccupyingPiece().getCOLOR()
                                        != this.getCOLOR()) {
                            legalMoves.add(board[y + k][x + i]);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return legalMoves;
    }

}
