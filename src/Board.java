import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Board {

    final int BOARD_SIZE = 8;
    private Piece[][] board;

    public Board() {
        board = new Piece[BOARD_SIZE][BOARD_SIZE];
    }

    public Piece[][] getBoard() { return board; }

    /**
     * Function to set up the board with pieces in normal start positions
     */
    public void setupBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Piece(1, i, false, 'P');
            board[6][i] = new Piece(6, i, true, 'P');
        }
        board[0][0] = new Piece(0, 0, false, 'R');
        board[0][7] = new Piece(0, 7, false, 'R');
        board[7][7] = new Piece(7, 7, true, 'R');
        board[7][0] = new Piece(7, 0, true, 'R');
        board[0][1] = new Piece(0, 1, false, 'N');
        board[0][6] = new Piece(0, 6, false, 'N');
        board[7][6] = new Piece(7, 6, true, 'N');
        board[7][1] = new Piece(7, 1, true, 'N');
        board[0][2] = new Piece(0, 2, false, 'B');
        board[0][5] = new Piece(0, 5, false, 'B');
        board[7][2] = new Piece(7, 2, true, 'B');
        board[7][5] = new Piece(7, 5, true, 'B');
        board[0][3] = new Piece(0, 3, false, 'Q');
        board[7][3] = new Piece(7, 3, true, 'Q');
        board[0][4] = new Piece(0, 4, false, 'K');
        board[7][4] = new Piece(7, 4, true, 'K');
    }

    public void generateMoves(Piece[][] board, boolean col) {
        Piece king = findKing(board, col);
        if (king == null) {
            System.out.println("king moves not able to be generated");
            return;
        }
        Piece[][] boardMinusKing = deepCopyBoard(board);
        boardMinusKing[king.getX()][king.getY()] = null;

        boolean[][] attackedSquareMask = allPiecesAttackedSquareMask(boardMinusKing, !col);
        boolean[][] pinnedMask = new boolean[BOARD_SIZE][BOARD_SIZE];
        boolean[][] tempKingMoves = new boolean[BOARD_SIZE][BOARD_SIZE];

        bitMaskKingAttack(board, tempKingMoves, king.getX(), king.getY(), col, false);
        filterMask(tempKingMoves, attackedSquareMask);
        king.setMoveMask(tempKingMoves);

        Stream.Builder<Pair> streamPieces = Stream.builder();
        streamPieces.accept(kingCheckedDirectionalUpdate(board, pinnedMask, king.getX(), king.getY(), col));
        streamPieces.accept(kingCheckedKnight(board, king.getX(), king.getY(), col));
        streamPieces.accept(kingCheckedPawn(board, king.getX(), king.getY(), col));

        ArrayList<Pair> checkingPieces = streamPieces.build()
                .filter(x -> (x.getX() != -1 && x.getY() != -1))
                .collect(Collectors.toCollection(ArrayList::new));

        if (checkingPieces.size() == 2) {
            // king is in double check, only king moves are available, so no other calculation needed
        } else if (checkingPieces.size() == 1) {
            // king is in single check, can move king, capture attacking piece, or block if it's a sliding piece
            boolean[][] captureBlockMask = new boolean[BOARD_SIZE][BOARD_SIZE];
            Pair attacker = checkingPieces.get(0);
            captureBlockMask[attacker.getX()][attacker.getY()] = true;
            if (board[attacker.getX()][attacker.getY()].getSymbol() == 'Q'
                    || board[attacker.getX()][attacker.getY()].getSymbol() == 'R'
                    || board[attacker.getX()][attacker.getY()].getSymbol() == 'B') {
                updateCaptureBlockMask(captureBlockMask, attacker, new Pair(king.getX(), king.getY()));
            }
            Piece[][] boardCopy = deepCopyBoard(board);
            removePinnedPieces(boardCopy, pinnedMask);
        } else {
            // king is not in check, can proceed with normal move generation
            Piece[][] boardCopy = deepCopyBoard(board);
        }
    }

    /**
     * Function to iterate over the board, and for each piece of the given colour add the squares which that piece attacks to a 2D
     * boolean array. Attacked squares can include pieces of the same colour as the piece the attacking squares are being calculated for.
     * @param board             the current board state
     * @param col               the colour of the pieces the attacked squares are being calculated for
     * @return                  the updated mask containing all the attacked squares by pieces of the given colour
     */
    private boolean[][] allPiecesAttackedSquareMask(Piece[][] board, boolean col) {
        boolean[][] attackedSquareMask = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                if (board[i][j] != null && board[i][j].getColor() == col) {
                    attackedSquareMask = getBitMaskAttack(board, attackedSquareMask, board[i][j], true);
                }
            }
        }
        return attackedSquareMask;
    }

    /**
     * Function to update the captureBlockMask in the case that the king is in single check by a sliding piece, as its possible
     * there are squares between the king and the attacking piece which other pieces can move into to block the attack. These squares
     * should be added to the mask.
     * @param mask              the 2D boolean array marking squares on the board a piece can capture or block on to prevent check
     * @param attacker          the coordinates of the attacking sliding piece
     * @param king              the coordinates of the king being attacked
     */
    private void updateCaptureBlockMask(boolean[][] mask, Pair attacker, Pair king) {
        int xDiff = king.getX() == attacker.getX() ? 0
                : king.getX() - attacker.getX() > 0 ? -1
                : 1;
        int yDiff = king.getY() == attacker.getY() ? 0
                : king.getY() - attacker.getY() > 0 ? -1
                : 1;
        int x = king.getX() + xDiff;
        int y = king.getY() + yDiff;
        while (x != attacker.getX() || y != attacker.getY()) {
            mask[x][y] = true;
            x += xDiff;
            y += yDiff;
        }
    }

    /**
     * Function to check in each direction starting from the king to detect opponent sliding pieces which may be putting the king in
     * check (bishop, rook, queen) and return their position if this is the case. Additionally, pieces of the same colour which may be
     * pinned by these sliding pieces are also marked in the pinnedMask 2D boolean array.
     * @param board             the current board state
     * @param pinnedMask        the 2D boolean array to mark squares containing a pinned piece
     * @param x                 the x coordinate of the king
     * @param y                 the y coordinate of the king
     * @param col               the colour of the king
     * @return                  the coordinates of the piece which is putting the king in check, if no pieces are putting the king
     *                          in check then the returned coordinates will be (-1, -1)
     */
    private Pair kingCheckedDirectionalUpdate(Piece[][] board, boolean[][] pinnedMask, int x, int y, boolean col) {
        Pair res = new Pair(-1, -1);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                Pair potentialPin = new Pair(-1, -1);
                while (true) {
                    x += i;
                    y += j;
                    if (!withinBounds(x, y)) {
                        break;
                    }
                    if (board[x][y] == null) {
                        continue;
                    }
                    if (board[x][y].getColor() == col) {
                        if (potentialPin.getX() != -1 && potentialPin.getY() != -1) {
                            break;
                        }
                        potentialPin.setX(x);
                        potentialPin.setY(y);
                    } else if (board[x][y].getSymbol() == 'Q' || board[x][y].getSymbol() == 'B'
                            || board[x][y].getSymbol() == 'R') {
                        if (potentialPin.getX() != -1 && potentialPin.getY() != -1) {
                            pinnedMask[potentialPin.getX()][potentialPin.getY()] = true;
                            board[potentialPin.getX()][potentialPin.getY()].setPinned(true);
                        } else {
                            res.setX(x);
                            res.setY(y);
                        }
                        break;
                    }
                }
            }
        }
        return res;
    }

    /**
     * Function to check one knight move in each direction from the king if there is a knight of opposite colour which is putting
     * the king in check, and return the position of the knight if so.
     * @param board             the current board state
     * @param x                 the x coordinate of the king
     * @param y                 the y coordinate of the king
     * @param col               the colour of the king
     * @return                  the position of the knight which is putting the king in check, or (-1, -1) if there is no knight
     *                          putting the king in check
     */
    private Pair kingCheckedKnight(Piece[][] board, int x, int y, boolean col) {
        for (int i = -2; i <= 2; i += 4) {
            for (int j = -1; j <= 1; j += 2) {
                if (withinBounds(x + i, y + j) && board[x + i][y + j] != null
                        && board[x + i][y + j].getColor() != col && board[x + i][y + j].getSymbol() == 'N') {
                    return new Pair(x + i, y + j);
                }
                if (withinBounds(x + j, y + i) && board[x + j][y + i] != null
                        && board[x + j][y + i].getColor() != col && board[x + j][y + i].getSymbol() == 'N') {
                    return new Pair(x + j, y + i);
                }
            }
        }
        return new Pair(-1, -1);
    }

    /**
     * Function to if there are any pawns of the opposite colour attacking the king on the given coordinates
     * @param board             the current board state
     * @param x                 the x coordinate of the king
     * @param y                 the y coordinate of the king
     * @param col               the colour of the king
     * @return                  the position of the pawn attacking the king, or if there is none then (-1, -1)
     */
    private Pair kingCheckedPawn(Piece[][] board, int x, int y, boolean col) {
        int vert = col ? -1 : 1;
        x += vert;
        for (int i = -1; i <=1; i += 2) {
            if (withinBounds(x, y + i) && board[x][y + i] != null
                    && board[x][y + i].getColor() != col && board[x][y + i].getSymbol() == 'P') {
                return new Pair(x, y + i);
            }
        }
        return new Pair(-1, -1);
    }

    /**
     * Function to return a 2D boolean array showing all the squares that a given piece on the board attacks
     * @param board             the current board state
     * @param mask              the mask representing attacked squares
     * @param piece             the piece we are calculating attacking squares for
     * @param includeOwn        boolean indicating whether pieces of same colour included in the mask
     * @return                  the updated mask representing the attacked squares
     */
    public boolean[][] getBitMaskAttack(Piece[][] board, boolean[][] mask, Piece piece, boolean includeOwn) {
        switch (piece.getSymbol()) {
            case 'P' -> bitMaskPawnAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), includeOwn);
            case 'R' -> {
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 0, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, 1, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 0, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, -1, piece.getColor(), includeOwn);
            }
            case 'B' -> {
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 1, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, -1, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 1, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, -1, piece.getColor(), includeOwn);
            }
            case 'N' -> bitMaskKnightAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), includeOwn);
            case 'Q' -> {
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 1, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, -1, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 1, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, -1, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 0, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, 1, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 0, piece.getColor(), includeOwn);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, -1, piece.getColor(), includeOwn);
            }
            case 'K' -> bitMaskKingAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), includeOwn);
            default -> System.out.println("invalid piece symbol, cant calculate attack mask");
        }
        return mask;
    }

    /**
     * Function to mark bits in the 2D mask array to represent attacked squares, based on the following params:
     * @param board             the current board state
     * @param mask              the mask which is updated with additional attacked squares
     * @param x                 the starting x coordinate
     * @param y                 the starting y coordinate
     * @param addx              the increment to add to x
     * @param addy              the increment to add to y
     * @param col               the colour of the moving piece
     * @param includeOwn        boolean to mark if pieces of the same colour should be included in the mask
     */
    private void bitMaskDirectional(Piece[][] board, boolean[][] mask, int x, int y, int addx, int addy, boolean col, boolean includeOwn) {
        while (true) {
            x += addx;
            y += addy;
            if (!withinBounds(x, y)) {
                break;
            }
            if (board[x][y] == null) {
                mask[x][y] = true;
            } else if (board[x][y].getColor() != col || includeOwn) {
                mask[x][y] = true;
                break;
            } else {
                break;
            }
        }
    }

    /**
     * Function to update a given bit mask with the squares attacked by a pawn in the given position on the board
     * @param board             the current board state
     * @param mask              the 2D array boolean mask to represent attacked squares
     * @param x                 the x coordinate of the pawn
     * @param y                 the y coordinate of the pawn
     * @param col               the colour of the piece
     * @param includeOwn        boolean indicating whether pieces of same colour should be included in mask
     */
    private void bitMaskPawnAttack(Piece[][] board, boolean[][] mask, int x, int y, boolean col, boolean includeOwn) {
        int vert = col ? -1 : 1;
        x += vert;
        for (int i = -1; i <=1; i += 2) {
            if (withinBounds(x, y + i) && (board[x][y + i] == null || board[x][y + i].getColor() != col || includeOwn)) {
                mask[x][y + i] = true;
            }
        }
    }

    /**
     * Function to update a given bit mask with the squares attacked by the knight at the given position
     * @param board             the current board state
     * @param mask              the 2D boolean array representing attacked squares
     * @param x                 the x coordinate of the knight
     * @param y                 the y coordinate of the knight
     * @param col               the colour of the knight
     * @param includeOwn        boolean to indicate if pieces of the same colour should be included in the mask
     */
    private void bitMaskKnightAttack(Piece[][] board, boolean[][] mask, int x, int y, boolean col, boolean includeOwn) {
        for (int i = -2; i <= 2; i += 4) {
            for (int j = -1; j <= 1; j += 2) {
                if (withinBounds(x + i, y + j) && (board[x + i][y + j] == null
                        || board[x + i][y + j].getColor() != col || includeOwn)) {
                    mask[x + i][y + j] = true;
                }
                if (withinBounds(x + j, y + i) && (board[x + j][y + i] == null
                        || board[x + j][y + i].getColor() != col || includeOwn)) {
                    mask[x + j][y + i] = true;
                }
            }
        }
    }

    /**
     * Function to update a given bit mask with the squares attacked by the king at the given position
     * @param board             the current board state
     * @param mask              the 2D boolean array representing attacked squares
     * @param x                 the x coordinate of the king
     * @param y                 the y coordinate of the king
     * @param col               the colour of the king
     * @param includeOwn        boolean to indicate if pieces of the same colour should be included in the mask
     */
    private void bitMaskKingAttack(Piece[][] board, boolean[][] mask, int x, int y, boolean col, boolean includeOwn) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <=1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (withinBounds(x + i, y + j) && (board[x + i][y + j] == null
                        || board[x + i][y + j].getColor() != col || includeOwn)) {
                    mask[x + i][y + j] = true;
                }
            }
        }
    }

    /**
     * Function to check that the provided coordinates are within the bounds of the chess board
     * @param x                 the x coordinate
     * @param y                 the y coordinate
     * @return                  boolean value if within bounds
     */
    private boolean withinBounds(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    /**
     * Function to return the board coordinates of the king of the given color, and null if not found (which should not happen
     * if everything is working properly)
     * @param board             the current board state
     * @param col               the colour of the king we want to find
     * @return                  the position of the king (or null if not found)
     */
    private Piece findKing(Piece[][] board, boolean col) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null && board[i][j].getSymbol() == 'K' && board[i][j].getColor() == col) {
                    return board[i][j];
                }
            }
        }
        String colour = col ? "white" : "black";
        System.out.println(colour + " king not found on board");
        return null;
    }

    /**
     * Function to filter out values from one mask using a filter mask, so that the updated original mask should only contain true
     * values at coordinates where the original mask has a true value, and the filter mask has a false value
     * @param original          the mask to be reduced
     * @param filter            the mask used to remove squares from the original
     */
    private void filterMask(boolean[][] original, boolean[][] filter) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (original[i][j] && filter[i][j]) {
                    original[i][j] = false;
                }
            }
        }
    }

    /**
     * Function to remove the pieces from the board which are in the squares marked 'true' in the given pinned mask
     * @param board             the current board state, including pieces which are pinned
     * @param pinnedMask        the 2D boolean array which marks the positions of pinned pieces
     */
    private void removePinnedPieces(Piece[][] board, boolean[][] pinnedMask) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (pinnedMask[i][j]) {
                    board[i][j] = null;
                }
            }
        }
    }

    /**
     * Function to provide a deep copy of the board
     * @param board             the board state to be copied
     * @return                  the new copy of the board
     */
    public Piece[][] deepCopyBoard(Piece[][] board) {
        Piece[][] copy = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null) {
                    copy[i][j] = board[i][j].clonePiece();
                }
            }
        }
        return copy;
    }

}
