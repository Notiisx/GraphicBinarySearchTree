package GraphicBinarySearchTree;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
//This Class is made for stablish the items we need to draw the tree inside the JPanel.
public class TheWindow extends JPanel implements ActionListener {

    static JFrame frame;
    static JOptionPane message = new JOptionPane();
    // the binary tree
    private BinaryTree tree = null;
    // the node location of the tree
    private HashMap nodeLocations = null;
    // the sizes of the subtrees
    private HashMap subtreeSizes = null;
    // do we need to calculate locations?
    private boolean dirty = true;
    // Default space between nodes
    private int parent2child = 10, child2child = 20;
    // helpers
    private Dimension empty = new Dimension(0, 0);
    private FontMetrics fm = null;

    // UI components
    private JTextField inputField;
    private JButton addButton, deleteButton, searchButton;

    /*When a button is pressed from the menu like "A F S D H" will make a different option
    a= add, f= add from file, s= search, d=Delete, H = Help.*/
    public TheWindow(BinaryTree tree){
        this.tree = tree;
        nodeLocations = new HashMap();
        subtreeSizes = new HashMap();
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        inputField = new JTextField(10);
        addButton = new JButton("Add");
        deleteButton = new JButton("Delete");
        searchButton = new JButton("Search");

        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        searchButton.addActionListener(this);

        controlPanel.add(new JLabel("Enter number:"));
        controlPanel.add(inputField);
        controlPanel.add(addButton);
        controlPanel.add(deleteButton);
        controlPanel.add(searchButton);

        add(controlPanel, BorderLayout.NORTH);

        registerKeyboardAction(this, "addFrom", KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(this, "help", KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), WHEN_IN_FOCUSED_WINDOW);
    }

