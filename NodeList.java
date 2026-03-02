public class NodeList {
    Node node;
    Node parent;
    Integer index; // null if root

    public NodeList(Node node, Node parent, Integer index) {
        this.node = node;
        this.parent = parent;
        this.index = index;
    }

}
