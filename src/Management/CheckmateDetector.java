package Management;

import Pieces.Bishop;
import Pieces.King;
import Pieces.Piece;
import Pieces.Queen;

import java.util.*;
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
    private King blackKing;
    private King whiteKing;
    private HashMap<Square, List<Piece>> whiteMoves;
    private HashMap<Square, List<Piece>> blackMoves;
    private final LinkedList<Square> SQUARES;


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
        SQUARES = new LinkedList<>();
        movableSquares = new LinkedList<>();
        whiteMoves = new HashMap<>();
        blackMoves = new HashMap<>();
        initializeBoard(board);

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
            pieces.removeAll(Collections.unmodifiableList(pieces));
        }

        for (List<Piece> pieces : blackMoves.values()) {
            pieces.removeAll(Collections.unmodifiableList(pieces));
        }

        movableSquares.removeAll(Collections.unmodifiableList(movableSquares));

        // Add each move white and black can make to map
        iterateAvailability(whiteIterator);
        iterateAvailability(blackIterator);
    }

    /**
     * Checks if the black king is threatened
     *
     * @return boolean representing whether the black king is in check.
     */
    public boolean blackInCheck() {
        update();

        Square position = blackKing.getPosition();
        if (!whiteMoves.get(position).isEmpty()) {
            return true;
        }

        movableSquares.addAll(SQUARES);
        return false;
    }

    /**
     * Checks if the white king is threatened
     *
     * @return boolean representing whether the white king is in check.
     */
    public boolean whiteInCheck() {
        update();

        Square position = whiteKing.getPosition();
        if (!blackMoves.get(position).isEmpty()) {
            return true;
        }

        movableSquares.addAll(SQUARES);
        return false;
    }

    /**
     * Checks whether black is in checkmate.
     *
     * @return boolean representing if black player is checkmated.
     */
    public boolean blackCheckMated() {
        // Check if black is in check
        if (!this.blackInCheck()) {
            return false;
        }

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
        if (!this.whiteInCheck()) {
            return false;
        }

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
        movableSquares.removeAll(Collections.unmodifiableList(movableSquares));

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

        movableSquares.addAll(SQUARES);
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

    private void initializeBoard(Board board) {
        Square[][] currentBoard = board.getBoard();

        // add all squares to squares list and as hashmap keys
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                SQUARES.add(currentBoard[y][x]);
                whiteMoves.put(currentBoard[y][x], new LinkedList<>());
                blackMoves.put(currentBoard[y][x], new LinkedList<>());
            }
        }
    }

    /*
     * Helper method to determine if the threatening piece can be captured.
     */
    private boolean canCapture(Map<Square, List<Piece>> positions, List<Piece> threats, King king) {

        if (threats.size() != 1) {
            return false;
        }

        boolean isCaptured = false;
        Square position = threats.get(0).getPosition();

        if (king.getLegalMoves(board).contains(position)) {
            movableSquares.add(position);
            if (testMove(king, position)) {
                isCaptured = true;
            }
        }

        ConcurrentLinkedDeque<Piece> captures = new ConcurrentLinkedDeque<>(positions.get(position));

        if (captures.isEmpty()) {
            return isCaptured;
        }

        movableSquares.add(position);
        for (Piece p : captures) {
            if (testMove(p, position)) {
                isCaptured = true;
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

        int max = 0, min = 0, currentPosition = 0;
        boolean isY = false;

        if (kingPosition.getXCoordinate() == threatsPosition.getXCoordinate()) {
            max = Math.max(kingPosition.getYCoordinate(), threatsPosition.getYCoordinate());
            min = Math.min(kingPosition.getYCoordinate(), threatsPosition.getYCoordinate());
            currentPosition = kingPosition.getXCoordinate();
            isY = false;
        } else if (kingPosition.getYCoordinate() == threatsPosition.getYCoordinate()) {
            max = Math.max(kingPosition.getXCoordinate(), threatsPosition.getXCoordinate());
            min = Math.min(kingPosition.getXCoordinate(), threatsPosition.getXCoordinate());
            currentPosition = kingPosition.getYCoordinate();
            isY = true;
        }
        isBlockable = fillBlockable(blockMoves, kingPosition, boardArray, max, min, currentPosition, isY);

        Class<? extends Piece> threatClass = threats.get(0).getClass();
        if (threatClass.equals(Queen.class) || threatClass.equals(Bishop.class)) {
            int kingX = kingPosition.getXCoordinate();
            int kingY = kingPosition.getYCoordinate();
            int threatX = threatsPosition.getXCoordinate();
            int threatY = threatsPosition.getYCoordinate();
            int increase = 0;
            int startIndex = 0;
            int endIndex = 0;

            if (kingY > threatY) {
                increase = 1;
            } else if (kingY < threatY) {
                increase = -1;
            }

            if (kingX > threatX) {
                startIndex = threatX + 1;
                endIndex = kingX;

            } else if (kingX < threatX) {
                startIndex = kingX + 1;
                endIndex = threatX;
            }

            for (int i = startIndex; i < endIndex; i++) {
                threatY += increase;
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

        return isBlockable;
    }

    private boolean fillBlockable(Map<Square, List<Piece>> blockMoves, Square kingPosition, Square[][] boardArray, int max, int min, int currentPosition, boolean isY) {
        boolean isTheBoardBlockable = false;
        for (int i = min + 1; i < max; i++) {

            List<Piece> blocks;
            if (isY) {
                blocks = blockMoves.get(boardArray[currentPosition][i]);
            } else {
                blocks = blockMoves.get(boardArray[i][currentPosition]);
            }
            ConcurrentLinkedDeque<Piece> blockers = new ConcurrentLinkedDeque<>(blocks);

            if (blockers.isEmpty()) {
                continue;
            }
            movableSquares.add(boardArray[i][kingPosition.getXCoordinate()]);

            for (Piece p : blockers) {
                if (testMove(p, boardArray[i][kingPosition.getXCoordinate()])) {
                    isTheBoardBlockable = true;
                }
            }
        }
        return isTheBoardBlockable;
    }

    /*
     *
     * Helper method to add each move can make to map
     * */

    private void iterateAvailability(Iterator<Piece> whiteIterator) {
        while (whiteIterator.hasNext()) {
            Piece piece = whiteIterator.next();

            if (piece.getClass().equals(King.class)) {
                continue;
            }

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
}