    /* event handler for pressing a button from the menu, this will also show
    messageboxes and will make different things,depending of the option selected*/
    public void actionPerformed(ActionEvent e) {
        String input = inputField.getText();
        int a;
        if (e.getSource() == addButton) {
            try {
                a = Integer.parseInt(input);
                tree.addNode(a);
                dirty = true;
                repaint();
            } catch (NumberFormatException z) {
                JOptionPane.showMessageDialog(frame, "Please, write an integer number");
            }
        } else if (e.getSource() == deleteButton) {
            try {
                a = Integer.parseInt(input);
                tree.deleteNode(a, tree.getRoot());
                dirty = true;
                repaint();
            } catch (NumberFormatException z) {
                JOptionPane.showMessageDialog(frame, "Please, write an integer number");
            }
        } else if (e.getSource() == searchButton) {
            try {
                a = Integer.parseInt(input);
                Node aux = tree.searchNode(a, tree.getRoot());
                if (aux == null)
                    JOptionPane.showMessageDialog(frame, "The number " + a + " was not found");
                else
                    JOptionPane.showMessageDialog(frame, "The number " + a + " was found");
                dirty = true;
                repaint();
            } catch (NumberFormatException z) {
                JOptionPane.showMessageDialog(frame, "Please, write an integer number");
            }
        } else if (e.getActionCommand().equals("addFrom")) {
            // This help us for reading and storage of data
            BufferedReader reader = null;
            try {
                File file = new File("C:\\Users\\Raúl\\Desktop\\numeros.txt");
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    input = line;
                    tree.addNode(Integer.parseInt(input));
                    dirty = true;
                    repaint();
                }
            } catch (IOException z) {
                z.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException z) {
                    z.printStackTrace();
                }
            }
        } else if (e.getActionCommand().equals("help")) {
            JOptionPane.showMessageDialog(frame, "The operations you can use are:"
                    + "\n a --- Add an integer number"
                    + "\n f --- Add from file"
                    + "\n s --- Search an integer number"
                    + "\n d --- Delete an integer number"
                    + "\n h --- Help (if you forgot this)");
        }
    }

    // This method calculates the node locations, to make it look stablish
    private void calculateLocations() {
        nodeLocations.clear();
        subtreeSizes.clear();
        Node root = tree.getRoot();
        if (root != null) {
            calculateSubtreeSize(root);
            calculateLocation(root, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        }
    }

    // This method calculates the size of a subtree rooted at n
    private Dimension calculateSubtreeSize(Node n) {
        if (n == null) return new Dimension(0, 0);
        String s = Integer.toString(n.getContent());
        Dimension ld = calculateSubtreeSize(n.getLeft());
        Dimension rd = calculateSubtreeSize(n.getRight());
        int h = fm.getHeight() + parent2child + Math.max(ld.height, rd.height);
        int w = ld.width + child2child + rd.width;
        Dimension d = new Dimension(w, h);
        subtreeSizes.put(n, d);
        return d;
    }

    // This method calculates the location of the nodes in the subtree rooted at n
    private void calculateLocation(Node n, int left, int right, int top) {
        if (n == null) return;
        Dimension ld = (Dimension) subtreeSizes.get(n.getLeft());
        if (ld == null) ld = empty;
        Dimension rd = (Dimension) subtreeSizes.get(n.getRight());
        if (rd == null) rd = empty;
        int center = 0;
        if (right != Integer.MAX_VALUE)
            center = right - rd.width - child2child/2;
        else if (left != Integer.MAX_VALUE)
            center = left + ld.width + child2child/2;
        int width = fm.stringWidth(Integer.toString(n.getContent()));
        Rectangle r = new Rectangle(center - width/2 - 3, top, width + 6, fm.getHeight());
        nodeLocations.put(n, r);
        calculateLocation(n.getLeft(), Integer.MAX_VALUE, center - child2child/2, top + fm.getHeight() + parent2child);
        calculateLocation(n.getRight(), center + child2child/2, Integer.MAX_VALUE, top + fm.getHeight() + parent2child);
    }

    // This method draws the tree using the pre-calculated locations. We need necessary a graphic
    private void drawTree(Graphics2D g, Node n, int px, int py, int yoffs) {
        if (n == null) return;
        Rectangle r = (Rectangle) nodeLocations.get(n);
        int radius = Math.max(r.width, r.height);
        int centerX = r.x + r.width / 2;
        int centerY = r.y + r.height / 2;
        g.drawOval(centerX - radius / 2, centerY - radius / 2, radius, radius);
        g.drawString(Integer.toString(n.getContent()), centerX - fm.stringWidth(Integer.toString(n.getContent())) / 2, centerY + fm.getAscent() / 2 - 2);
        if (px != Integer.MAX_VALUE)
            g.drawLine(px, py, centerX, centerY - radius / 2);
        drawTree(g, n.getLeft(), centerX, centerY + radius / 2, yoffs);
        drawTree(g, n.getRight(), centerX, centerY + radius / 2, yoffs);
    }

    // This method will draw our tree, this receives a graphic called "g"
    public void paint(Graphics g) {
        super.paint(g);
        setBackground(Color.CYAN);
        fm = g.getFontMetrics();
        // if node locations not calculated
        if (dirty) {
            calculateLocations();
            dirty = false;
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(getWidth() / 2, parent2child + 50); // Adjust the translation to move the tree down
        drawTree(g2d, tree.getRoot(), Integer.MAX_VALUE, Integer.MAX_VALUE, fm.getLeading() + fm.getAscent());
        fm = null;
    }

    /*At the start of the program will show a messagebox with all the commands that
     can be used to work this program correctly,also set the dimension of the principal
     window */
    public static void main(String[] args) {
        // TODO code application logic here
        BinaryTree tree = new BinaryTree();
        JFrame f = new JFrame("Binary Tree");
        f.getContentPane().add(new TheWindow(tree));
        // create and add an event handler for window closing event
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setBounds(50, 50, 700, 700);
        f.show();
    }
}