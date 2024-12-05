import java.util.ArrayList;
import java.util.List;

public class Gamelogic {

    final int BOARD_SIZE = 8;
    private boolean colour;
    private int numMoves = 0;
    private boolean gameOver;
    private BoardUtils boardUtils;
    //private Piece[][] board;
    private MoveGeneration moveGeneration;
    private ArrayList<Piece> moveablePieces;
    private ArrayList<Move> moveHistory;

    public Gamelogic() {
        boardUtils = new BoardUtils();
        gameOver = false;
        colour = true;
        //board = new Piece[BOARD_SIZE][BOARD_SIZE];
        moveGeneration = new MoveGeneration();
        moveablePieces = new ArrayList<>();
        moveHistory = new ArrayList<>();
    }

    public Piece[][] setupGame() {
        Piece[][] board = boardUtils.setupBoard();
        colour = true;
        return board;
    }

    public boolean isGameOver() { return gameOver; }

    //public Piece[][] getBoard() { return board; }

    //public void setBoard(Piece[][] board) { this.board = board; }

    public boolean getColour() { return colour; }

    public void setColour(boolean colour) { this.colour = colour; }

    public List<Piece> getMoveablePieces() { return moveablePieces; }

    public Piece getPiece(Piece[][] board, int x, int y) { return board[x][y]; }

    public boolean checkValidPiece(Piece[][] board, int x, int y, boolean col) {
        return board[x][y] != null && board[x][y].getColor() == col;
    }

    public boolean checkPawnPromote(Piece piece, int x, boolean col) {
        int promoteRow = col ? 0 : 7;
        return piece.getSymbol() == 'P' && x == promoteRow;
    }

    public boolean moveInPieceMoveList(Piece piece, int x, int y) {
        for (Pair pair : piece.getMoveList()) {
            if (pair.getX() == x && pair.getY() == y) {
                return true;
            }
        }
        return false;
    }

    public List<Pair> getPieceMoveList(Piece[][] board, int x, int y) {
        List<Pair> moves = new ArrayList<>();
        if (board[x][y] != null) {
            moves = board[x][y].getMoveList();
        }
        return moves;
    }

    public void makeGameMove(Piece[][] board, Piece piece, int x, int y) {
        int oldX = piece.getX();
        int oldY = piece.getY();
        board[x][y] = piece;
        piece.setX(x);
        piece.setY(y);
        piece.incrementNumMoves();
        board[oldX][oldY] = null;
        if (piece.getSymbol() == 'K' && Math.abs(oldY - y) > 1) {
            int rY = oldY > y ? oldY - 1 : oldY + 1;
            int oldRY = oldY > y ? 0 : 7;
            if (board[x][oldRY] != null && board[x][oldRY].getSymbol() == 'R') {
                board[x][rY] = board[x][oldRY];
                board[x][rY].incrementNumMoves();
                board[x][rY].setY(rY);
                board[x][oldRY] = null;
            }
        } else if (checkEnPassentMove(piece, oldX, y)) {
            board[piece.getEnPassant().getX()][piece.getEnPassant().getY()] = null;
        }
        clearOldMoves(board);
        piece.setLastMoved(true);
    }

    public void clearOldMoves(Piece[][] board) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null) {
                    board[i][j].clearMoves();
                    board[i][j].setEnPassant(null);
                    board[i][j].setLastMoved(false);
                }
            }
        }
    }

    public void updateAvailableMoves(Piece[][] board) {
        if (!moveGeneration.generateMoves(board, colour)) {
            gameOver = true;
        }
        ArrayList<Piece> pieceList = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null && !board[i][j].getMoveList().isEmpty()) {
                    pieceList.add(board[i][j]);
                }
            }
        }
        if (pieceList.isEmpty()) {
            gameOver = true;
        }
        moveablePieces = pieceList;
    }

    private boolean checkEnPassentMove(Piece piece, int x, int y) {
        return piece.getSymbol() == 'P' && piece.getEnPassant() != null && x == piece.getEnPassant().getX() && y == piece.getEnPassant().getY();
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
