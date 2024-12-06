import java.util.Objects;

public class FEN {

    final int BOARD_SIZE = 8;

    public FEN() {}

    public String serializeFEN(Piece[][] board, boolean col, int halfMove, int fullMove) {
        StringBuilder fen = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE; i++) {
            int k = 0;
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == null) {
                    k++;
                } else {
                    if (k > 0) {
                        fen.append(k);
                        k = 0;
                    }
                    if (board[i][j].getColor()) {
                        fen.append(Character.toUpperCase(board[i][j].getSymbol()));
                    } else {
                        fen.append(Character.toLowerCase(board[i][j].getSymbol()));
                    }
                }
            }
            if (k > 0) {
                fen.append(k);
            }
            if (i < BOARD_SIZE-1) {
                fen.append('/');
            }
        }
        String turn = col ? " w " : " b ";
        fen.append(turn);
        fen.append(getCastlingFENString(board));
        fen.append(' ');
        fen.append(getEnPassentFENSquare(board));
        fen.append(halfMove);
        fen.append(' ');
        fen.append(fullMove);

        return fen.toString();
    }

    public Piece[][] deserializeFENBoard(String fen) {
        Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];
        String[] fenArr = fen.split(" ");
        String boardDescription = fenArr[0];
        int i = 0, j = 0;
        for (int x = 0; x < boardDescription.length(); x++) {
            if (boardDescription.charAt(x) == '/') {
                i++;
                j = 0;
            } else if (Character.isDigit(boardDescription.charAt(x))) {
                j += Character.getNumericValue(boardDescription.charAt(x));
            } else {
                boolean col = Character.isUpperCase(boardDescription.charAt(x));
                board[i][j] = new Piece(i, j, col, Character.toUpperCase(boardDescription.charAt(x)));
            }
        }
        return board;
    }

    public boolean deserializeFENTurn(String fen) {
        String[] fenArr = fen.split(" ");
        return Objects.equals(fenArr[1], "w");
    }

    public void deserializeFENCastling(Piece[][] board, String fen) {
        String[] fenArr = fen.split(" ");
        for (char c : fenArr[2].toCharArray()) {
            
        }
    }

    private String getCastlingFENString(Piece[][] board) {
        StringBuilder castling = new StringBuilder();
        if (board[7][4] != null && board[7][4].getSymbol() == 'K' && board[7][4].getNumMoves() == 0 && board[7][4].getColor()) {
            if (board[7][7] != null && board[7][7].getSymbol() == 'R' && board[7][7].getNumMoves() == 0 && board[7][7].getColor()) {
                castling.append('K');
            }
            if (board[7][0] != null && board[7][0].getSymbol() == 'R' && board[7][0].getNumMoves() == 0 && board[7][0].getColor()) {
                castling.append('Q');
            }
        }
        if (board[0][4] != null && board[0][4].getSymbol() == 'K' && board[0][4].getNumMoves() == 0 && !board[0][4].getColor()) {
            if (board[0][7] != null && board[0][7].getSymbol() == 'R' && board[0][7].getNumMoves() == 0 && board[0][7].getColor()) {
                castling.append('k');
            }
            if (board[0][0] != null && board[0][0].getSymbol() == 'R' && board[0][0].getNumMoves() == 0 && board[0][0].getColor()) {
                castling.append('q');
            }
        }
        if (castling.isEmpty()) {
            castling.append('-');
        }
        return castling.toString();
    }

    private String getEnPassentFENSquare(Piece[][] board) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null && board[i][j].getLastMoved() && board[i][j].getSymbol() == 'P') {
                    if (board[i][j].getX() == 3 || board[i][j].getX() == 4) {
                        result.append(board[i][j].getX());
                        result.append(j);
                    }
                }
            }
        }
        if (result.isEmpty()) {
            result.append('-');
        }
        return result.toString();
    }
}
