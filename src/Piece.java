public class Piece {

    final int BOARD_SIZE = 8;
    private int x;
    private int y;
    private boolean color;
    private char symbol;
    private int moves;

    public Piece(int x, int y, boolean color, char symbol) {
        this.setX(x);
        this.setY(y);
        this.color = color;
        this.symbol = symbol;
        moves = 0;
    }

    public Piece clonePiece() {
        Piece p = new Piece(getX(), getY(), getColor(), getSymbol());
        p.setMoves(getMoves());
        return p;
    }

    public int getX() { return x; }

    public void setX(int x) { this.x = x; }

    public int getY() { return y; }

    public void setY(int y) { this.y = y; }

    public boolean getColor() { return color; }

    public char getSymbol() { return symbol; }

    public int getMoves() { return moves; }

    public void setMoves(int moves) { this.moves = moves;}
}
