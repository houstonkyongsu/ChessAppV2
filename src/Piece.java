public class Piece {

    final int BOARD_SIZE = 8;
    private int x;
    private int y;
    private boolean color;
    private char symbol;
    private int numMoves;
    private boolean isPinned;
    private boolean[][] moveMask;

    public Piece(int x, int y, boolean color, char symbol) {
        this.setX(x);
        this.setY(y);
        this.color = color;
        this.symbol = symbol;
        moveMask = new boolean[BOARD_SIZE][BOARD_SIZE];
        isPinned = false;
        numMoves = 0;
    }

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

    public int getNumMoves() { return numMoves; }

    public void setNumMoves(int moves) { this.numMoves = moves; }

    public boolean getPinned() { return isPinned; }

    public void setPinned(boolean isPinned) { this.isPinned = isPinned; }

    public boolean[][] getMoveMask() { return moveMask; }

    public void setMoveMask(boolean[][] moveMask) { this.moveMask = moveMask; }

    public void resetMask() { moveMask = new boolean[BOARD_SIZE][BOARD_SIZE]; }
}
