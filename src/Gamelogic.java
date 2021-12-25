public class Gamelogic {

    private boolean playerTurn;
    private Piece[][] board;
    final int BOARD_SIZE = 8;
    private int numMoves = 0;
    private boolean gameOver;

    public Gamelogic()
    {
        board = new Piece[BOARD_SIZE][BOARD_SIZE];
        playerTurn = true;
        gameOver = false;
    }

    public boolean isGameOver() { return gameOver; }

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

    public Piece[][] getBoard() { return board; }

    public boolean isPlayerTurn() { return playerTurn; }

    public void setPlayerTurn(boolean playerTurn) { this.playerTurn = playerTurn; }

    /**
     * Function to return a 2D boolean array showing all the squares that a given piece on the board attacks
     * @param board             the current board state
     * @param pair              the position of the piece we are calculating attacking squares for
     * @return                  the mask representing the attacked squares
     */
    public boolean[][] getBitMaskAttack(Piece[][] board, Pair pair) {
        boolean[][] mask = new boolean[BOARD_SIZE][BOARD_SIZE];
        Piece piece = board[pair.getX()][pair.getY()];
        switch (piece.getSymbol()) {
            case 'P':
                break;
            case 'R':
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 1, 0, piece.getColor(), false);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, 1, piece.getColor(), false);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), -1, 0, piece.getColor(), false);
                mask = bitMaskDirectional(board, mask, piece.getX(), piece.getY(), 0, -1, piece.getColor(), false);
                break;
            case 'B':
                break;
            case 'N':
                break;
            case 'Q':
                break;
            case 'K':
                break;
            default:
                System.out.println();
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
     * Function to provide a deep copy of the board
     * @param board         the board state to be copied
     * @return              the new copy of the board
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
