package Pieces;

import Management.Board;
import Management.Square;

import java.util.List;

public class Bishop extends Piece {

    public Bishop(int color, Square initSq, String img_file) {
        super(color, initSq, img_file);
    }

    @Override
    public List<Square> getLegalMoves(Board b) {
        Square[][] board = b.getBoard();
        int x = this.getPosition().getXCoordinate();
        int y = this.getPosition().getYCoordinate();

        return getDiagonalOccupations(board, x, y);
    }
}
