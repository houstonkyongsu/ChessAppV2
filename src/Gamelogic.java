import java.util.ArrayList;
import java.util.List;

public class Gamelogic {

    final int BOARD_SIZE = 8;
    private boolean colour;
    private int numMoves = 0;
    private boolean gameOver;
    private BoardUtils boardUtils;
    private Piece[][] board;
    private MoveGeneration moveGeneration;
    private ArrayList<Piece> moveablePieces;
    private ArrayList<Move> moveHistory;

    public Gamelogic() {
        boardUtils = new BoardUtils();
        gameOver = false;
        colour = true;
        board = new Piece[BOARD_SIZE][BOARD_SIZE];
        moveGeneration = new MoveGeneration();
        moveablePieces = new ArrayList<>();
        moveHistory = new ArrayList<>();
    }

    public void setupGame() {
        board = boardUtils.setupBoard();
        colour = true;
    }

    public boolean isGameOver() { return gameOver; }

    public Piece[][] getBoard() { return board; }

    public void setBoard(Piece[][] board) { this.board = board; }

    public boolean getColour() { return colour; }

    public void setColour(boolean colour) { this.colour = colour; }

    public List<Piece> getMoveablePieces() { return moveablePieces; }

    public void makeGameMove(Move move) {
        
    }

    public void updateAvailableMoves() {
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
