import java.util.ArrayList;
import java.util.List;

public class Node {

    String type; // "function" or "terminal"
    String valArity; // The type of function
    List<Node> children; // Children of the node
    Double constVal; // The constant value

    // Constructor
    public Node(String t, String val, List<Node> kids, Double c) {
        this.type = t;
        this.valArity = val;
        this.constVal = c;

        // Avoid null pointer problems
        if (kids != null) {
            this.children = kids;
        } else {
            this.children = new ArrayList<>();
        }
    }

    public Node() {
        this("", "", new ArrayList<>(), 0.0);
    }

    // Convenience constructor (no children)
    public Node(String t, String val) {
        this(t, val, new ArrayList<>(), 0.0);
    }

    // Convenience constructor (no children, with const val)
    public Node(String t, String val, Double c) {
        this(t, val, new ArrayList<>(), c);
    }

    // Convenience constructor (no const val)
    public Node(String t, String val, List<Node> kids) {
        this(t, val, kids, 0.0);
    }

    // Deep copy constructor
    public Node(Node n) {
        this.type = n.type;
        this.valArity = n.valArity;
        this.constVal = n.constVal;

        this.children = new ArrayList<>();
        for (Node child : n.children) {
            this.children.add(child.copy()); // deep copy
        }
    }

    public Node copy() {
        return new Node(this);
    }

    // Count total nodes in this subtree
    public int size() {
        int total = 1;

        for (Node child : children) {
            if (child != null) {
                total += child.size();
            }
        }

        return total;
    }

    // Depth of this subtree
    public int depth() {
        if (children.isEmpty()) {
            return 1;
        }

        int maxDepth = 0;
        for (Node child : children) {
            maxDepth = Math.max(maxDepth, child.depth());
        }

        return 1 + maxDepth;
    }

    public String toString() {

        if (this.getType().equals("term")) {

            if (this.getVal().equals("CONST")) {
                return String.format("%.2f", constVal);
            }

            return this.getVal(); // e.g. LOAD_1 or X
        }
        // Recursively convert children
        StringBuilder args = new StringBuilder();

        for (Node child : children) {
            args.append(child.toString()).append(" ");
        }

        return "(" + this.getVal() + " " + args.toString().trim() + ")";
    }

    public void setChildren(int index, Node prog) {
        this.children.set(index, prog);
    }

    // Optional helper methods
    public boolean isTerminal() {
        return children.isEmpty();
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public List<Node> getChildren() {
        return children;
    }

    public String getVal() {
        return valArity;
    }

    public double getConstVal() {
        return constVal;
    }

    public String getType() {
        return type;
    }
}