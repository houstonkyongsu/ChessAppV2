public class Move {

    private Pair start;
    private Pair end;
    private boolean col;
    private Pair enPassant;

    public Move(Pair start, Pair end, boolean col) {
        this.start = start;
        this.end = end;
        this.col = col;
        enPassant = null;
    }

    public Pair getStart() { return start; }

    public Pair getEnd() {return end; }

    public Pair getEnPassant() { return enPassant; }

    public void setEnPassant(Pair enPassant) { this.enPassant = enPassant; }
}
