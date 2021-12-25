public class Gamelogic {

    final int BOARD_SIZE = 8;
    private boolean playerTurn;
    private int numMoves = 0;
    private boolean gameOver;
    private Board board;

    public Gamelogic() {
        board = new Board();
        playerTurn = true;
        gameOver = false;
    }

    public void setupGame() {
        board.setupBoard();
    }

    public boolean isGameOver() { return gameOver; }

    public Piece[][] getBoard() { return board.getBoard(); }

    public boolean isPlayerTurn() { return playerTurn; }

    public void setPlayerTurn(boolean playerTurn) { this.playerTurn = playerTurn; }


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
