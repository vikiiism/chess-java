package Pieces;

import Management.Board;
import Management.Square;

import java.util.LinkedList;
import java.util.List;

public class Queen extends Piece {

    public Queen(int color, Square initSq, String img_file) {
        super(color, initSq, img_file);
    }

    @Override
    public List<Square> getLegalMoves(Board b) {
        LinkedList<Square> legalMoves = new LinkedList<>();
        Square[][] board = b.getBoard();

        int x = this.getPosition().getXCoordinate();
        int y = this.getPosition().getYCoordinate();

        int[] occupations = getLinearOccupations(board, x, y);

        for (int i = occupations[0]; i <= occupations[1]; i++) {
            if (i != y) {
                legalMoves.add(board[i][x]);
            }
        }

        for (int i = occupations[2]; i <= occupations[3]; i++) {
            if (i != x) {
                legalMoves.add(board[y][i]);
            }
        }

        List<Square> bMoves = getDiagonalOccupations(board, x, y);

        legalMoves.addAll(bMoves);

        return legalMoves;
    }

}
