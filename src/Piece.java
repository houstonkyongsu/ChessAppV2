import java.util.ArrayList;

public class Piece {

    final int BOARD_SIZE = 8;
    private int x;
    private int y;
    private boolean color;
    private char symbol;
    private int numMoves;
    private boolean isPinned;
    private boolean[][] moveMask;
    private boolean enPassant;
    private ArrayList<Pair> moveList;

    public Piece(int x, int y, boolean color, char symbol) {
        this.setX(x);
        this.setY(y);
        this.color = color;
        this.symbol = symbol;
        moveMask = new boolean[BOARD_SIZE][BOARD_SIZE];
        isPinned = false;
        numMoves = 0;
        enPassant = false;
        moveList = new ArrayList<>();
    }

    /**
     * Function to clone this piece object
     * @return          a new copy of this object with the same attribute values
     */
    public Piece clonePiece() {
        Piece p = new Piece(getX(), getY(), getColor(), getSymbol());
        p.setNumMoves(getNumMoves());
        return p;
    }

    public int getX() { return x; }

    public void setX(int x) { this.x = x; }

    public int getY() { return y; }

    public void setY(int y) { this.y = y; }

    public boolean getColor() { return color; }

    public char getSymbol() { return symbol; }

    public void setSymbol(char c) { symbol = c; }

    public int getNumMoves() { return numMoves; }

    public void setNumMoves(int numMoves) { this.numMoves = numMoves; }

    public void incrementNumMoves() { numMoves++; }

    public boolean getPinned() { return isPinned; }

    public void setPinned(boolean isPinned) { this.isPinned = isPinned; }

    public boolean[][] getMoveMask() { return moveMask; }

    public void setMoveMask(boolean[][] moveMask) { this.moveMask = moveMask; }

    public void resetMask() { moveMask = new boolean[BOARD_SIZE][BOARD_SIZE]; }

    public boolean getEnPassant() { return enPassant; }

    public void setEnPassant(boolean enPassant) { this.enPassant = enPassant; }

    public void clearMoves() {
        moveList = new ArrayList<>();
        resetMask();
    }

    public void setMoveListFromMask(boolean[][] moveMask) {
        moveList = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (moveMask[i][j]) {
                    moveList.add(new Pair(i, j));
                }
            }
        }
    }

    public ArrayList<Pair> getMoveList() { return moveList; }
}
