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
     *  Function to set up the pieces on the board
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
        board[0][1] = new Piece(0, 1, false, 'H');
        board[0][6] = new Piece(0, 6, false, 'H');
        board[7][6] = new Piece(7, 6, true, 'H');
        board[7][1] = new Piece(7, 1, true, 'H');
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

}
