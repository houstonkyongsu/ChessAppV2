public class FEN {

    final int BOARD_SIZE = 8;

    public FEN() {}

    public String serialize(Piece[][] board) {
        StringBuilder fen = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE; i++) {
            int k = 0;
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == null) {
                    k++;
                } else {
                    if (k > 0) {
                        fen.append(String.valueOf(k));
                        k = 0;
                    }
                    if (board[i][j].getColor()) {
                        fen.append(board[i][j].getSymbol());
                    } else {
                        fen.append(Character.toLowerCase(board[i][j].getSymbol()));
                    }
                }
            }
            if (k > 0) {
                fen.append(String.valueOf(k));
            }
            if (i < BOARD_SIZE-1) {
                fen.append('/');
            }
        }
        fen.append(' ');

        return fen.toString();
    }

    public Piece[][] deserialize(String fen) {
        Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];

        return board;
    }
}
