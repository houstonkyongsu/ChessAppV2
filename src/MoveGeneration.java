import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoveGeneration {

    final int BOARD_SIZE = 8;
    BoardUtils boardUtils;

    public MoveGeneration() { boardUtils = new BoardUtils(); }

    /**
     * Function to generate all available moves for a given colour and board state, and then save the available moves in that pieces move list.
     * @param board     the current board state
     * @param col       the colour to find moves for
     * @return
     */
    public boolean generateMoves(Piece[][] board, boolean col) {
        Piece king = findKing(board, col);
        if (king == null) {
            System.out.println("king moves not able to be generated");
            return false;
        }
        Piece[][] boardMinusKing = boardUtils.deepCopyBoard(board);
        boardMinusKing[king.getX()][king.getY()] = null;

        boolean[][] attackedSquareMask = allPiecesAttackedSquareMask(boardMinusKing, !col);
        boolean[][] tempKingMoves = new boolean[BOARD_SIZE][BOARD_SIZE];
        boolean[][] captureBlockMask = new boolean[BOARD_SIZE][BOARD_SIZE];
        Pair[][] pinnedMask = new Pair[BOARD_SIZE][BOARD_SIZE];

        bitMaskKingAttack(board, tempKingMoves, king.getX(), king.getY(), col, false);
        filterXAndNotY(tempKingMoves, attackedSquareMask);
        bitMaskKingCastle(board, tempKingMoves, king.getX(), king.getY(), col, attackedSquareMask);
        king.setMoveMask(tempKingMoves);
        king.setMoveListFromMask(tempKingMoves);

        if (attackedSquareMask[king.getX()][king.getY()]) {
            Stream.Builder<Pair> streamPieces = Stream.builder();
            streamPieces.accept(kingCheckedDirectionalUpdate(board, pinnedMask, king.getX(), king.getY(), col));
            streamPieces.accept(kingCheckedKnight(board, king.getX(), king.getY(), col));
            streamPieces.accept(kingCheckedPawn(board, king.getX(), king.getY(), col));

            ArrayList<Pair> checkingPieces = streamPieces.build()
                    .filter(x -> (x.getX() != -1 && x.getY() != -1))
                    .collect(Collectors.toCollection(ArrayList::new));

            System.out.println("Checking pieces: " + checkingPieces.size());
            if (checkingPieces.size() == 2) {
                // king is in double check, only king moves are available, so no other calculation needed
                if (king.getMoveList().isEmpty()) {
                    String winningCol = !col ? "white" : "black";
                    System.out.println("Checkmate, " + winningCol + " wins!");
                    return false;
                }
            } else if (checkingPieces.size() == 1) {
                // king is in single check, can move king, capture attacking piece, or block if it's a sliding piece
                Pair attacker = checkingPieces.getFirst();
                captureBlockMask[attacker.getX()][attacker.getY()] = true;
                if (board[attacker.getX()][attacker.getY()].getSymbol() == 'Q'
                        || board[attacker.getX()][attacker.getY()].getSymbol() == 'R'
                        || board[attacker.getX()][attacker.getY()].getSymbol() == 'B') {
                    updateCaptureBlockMask(captureBlockMask, attacker, new Pair(king.getX(), king.getY()));
                }
                // maybe use boardminusking instead of the normal board, as king moves already calculated?
                //Piece[][] boardCopy = boardUtils.deepCopyBoard(board);
                //removePinnedPieces(boardCopy, pinnedMask);
                allPiecesFindMoves(board, captureBlockMask, pinnedMask, col, true);
            } else {
                System.out.println("you shouldnt be here, in check but no checking pieces found");
            }
        } else {
            // king is not in check, can proceed with normal move generation
            // maybe use boardminusking instead of the normal board, as king moves already calculated?
            //Piece[][] boardCopy = boardUtils.deepCopyBoard(board);
            kingCheckedDirectionalUpdate(board, pinnedMask, king.getX(), king.getY(), col);
            allPiecesFindMoves(board, captureBlockMask, pinnedMask, col, false);
        }
        return true;
    }

    /**
     * Function to iterate through all the pieces on the input board param and calculate the available moves for them, saving the
     * calculated bit mask to each piece object. Additional input parameters are given to help determine what moves are available
     * to each of the pieces, and to help filter through any unnecessary calculation.
     * @param board             the current board state
     * @param captureBlockMask  a 2D boolean array mask showing the positions pieces can go to either block or take the attacker
     * @param col               the colour indicating who's turn it is
     * @param kingChecked       a boolean value to represent if the king is in check or not
     */
    private void allPiecesFindMoves(Piece[][] board, boolean[][] captureBlockMask, Pair[][] pinnedMask, boolean col, boolean kingChecked) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null && board[i][j].getColor() == col && board[i][j].getSymbol() != 'K') {
                    // Knights cannot move at all if they are pinned, so skip move generation
                    if (pinnedMask[i][j] != null && board[i][j].getSymbol() == 'N') {
                        board[i][j].setMoveMask(new boolean[BOARD_SIZE][BOARD_SIZE]);
                        continue;
                    }
                    boolean[][] moveMask = getBitMaskMove(board, board[i][j], pinnedMask, kingChecked);
                    if (kingChecked) {
                        // filter out any moves which are not capturing or blocking the checking piece
                        filterXAndY(moveMask, captureBlockMask);
                    }
                    board[i][j].setMoveListFromMask(moveMask);
                    board[i][j].setMoveMask(moveMask);
                }
            }
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
                    attackedSquareMask = getBitMaskAttack(board, attackedSquareMask, board[i][j]);
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
     * @param pinnedMask        the 2D Pair array to mark positions of pinned piece, and the vector of their attacker from the king
     * @param x                 the x coordinate of the king
     * @param y                 the y coordinate of the king
     * @param col               the colour of the king
     * @return                  the coordinates of the piece which is putting the king in check, if no pieces are putting the king
     *                          in check then the returned coordinates will be (-1, -1)
     */
    private Pair kingCheckedDirectionalUpdate(Piece[][] board, Pair[][] pinnedMask, int x, int y, boolean col) {
        Pair res = new Pair(-1, -1);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int tempX = x;
                int tempY = y;
                Pair vector = new Pair(i, j);
                if (i == 0 && j == 0) {
                    continue;
                }
                Pair potentialPin = new Pair(-1, -1);
                while (true) {
                    tempX += i;
                    tempY += j;
                    if (!withinBounds(tempX, tempY)) {
                        break;
                    }
                    if (board[tempX][tempY] == null) {
                        continue;
                    }
                    if (board[tempX][tempY].getColor() == col) {
                        if (potentialPin.getX() != -1 && potentialPin.getY() != -1) {
                            break;
                        }
                        potentialPin.setX(tempX);
                        potentialPin.setY(tempY);
                    } else if (board[tempX][tempY].getSymbol() == 'Q' || (board[tempX][tempY].getSymbol() == 'B' && (i != 0 && j != 0))
                            || (board[tempX][tempY].getSymbol() == 'R' && (i == 0 || j == 0))) {
                        if (potentialPin.getX() != -1 && potentialPin.getY() != -1) {
                            pinnedMask[potentialPin.getX()][potentialPin.getY()] = vector;
                            board[potentialPin.getX()][potentialPin.getY()].setPinned(true);
                        } else {
                            res.setX(tempX);
                            res.setY(tempY);
                        }
                        break;
                    } else if (board[tempX][tempY].getColor() != col) {
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
     * Function to return a 2D boolean array showing all the squares that a single given piece on the board can move to
     * @param board             the current board state
     * @param piece             the piece for which the moves are being generated
     * @param pinnedMask        the 2D Pair array to indicate position of pinned pieces and the direction of their attacker
     * @param kingChecked       boolean which denotes if the king is in check or not
     * @return                  2D boolean array noting all the squares a piece can move to based on the piece's own movement rules
     */
    private boolean[][] getBitMaskMove(Piece[][] board, Piece piece, Pair[][] pinnedMask, boolean kingChecked) {
        boolean[][] mask = new boolean[BOARD_SIZE][BOARD_SIZE];
        Pair vector = pinnedMask[piece.getX()][piece.getY()];
        switch (piece.getSymbol()) {
            case 'P' -> {
                bitMaskPawnAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), false, vector);
                bitMaskPawnMove(board, mask, piece.getX(), piece.getY(), piece.getColor(), vector);
            }
            case 'R' -> {
                if (vector == null || (vector.getX() == 0 && vector.getY() != 0)) {
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, 1, piece.getColor(), false);
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, -1, piece.getColor(), false);
                }
                if (vector == null || (vector.getX() != 0 && vector.getY() == 0)) {
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 0, piece.getColor(), false);
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 0, piece.getColor(), false);
                }
            }
            case 'B' -> {
                if (vector == null || vector.getX() + vector.getY() == 0) {
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, -1, piece.getColor(), false);
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 1, piece.getColor(), false);
                }
                if (vector == null || vector.getX() + vector.getY() == 2 || vector.getX() + vector.getY() == -2) {
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 1, piece.getColor(), false);
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, -1, piece.getColor(), false);
                }
            }
            case 'N' -> {
                if (vector == null) {
                    bitMaskKnightAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), false);
                }
            }
            case 'Q' -> {
                if (vector == null || (vector.getX() == 0 && vector.getY() != 0)) {
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, 1, piece.getColor(), false);
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, -1, piece.getColor(), false);
                }
                if (vector == null || (vector.getX() != 0 && vector.getY() == 0)) {
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 0, piece.getColor(), false);
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 0, piece.getColor(), false);
                }
                if (vector == null || vector.getX() + vector.getY() == 0) {
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, -1, piece.getColor(), false);
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 1, piece.getColor(), false);
                }
                if (vector == null || (vector.getX() + vector.getY() != 0 && vector.getX() != 0 && vector.getY() != 0)) {
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 1, piece.getColor(), false);
                    bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, -1, piece.getColor(), false);
                }
            }
            case 'K' -> {
                // empty as king moves should be generated already
            }
            default -> System.out.println("invalid piece symbol, cant calculate move mask");
        }
        return mask;
    }

    /**
     * Function to return a 2D boolean array with all the squares that a given piece on the board attacks added to it
     * @param board             the current board state
     * @param mask              the mask representing attacked squares
     * @param piece             the piece we are calculating attacking squares for
     * @return                  the updated mask representing the attacked squares
     */
    private boolean[][] getBitMaskAttack(Piece[][] board, boolean[][] mask, Piece piece) {
        switch (piece.getSymbol()) {
            case 'P' -> bitMaskPawnAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), true, null);
            case 'R' -> {
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 0, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, 1, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 0, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, -1, piece.getColor(), true);
            }
            case 'B' -> {
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 1, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, -1, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 1, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, -1, piece.getColor(), true);
            }
            case 'N' -> bitMaskKnightAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), true);
            case 'Q' -> {
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 1, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, -1, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 1, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, -1, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 0, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, 1, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 0, piece.getColor(), true);
                bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, -1, piece.getColor(), true);
            }
            case 'K' -> bitMaskKingAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), true);
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
     * @param X                 the x coordinate of the pawn
     * @param y                 the y coordinate of the pawn
     * @param col               the colour of the piece
     * @param includeOwn        boolean indicating whether pieces of same colour should be included in mask
     * @param pinnedVector      vector pair to represent the direction of a pinning piece, to only movement along that vector is allowed if it's not null
     */
    private void bitMaskPawnAttack(Piece[][] board, boolean[][] mask, int X, int y, boolean col, boolean includeOwn, Pair pinnedVector) {
        int vert = col ? -1 : 1;
        int x = X + vert;
        for (int i = -1; i <=1; i += 2) {
            if (withinBounds(x, y + i) && board[x][y + i] != null && (board[x][y + i].getColor() != col || includeOwn)) {
                if (pinnedVector == null || (X + pinnedVector.getX() == x && i == pinnedVector.getY()) || (X - pinnedVector.getX() == x && i == -pinnedVector.getY())) {
                    mask[x][y + i] = true;
                }
            } else if (withinBounds(x - vert, y + i) && board[x - vert][y + i] != null && board[x - vert][y + i].getSymbol() == 'P'
                    && board[x - vert][y + i].getColor() != col && board[x - vert][y + i].getNumMoves() == 1) {
                if (pinnedVector == null || (X + pinnedVector.getX() == x && i == pinnedVector.getY()) || (X - pinnedVector.getX() == x && i == -pinnedVector.getY())) {
                    mask[x][y + i] = true;
                    board[x - vert][y + i].setEnPassant(true);
                }
            }
        }
    }

    /**
     * Function to get pawn moves which are not pawn attacks
     * @param board             the current board state
     * @param mask              the 2D boolean array representing possible move squares
     * @param x                 the x coordinate of the pawn
     * @param y                 the y coordinate of the pawn
     * @param col               the colour of the pawn
     * @param pinnedVector      vector pair to represent the direction of a pinning piece, to only movement along that vector is allowed if it's not null
     */
    private void bitMaskPawnMove(Piece[][] board, boolean[][] mask, int x, int y, boolean col, Pair pinnedVector) {
        int vert = col ? -1 : 1;
        int newx = x + vert;
        if (pinnedVector == null || pinnedVector.getY() == 0) {
            if (withinBounds(newx, y) && board[newx][y] == null) {
                mask[newx][y] = true;
            }
            newx += vert;
            if (board[x][y].getNumMoves() == 0 && withinBounds(newx, y) && board[newx][y] == null) {
                mask[newx][y] = true;
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

    private void bitMaskKingCastle(Piece[][] board, boolean[][] mask, int x, int y, boolean col, boolean[][] attackedMask) {
        if (!attackedMask[x][y]) {
            if (board[x][y].getNumMoves() == 0 && board[x][0] != null && board[x][0].getColor() == col
                    && board[x][0].getSymbol() == 'R' && board[x][0].getNumMoves() == 0) {
                if (board[x][1] == null && board[x][2] == null && board[x][3] == null && !attackedMask[x][2] && !attackedMask[x][3]) {
                    mask[x][y - 2] = true;
                }
            }
            if (board[x][y].getNumMoves() == 0 && board[x][7] != null && board[x][7].getColor() == col
                    && board[x][7].getSymbol() == 'R' && board[x][7].getNumMoves() == 0) {
                if (board[x][6] == null && board[x][5] == null && !attackedMask[x][6] && !attackedMask[x][5]) {
                    mask[x][y + 2] = true;
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
    private void filterXAndNotY(boolean[][] original, boolean[][] filter) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (original[i][j] && filter[i][j]) {
                    original[i][j] = false;
                }
            }
        }
    }

    /**
     * Function to filter out true values from the original mask in all places where there is a false value in the filter mask
     * i.e. perform logical 'and' operation on each board square between the two masks
     * @param original          the mask to be reduced
     * @param filter            the mask used to remove squares from the original
     */
    private void filterXAndY(boolean[][] original, boolean[][] filter) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                original[i][j] = original[i][j] && filter[i][j];
            }
        }
    }

    /**
     * Function to remove the pieces from the board which are in the squares marked 'true' in the given pinned mask
     * @param board             the current board state, including pieces which are pinned
     * @param pinnedMask        the 2D Pair array which marks the positions of pinned pieces
     */
    private void removePinnedPieces(Piece[][] board, Pair[][] pinnedMask) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (pinnedMask[i][j] != null) {
                    board[i][j] = null;
                }
            }
        }
    }

    /**
     * Function to return the total number of marked squares within a 2D boolean array
     * @param mask              the 2D boolean array input containing bits to be summed
     * @return                  the total number of set bits in the input mask
     */
    private int sumMaskBits(boolean[][] mask) {
        int total = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (mask[i][j]) {
                    total++;
                }
            }
        }
        return total;
    }
}
