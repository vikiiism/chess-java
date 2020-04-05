package Management;

import Pieces.Bishop;
import Pieces.King;
import Pieces.Piece;
import Pieces.Queen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * Component of the Chess game that detects check mates in the game.
 *
 * @author Jussi Lundstedt
 */
public class CheckmateDetector {
    private Board board;
    private LinkedList<Piece> whitePieces;
    private LinkedList<Piece> blackPieces;
    private LinkedList<Square> movableSquares;
    private final LinkedList<Square> squares;
    private King blackKing;
    private King whiteKing;
    private HashMap<Square, List<Piece>> whiteMoves;
    private HashMap<Square, List<Piece>> blackMoves;

    /**
     * Constructs a new instance of Management.CheckmateDetector on a given board. By
     * convention should be called when the board is in its initial state.
     *
     * @param board       The board which the detector monitors
     * @param whitePieces White pieces on the board.
     * @param blackPieces Black pieces on the board.
     * @param whiteKing   Pieces.Piece object representing the white king
     * @param blackKing   Pieces.Piece object representing the black king
     */
    public CheckmateDetector(Board board, LinkedList<Piece> whitePieces,
                             LinkedList<Piece> blackPieces, King whiteKing, King blackKing) {
        this.board = board;
        this.whitePieces = whitePieces;
        this.blackPieces = blackPieces;
        this.blackKing = blackKing;
        this.whiteKing = whiteKing;

        // Initialize other fields
        squares = new LinkedList<>();
        movableSquares = new LinkedList<>();
        whiteMoves = new HashMap<>();
        blackMoves = new HashMap<>();

        Square[][] currentBoard = board.getBoard();

        // add all squares to squares list and as hashmap keys
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                squares.add(currentBoard[y][x]);
                whiteMoves.put(currentBoard[y][x], new LinkedList<>());
                blackMoves.put(currentBoard[y][x], new LinkedList<>());
            }
        }

        // update situation
        update();
    }

    /**
     * Updates the object with the current situation of the game.
     */
    public void update() {
        // Iterators through pieces
        Iterator<Piece> whiteIterator = whitePieces.iterator();
        Iterator<Piece> blackIterator = blackPieces.iterator();

        // empty moves and movable squares at each update
        for (List<Piece> pieces : whiteMoves.values()) {
            pieces.removeAll(pieces);
        }

        for (List<Piece> pieces : blackMoves.values()) {
            pieces.removeAll(pieces);
        }

        movableSquares.removeAll(movableSquares);

        // Add each move white and black can make to map
        while (whiteIterator.hasNext()) {
            Piece piece = whiteIterator.next();

            if (!piece.getClass().equals(King.class)) {
                if (piece.getPosition() == null) {
                    whiteIterator.remove();
                    continue;
                }

                List<Square> moves = piece.getLegalMoves(board);
                for (Square move : moves) {
                    List<Piece> pieces = whiteMoves.get(move);
                    pieces.add(piece);
                }
            }
        }

        while (blackIterator.hasNext()) {
            Piece piece = blackIterator.next();

            if (!piece.getClass().equals(King.class)) {
                if (piece.getPosition() == null) {
                    whiteIterator.remove();
                    continue;
                }

                List<Square> moves = piece.getLegalMoves(board);
                for (Square move : moves) {
                    List<Piece> pieces = blackMoves.get(move);
                    pieces.add(piece);
                }
            }
        }
    }

    /**
     * Checks if the black king is threatened
     *
     * @return boolean representing whether the black king is in check.
     */
    public boolean blackInCheck() {
        update();
        Square position = blackKing.getPosition();
        if (whiteMoves.get(position).isEmpty()) {
            movableSquares.addAll(squares);
            return false;
        } else return true;
    }

    /**
     * Checks if the white king is threatened
     *
     * @return boolean representing whether the white king is in check.
     */
    public boolean whiteInCheck() {
        update();
        Square position = whiteKing.getPosition();
        if (blackMoves.get(position).isEmpty()) {
            movableSquares.addAll(squares);
            return false;
        } else return true;
    }

    /**
     * Checks whether black is in checkmate.
     *
     * @return boolean representing if black player is checkmated.
     */
    public boolean blackCheckMated() {
        // Check if black is in check
        if (!this.blackInCheck()) return false;

        // If yes, check if king can evade
        boolean isCheckmate = true;
        if (canEvade(whiteMoves, blackKing)) {
            isCheckmate = false;
        }

        // If no, check if threat can be captured
        List<Piece> threats = whiteMoves.get(blackKing.getPosition());
        if (canCapture(blackMoves, threats, blackKing)) {
            isCheckmate = false;
        }

        // If no, check if threat can be blocked
        if (canBlock(threats, blackMoves, blackKing)) {
            isCheckmate = false;
        }

        // If no possible ways of removing check, checkmate occurred
        return isCheckmate;
    }

    /**
     * Checks whether white is in checkmate.
     *
     * @return boolean representing if white player is checkmated.
     */
    public boolean whiteCheckMated() {
        // Check if white is in check
        if (!this.whiteInCheck()) return false;

        // If yes, check if king can evade
        boolean isCheckmate = true;
        if (canEvade(blackMoves, whiteKing)) {
            isCheckmate = false;
        }

        // If no, check if threat can be captured
        List<Piece> threats = blackMoves.get(whiteKing.getPosition());
        if (canCapture(whiteMoves, threats, whiteKing)) {
            isCheckmate = false;
        }

        // If no, check if threat can be blocked
        if (canBlock(threats, whiteMoves, whiteKing)) {
            isCheckmate = false;
        }

        // If no possible ways of removing check, isCheckmate occurred
        return isCheckmate;
    }

    /**
     * Method to get a list of allowable squares that the player can move.
     * Defaults to all squares, but limits available squares if player is in
     * check.
     *
     * @param b boolean representing whether it's white player's turn (if yes,
     *          true)
     * @return List of squares that the player can move into.
     */
    public List<Square> getAllowableSquares(boolean b) {
        movableSquares.removeAll(movableSquares);

        if (whiteInCheck()) {
            whiteCheckMated();
        } else if (blackInCheck()) {
            blackCheckMated();
        }

        return movableSquares;
    }

    /**
     * Tests a move a player is about to make to prevent making an illegal move
     * that puts the player in check.
     *
     * @param piece  Pieces.Piece moved
     * @param square Management.Square to which piece is about to move
     * @return false if move would cause a check
     */
    public boolean testMove(Piece piece, Square square) {
        Piece c = square.getOccupyingPiece();

        boolean isMovable = true;
        Square init = piece.getPosition();

        piece.move(square);
        update();

        if (piece.getCOLOR() == 0 && blackInCheck()) {
            isMovable = false;
        } else if (piece.getCOLOR() == 1 && whiteInCheck()) {
            isMovable = false;
        }

        piece.move(init);
        if (c != null) square.put(c);

        update();

        movableSquares.addAll(squares);
        return isMovable;
    }

    /*
     * Helper method to determine if the king can evade the check.
     * Gives a false positive if the king can capture the checking piece.
     */
    private boolean canEvade(Map<Square, List<Piece>> tMoves, King tKing) {
        boolean isEvade = false;
        List<Square> kingsMoves = tKing.getLegalMoves(board);

        // If king is not threatened at some square, it can evade
        for (Square position : kingsMoves) {
            if (!testMove(tKing, position)) continue;
            if (tMoves.get(position).isEmpty()) {
                movableSquares.add(position);
                isEvade = true;
            }
        }

        return isEvade;
    }

    /*
     * Helper method to determine if the threatening piece can be captured.
     */
    private boolean canCapture(Map<Square, List<Piece>> positions, List<Piece> threats, King king) {

        boolean isCaptured = false;
        if (threats.size() == 1) {
            Square position = threats.get(0).getPosition();

            if (king.getLegalMoves(board).contains(position)) {
                movableSquares.add(position);
                if (testMove(king, position)) {
                    isCaptured = true;
                }
            }

            List<Piece> caps = positions.get(position);
            ConcurrentLinkedDeque<Piece> captures = new ConcurrentLinkedDeque<>(caps);

            if (!captures.isEmpty()) {
                movableSquares.add(position);
                for (Piece p : captures) {
                    if (testMove(p, position)) {
                        isCaptured = true;
                    }
                }
            }
        }

        return isCaptured;
    }

    /*
     * Helper method to determine if check can be blocked by a piece.
     */
    private boolean canBlock(List<Piece> threats, Map<Square, List<Piece>> blockMoves, King king) {
        if (threats.size() != 1) {
            return false;
        }

        boolean isBlockable = false;

        Square threatsPosition = threats.get(0).getPosition();
        Square kingPosition = king.getPosition();
        Square[][] boardArray = board.getBoard();

        if (kingPosition.getXCoordinate() == threatsPosition.getXCoordinate()) {
            int max = Math.max(kingPosition.getYCoordinate(), threatsPosition.getYCoordinate());
            int min = Math.min(kingPosition.getYCoordinate(), threatsPosition.getYCoordinate());

            for (int i = min + 1; i < max; i++) {
                List<Piece> blocks = blockMoves.get(boardArray[i][kingPosition.getXCoordinate()]);
                ConcurrentLinkedDeque<Piece> blockers = new ConcurrentLinkedDeque<>(blocks);

                if (blockers.isEmpty()) {
                    continue;
                }
                movableSquares.add(boardArray[i][kingPosition.getXCoordinate()]);

                for (Piece p : blockers) {
                    if (testMove(p, boardArray[i][kingPosition.getXCoordinate()])) {
                        isBlockable = true;
                    }


                }
            }
        }

        if (kingPosition.getYCoordinate() == threatsPosition.getYCoordinate()) {
            int max = Math.max(kingPosition.getXCoordinate(), threatsPosition.getXCoordinate());
            int min = Math.min(kingPosition.getXCoordinate(), threatsPosition.getXCoordinate());

            for (int i = min + 1; i < max; i++) {
                List<Piece> blocks = blockMoves.get(boardArray[kingPosition.getYCoordinate()][i]);
                ConcurrentLinkedDeque<Piece> blockers = new ConcurrentLinkedDeque<>(blocks);

                if (blockers.isEmpty()) {
                    continue;
                }
                movableSquares.add(boardArray[i][kingPosition.getXCoordinate()]);

                for (Piece p : blockers) {
                    if (testMove(p, boardArray[i][kingPosition.getXCoordinate()])) {
                        isBlockable = true;
                    }


                }
            }
        }

        Class<? extends Piece> threatClass = threats.get(0).getClass();

        if (threatClass.equals(Queen.class) || threatClass.equals(Bishop.class)) {
            int kingX = kingPosition.getXCoordinate();
            int kingY = kingPosition.getYCoordinate();
            int threatX = threatsPosition.getXCoordinate();
            int threatY = threatsPosition.getYCoordinate();

            if (kingX > threatX && kingY > threatY) {
                for (int i = threatX + 1; i < kingX; i++) {

                    threatY++;
                    List<Piece> blocks = blockMoves.get(boardArray[threatY][i]);

                    ConcurrentLinkedDeque<Piece> blockers = new ConcurrentLinkedDeque<>(blocks);
                    if (blockers.isEmpty()) {
                        continue;
                    }

                    movableSquares.add(boardArray[threatY][i]);

                    for (Piece p : blockers) {
                        if (testMove(p, boardArray[threatY][i])) {
                            isBlockable = true;
                        }
                    }

                }
            }

            if (kingX > threatX && threatY > kingY) {
                for (int i = threatX + 1; i < kingX; i++) {
                    threatY--;
                    List<Piece> blocks = blockMoves.get(boardArray[threatY][i]);
                    ConcurrentLinkedDeque<Piece> blockers = new ConcurrentLinkedDeque<>(blocks);

                    if (blockers.isEmpty()) {
                        continue;
                    }

                    movableSquares.add(boardArray[threatY][i]);

                    for (Piece p : blockers) {
                        if (testMove(p, boardArray[threatY][i])) {
                            isBlockable = true;
                        }
                    }
                }
            }

            if (threatX > kingX && kingY > threatY) {
                for (int i = threatX - 1; i > kingX; i--) {
                    threatY++;
                    List<Piece> blocks = blockMoves.get(boardArray[threatY][i]);
                    ConcurrentLinkedDeque<Piece> blockers = new ConcurrentLinkedDeque<>(blocks);

                    if (!blockers.isEmpty()) {
                        movableSquares.add(boardArray[threatY][i]);

                        for (Piece p : blockers) {
                            if (testMove(p, boardArray[threatY][i])) {
                                isBlockable = true;
                            }
                        }
                    }
                }
            }

            if (threatX > kingX && threatY > kingY) {
                for (int i = threatX - 1; i > kingX; i--) {
                    threatY--;
                    List<Piece> blocks = blockMoves.get(boardArray[threatY][i]);
                    ConcurrentLinkedDeque<Piece> blockers = new ConcurrentLinkedDeque<>(blocks);

                    if (blockers.isEmpty()) {
                        continue;
                    }

                    movableSquares.add(boardArray[threatY][i]);

                    for (Piece p : blockers) {
                        if (testMove(p, boardArray[threatY][i])) {
                            isBlockable = true;
                        }
                    }
                }
            }
        }

        return isBlockable;
    }

}
