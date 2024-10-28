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

    public Gamelogic() {
        boardUtils = new BoardUtils();
        gameOver = false;
        colour = true;
        board = new Piece[BOARD_SIZE][BOARD_SIZE];
        moveGeneration = new MoveGeneration();
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

    public void processGameMove(Move move) {
        
    }

    public List<Piece> getMoveablePieces() {
        if (!moveGeneration.generateMoves(board, colour)) {
            gameOver = true;
            return new ArrayList<Piece>();
        }
        ArrayList<Piece> moveList = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null && !board[i][j].getMoveList().isEmpty()) {
                    moveList.add(board[i][j]);
                }
            }
        }
        if (moveList.isEmpty()) {
            gameOver = true;
        }
        return moveList;
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
