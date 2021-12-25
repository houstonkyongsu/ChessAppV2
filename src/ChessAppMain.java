import javax.swing.border.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.image.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChessAppMain extends JPanel implements ActionListener {

    final int BOARD_SIZE = 8;
    private JFrame frame;
    private JPanel ui;
    private JPanel board;
    private static JButton[][] gridSquares;
    private HashMap<String, Image> iconmap;
    private Pair first;

    private static final long serialVersionUID = 1L;

    private Gamelogic logic;

    public ChessAppMain(int p1, int p2) {
        frame = new JFrame("ChameOfGuess");
        gridSquares = new JButton[BOARD_SIZE][BOARD_SIZE];
        iconmap = new HashMap<>();
        first = new Pair(-1, -1);
        loadGUI();
        loadIconMap();
        Thread thread = new Thread() {
            public void run() {
                runGame();
            }
        };
        thread.start();
    }

    private void loadGUI() {
        loadBoardGUI();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(ui);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    private void loadIconMap() {
        try {
            Image img;
            img = ImageIO.read(getClass().getResource("/bk.png"));
            iconmap.put("BK", img);
            img = ImageIO.read(getClass().getResource("/wk.png"));
            iconmap.put("WK", img);
            img = ImageIO.read(getClass().getResource("/bq.png"));
            iconmap.put("BQ", img);
            img = ImageIO.read(getClass().getResource("/wq.png"));
            iconmap.put("WQ", img);
            img = ImageIO.read(getClass().getResource("/br.png"));
            iconmap.put("BR", img);
            img = ImageIO.read(getClass().getResource("/wr.png"));
            iconmap.put("WR", img);
            img = ImageIO.read(getClass().getResource("/bb.png"));
            iconmap.put("BB", img);
            img = ImageIO.read(getClass().getResource("/wb.png"));
            iconmap.put("WB", img);
            img = ImageIO.read(getClass().getResource("/bp.png"));
            iconmap.put("BP", img);
            img = ImageIO.read(getClass().getResource("/wp.png"));
            iconmap.put("WP", img);
            img = ImageIO.read(getClass().getResource("/bn.png"));
            iconmap.put("BN", img);
            img = ImageIO.read(getClass().getResource("/wn.png"));
            iconmap.put("WN", img);
        } catch (IOException e) {
            System.out.println("IOException caught! " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("Illegal argument exception caught! " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *  This function is mostly taken from this stackoverflow post:
     *  https://stackoverflow.com/questions/21142686/making-a-robust-resizable-swing-chess-gui
     */
    private void loadBoardGUI() {
        board = new JPanel(new GridLayout(0, 8)) {

            private static final long serialVersionUID = 1L;

            /**
             * Override the preferred size to return the largest it can, in
             * a square shape.  Must (must, must) be added to a GridBagLayout
             * as the only component (it uses the parent as a guide to size)
             * with no GridBagConstaint (so it is centered).
             */
            @Override
            public final Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                Dimension prefSize = null;
                Component c = getParent();
                if (c == null) {
                    prefSize = new Dimension(
                            (int)d.getWidth(),(int)d.getHeight());
                } else if (c!=null &&
                        c.getWidth()>d.getWidth() &&
                        c.getHeight()>d.getHeight()) {
                    prefSize = c.getSize();
                } else {
                    prefSize = d;
                }
                int w = (int) prefSize.getWidth();
                int h = (int) prefSize.getHeight();
                int s = (w>h ? h : w);
                return new Dimension(s,s);
            }
        };
        board.setBorder(new CompoundBorder(new EmptyBorder(8,8,8,8), new LineBorder(Color.BLACK)));

        Insets buttonMargin = new Insets(0, 0, 0, 0);
        for (int i = 0; i < gridSquares.length; i++) {
            for (int j = 0; j < gridSquares[0].length; j++) {
                JButton b = new JButton();
                b.setMargin(buttonMargin);
                ImageIcon icon = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
                b.setIcon(icon);
                b.putClientProperty("row", i);
                b.putClientProperty("col", j);
                b.addActionListener(this);
                if ((i + j) % 2 == 0) {
                    b.setBackground(Color.WHITE);
                } else {
                    b.setBackground(Color.GRAY);
                }
                gridSquares[j][i] = b;
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board.add(gridSquares[i][j]);
            }
        }
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        BasicArrowButton b1 = new BasicArrowButton(BasicArrowButton.WEST);
        b1.setName("left");
        BasicArrowButton b2 = new BasicArrowButton(BasicArrowButton.EAST);
        b2.setName("right");
        b1.addActionListener(this);
        b2.addActionListener(this);

        panel.add(b1);
        panel.add(b2);
        ui = new JPanel();
        ui.setLayout(new BoxLayout(ui, BoxLayout.PAGE_AXIS));
        ui.add(board);
        ui.add(panel);

    }

    /**
     *  Function to update the graphics based on the current board state
     */
    public void updateGraphics() {
        Piece[][] brd = logic.getBoard();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (brd[i][j] != null) {
                    String key = "";
                    if (brd[i][j].getColor()) {
                        key += "W";
                    } else {
                        key += "B";
                    }
                    key += brd[i][j].getSymbol();
                    Image temp = iconmap.get(key);
                    if (temp != null) {
                        Image img = temp.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH);
                        gridSquares[i][j].setIcon(new ImageIcon(img));
                    } else {
                        System.out.println("key '" + key + "' not in hashmap");
                    }
                } else {
                    ImageIcon icon = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
                    gridSquares[i][j].setIcon(icon);
                }
            }
        }
    }

    private void runGame() {
        logic = new Gamelogic();
        logic.setupGame();

        try {

            while (!logic.isGameOver()) {

                updateGraphics();

                while (logic.isPlayerTurn()) {

                    TimeUnit.MILLISECONDS.sleep(1);
                }

                logic.setPlayerTurn(true);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new ChessAppMain(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                }
            });
        } else {
            System.out.println("Usage: Chame <> <>");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton b = (JButton) e.getSource();
        if (b != null && logic.isPlayerTurn() && b.getName() == null) {
            int i = (int) b.getClientProperty("row");
            int j = (int) b.getClientProperty("col");
            if (first.getX() == -1) {
                System.out.println("first: " + j + "," + i);
                first.setX(j);
                first.setY(i);
            } else {
                System.out.println("second: " + j + "," + i);
                //if (logic.tryMove(first.getX(), first.getY(), j, i)) {
                //    logic.makeGameMove(first.getX(), first.getY(), j, i, true);
                //}
                first.setX(-1);
                first.setY(-1);
            }
        } else if (b != null && b.getName() != null && logic.isGameOver()) {
            if (b.getName().equals("left")) {
                System.out.println("left");
            } else {
                System.out.println("right");
            }
        }
    }

}
