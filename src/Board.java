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

    public void generateKingMoves(Piece[][] board, boolean col) {
        boolean[][] attackedSquareMask = new boolean[BOARD_SIZE][BOARD_SIZE];

        Pair kingLocation = findKing(board, col);
        if (kingLocation != null) {

        } else {
            System.out.println("king moves not able to be generated");
        }

        boolean[][] captureMask = new boolean[BOARD_SIZE][BOARD_SIZE];
        boolean[][] blockMask = new boolean[BOARD_SIZE][BOARD_SIZE];

    }

    /**
     * Function to return a 2D boolean array showing all the squares that a given piece on the board attacks
     * @param board             the current board state
     * @param mask              the mask representing attacked squares
     * @param pair              the position of the piece we are calculating attacking squares for
     * @param includeOwn        boolean indicating whether pieces of same colour included in the mask
     * @return                  the updated mask representing the attacked squares
     */
    public boolean[][] getBitMaskAttack(Piece[][] board, boolean[][] mask, Pair pair, boolean includeOwn) {
        Piece piece = board[pair.getX()][pair.getY()];
        switch (piece.getSymbol()) {
            case 'P':
                mask = bitMaskPawnAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), includeOwn);
                break;
            case 'R':
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 0, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, 1, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 0, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, -1, piece.getColor(), includeOwn);
                break;
            case 'B':
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 1, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, -1, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 1, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, -1, piece.getColor(), includeOwn);
                break;
            case 'N':
                mask = bitMaskKnightAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), includeOwn);
                break;
            case 'Q':
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 1, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, -1, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 1, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, -1, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 0, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, 1, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 0, piece.getColor(), includeOwn);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, -1, piece.getColor(), includeOwn);
                break;
            case 'K':
                mask = bitMaskKingAttack(board, mask, piece.getX(), piece.getY(), piece.getColor(), includeOwn);
                break;
            default:
                System.out.println("invalid piece symbol, cant calculate attack mask");
        }
        return mask;
    }

    /**
     * Function to mark bits in the 2D mask array to represent attacked squares, based on the following params:
     * @param board         the current board state
     * @param mask          the mask which is updated with additional attacked squares
     * @param x             the starting x coordinate
     * @param y             the starting y coordinate
     * @param addx          the increment to add to x
     * @param addy          the increment to add to y
     * @param col           the colour of the moving piece
     * @param includeOwn    boolean to mark if pieces of the same colour should be included in the mask
     * @return              the updated mask containing all attacked squares so far calculated
     */
    private boolean[][] bitMaskDirectional(Piece[][] board, boolean[][] mask, int x, int y, int addx, int addy, boolean col, boolean includeOwn) {
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
        return mask;
    }

    /**
     * Function to return a bit mask updated with the squares attacked by a pawn in the given position on the board
     * @param board             the current board state
     * @param mask              the 2D array boolean mask to represent attacked squares
     * @param x                 the x coordinate of the pawn
     * @param y                 the y coordinate of the pawn
     * @param col               the colour of the piece
     * @param includeOwn        boolean indicating whether pieces of same colour should be included in mask
     * @return                  the updated boolean mask
     */
    private boolean[][] bitMaskPawnAttack(Piece[][] board, boolean[][] mask, int x, int y, boolean col, boolean includeOwn) {
        int vert = col ? -1 : 1;
        x += vert;
        if (withinBounds(x, y + 1) && (board[x][y + 1] == null || board[x][y + 1].getColor() != col || includeOwn)) {
            mask[x][y + 1] = true;
        }
        if (withinBounds(x, y - 1) && (board[x][y - 1] == null || board[x][y - 1].getColor() != col || includeOwn)) {
            mask[x][y - 1] = true;
        }
        return mask;
    }

    /**
     * Function to return a bit mask updated with the squares attacked by the knight at the given position
     * @param board             the current board state
     * @param mask              the 2D boolean array representing attacked squares
     * @param x                 the x coordinate of the knight
     * @param y                 the y coordinate of the knight
     * @param col               the colour of the knight
     * @param includeOwn        boolean to indicate if pieces of the same colour should be included in the mask
     * @return                  the updated mask of attacked squares
     */
    private boolean[][] bitMaskKnightAttack(Piece[][] board, boolean[][] mask, int x, int y, boolean col, boolean includeOwn) {
        for (int i = -2; i <= 2; i+=4) {
            for (int j = -1; j <= 1; j+=2) {
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
        return mask;
    }

    /**
     * Function to return a bit mask updated with the squares attacked by the king at the given position
     * @param board             the current board state
     * @param mask              the 2D boolean array representing attacked squares
     * @param x                 the x coordinate of the king
     * @param y                 the y coordinate of the king
     * @param col               the colour of the king
     * @param includeOwn        boolean to indicate if pieces of the same colour should be included in the mask
     * @return                  the updated mask of attacked squares
     */
    private boolean[][] bitMaskKingAttack(Piece[][] board, boolean[][] mask, int x, int y, boolean col, boolean includeOwn) {
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
        return mask;
    }

    /**
     * Function to check that the provided coordinates are within the bounds of the chess board
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @return          boolean value if within bounds
     */
    private boolean withinBounds(int x, int y) {
        if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
            return true;
        }
        return false;
    }

    /**
     * Function to return the board coordinates of the king of the given color, and null if not found (which should
     * not happen if everything is working properly)
     * @param board             the current board state
     * @param col               the colour of the king we want to find
     * @return                  the position of the king (or null if not found)
     */
    private Pair findKing(Piece[][] board, boolean col) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null && board[i][j].getSymbol() == 'K' && board[i][j].getColor() == col) {
                    return new Pair(i, j);
                }
            }
        }
        String colour = col ? "white" : "black";
        System.out.println(colour + " king not found on board");
        return null;
    }

}
