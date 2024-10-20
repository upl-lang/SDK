package upl.regex3.syntaxtree;

import upl.regex3.lang.OperatorsSet;
import upl.regex3.lang.SymbolSet;
import upl.regex3.regex.RegExp;

import java.util.*;

public class SyntaxTree {
    protected int leafNodeId = 0;
    protected OperatorsSet opSet = new OperatorsSet();
    protected SymbolSet symbolSet = new SymbolSet();
    protected Stack<Node> nodeStack = new Stack<>();
    protected Stack<Character> opStack = new Stack<>();
    protected Node root;
    protected Set<LeafNode> leaves = new HashSet<>();
    protected Stack<Set<Integer>> notCompletedGroups = new Stack<>();
    protected Set<Integer> used = new HashSet<>();
    protected Map<Integer, Set<Integer>> tmpGroups = new HashMap<>();
    protected int cntOpen = 0;

    public SyntaxTree(RegExp regex) {
        String regexString = regex.toString();
        buildBinaryTree(regexString);
        computeFunctions();
    }

    public SyntaxTree(String regex) {
        RegExp preparedRegex = new RegExp(regex);
        String regexString = preparedRegex.toString();
        buildBinaryTree(regexString);
        computeFunctions();
    }

    protected void buildBinaryTree(String regexString) {
        int len = regexString.length();
        // Flag which is true when there is something like: \( or \* or etc
        boolean isEscaped = false;
        for (int i = 0; i < len; i++) {
            if (regexString.charAt(i) == '\\' && !isEscaped) {
                isEscaped = true;
                continue;
            }
            if (isSymbol(regexString.charAt(i)) || isEscaped) {
                if (isEscaped) {
                    //create a node with "\{symbol}" symbol
                    this.pushNode("\\" + regexString.charAt(i));
                } else {
                    this.pushNode(Character.toString(regexString.charAt(i)));
                }
                for (Set<Integer> group : notCompletedGroups) {
                    group.add(leafNodeId);
                }
                isEscaped = false;
            } else if (opStack.isEmpty() || regexString.charAt(i) == '(') {
                ++cntOpen;
                opStack.push(regexString.charAt(i));
                notCompletedGroups.push(new HashSet<>());
            }
            else if (regexString.charAt(i) == ')') {
                while (opStack.peek() != '(') this.operate();
                this.operate();
                Set<Integer> newGroup = notCompletedGroups.pop();
                int maxCnt = cntOpen;
                while(used.contains(maxCnt)) {
                    maxCnt--;
                }
                tmpGroups.put(maxCnt, newGroup);
                used.add(maxCnt);
            } else {
                while (this.priority(opStack.peek(), regexString.charAt(i))) this.operate();
                opStack.push(regexString.charAt(i));
            }
        }
        while (!opStack.isEmpty()) operate();
    }

    protected void computeFunctions() {
        this.checkNullable(root);
        this.genFirstPos(root);
        this.genLastPos(root);
        this.genFollowPos(root);
    }

    protected void checkNullable(Node root) {
        if (root == null) return;
        checkNullable(root.getLeft());
        checkNullable(root.getRight());
        if (root instanceof LeafNode) return;
        if (root.getSymbol().charAt(0) == '|') {
            if (root.getLeft().isNullable() || root.getRight().isNullable()) root.setNullable(true);
        } else if (root.getSymbol().charAt(0) == '&') {
            if (root.getLeft().isNullable() && root.getRight().isNullable()) root.setNullable(true);
        } else if (root.getSymbol().equals("()")) {
            if (root.getLeft().isNullable()) root.setNullable(true);
        } else if (root.getSymbol().charAt(0) == '*') {
            root.setNullable(true);
        }
    }

    public Map<Integer, Set<Integer>> getTmpGroups() {
        return tmpGroups;
    }

    protected void genFirstPos(Node root) {
        if (root == null) return;
        genFirstPos(root.getLeft());
        genFirstPos(root.getRight());
        if (root instanceof LeafNode) {
            LeafNode temp = (LeafNode) root;
            root.addToFirstPos(temp.getId());

        } else if (root.getSymbol().charAt(0) == '|') {
            Set<Integer> tempSetLeft = root.getLeft().getFirstPos();
            Set<Integer> tempSetRight = root.getRight().getFirstPos();
            root.addAllToFirstPos(tempSetLeft);
            root.addAllToFirstPos(tempSetRight);
        } else if (root.getSymbol().charAt(0) == '&') {
            Set<Integer> tempSetLeft = root.getLeft().getFirstPos();
            if (root.getLeft().isNullable()) {
                Set<Integer> tempSetRight = root.getRight().getFirstPos();
                root.addAllToFirstPos(tempSetLeft);
                root.addAllToFirstPos(tempSetRight);
            } else {
                root.addAllToFirstPos(tempSetLeft);
            }

        } else if (root.getSymbol().charAt(0) == '*') {
            Set<Integer> tempSetLeft = root.getLeft().getFirstPos();
            root.addAllToFirstPos(tempSetLeft);
        } else if (root.getSymbol().equals("()")) {
            Set<Integer> tempSetLeft = root.getLeft().getFirstPos();
            root.addAllToFirstPos(tempSetLeft);
        }
    }

    protected void genLastPos(Node root) {
        if (root == null) return;
        genLastPos(root.getLeft());
        genLastPos(root.getRight());
        if (root instanceof LeafNode) {
            LeafNode temp = (LeafNode) root;
            root.addToLastPos(temp.getId());
        } else if (root.getSymbol().charAt(0) == '|') {
            Set<Integer> tempSetLeft = root.getLeft().getLastPos();
            Set<Integer> tempSetRight = root.getRight().getLastPos();
            root.addAllToLastPos(tempSetLeft);
            root.addAllToLastPos(tempSetRight);
        } else if (root.getSymbol().charAt(0) == '&') {
            if (root.getRight().isNullable()) {
                Set<Integer> tempSetLeft = root.getLeft().getLastPos();
                Set<Integer> tempSetRight = root.getRight().getLastPos();
                root.addAllToLastPos(tempSetLeft);
                root.addAllToLastPos(tempSetRight);
            } else {
                Set<Integer> tempSetRight = root.getRight().getLastPos();
                root.addAllToLastPos(tempSetRight);
            }
        } else if (root.getSymbol().charAt(0) == '*') {
            Set<Integer> tempSetLeft = root.getLeft().getLastPos();
            root.addAllToLastPos(tempSetLeft);
        } else if (root.getSymbol().equals("()")) {
            Set<Integer> tempSetLeft = root.getLeft().getLastPos();
            root.addAllToLastPos(tempSetLeft);
        }
    }

    protected void genFollowPos(Node root) {
        if (root == null || root instanceof LeafNode) return;
        if (root.getSymbol().charAt(0) == '|' || root.getSymbol().equals("()")) {
            genFollowPos(root.getLeft());
            genFollowPos(root.getRight());
        } else {
            genFollowPos(root.getLeft());
            genFollowPos(root.getRight());
            if (root.getSymbol().charAt(0) == '&') {
                for (int i : root.getLeft().getLastPos()) {
                    Set<Integer> temp = root.getRight().getFirstPos();
                    Objects.requireNonNull(this.getLeaf(i)).addAllToFollowPos(temp);
                }
            } else if (root.getSymbol().charAt(0) == '*') {
                for (int i : root.getLastPos()) {
                    Set<Integer> temp = root.getFirstPos();
                    Objects.requireNonNull(this.getLeaf(i)).addAllToFollowPos(temp);
                }
            }
        }
    }

    protected boolean isSymbol(String symb) {
        return symbolSet.contains(symb.charAt(0)) && !opSet.contains(symb.charAt(0)) || symb.charAt(0) == '\\';
    }

    protected boolean isSymbol(char ch) {
        return symbolSet.contains(ch) && !opSet.contains(ch);
    }

    protected LeafNode getLeaf(int id) {
        for (LeafNode node : leaves) {
            if (node.getId() == id) return node;
        }
        return null;
    }

    public void printData() {
        System.out.println("All nodes with Firstpos and Lastpos:");
        this.printInOrder(root);
        System.out.println("All leaves with followpos:");
        this.printDataFollowPos(root);
    }

    protected boolean priority(char c1, char c2) {
        if (c1 == c2) return true;
        else if (c1 == '*') return true;
        else if (c2 == '*') return false;
        else if (c1 == '&' && c2 == '|') return true;
        else if (c1 == '(') return false;
        else return false;
    }

    protected void operate() {
        char sw = opStack.pop();
        if (sw == '|') union();
        else if (sw == '&') concat();
        else if (sw == '*') closure();
        else if (sw == '(') makeGroup();
    }

    protected void union() {
        Node right = nodeStack.pop();
        Node left = nodeStack.pop();
        Node newRoot = pushNode("|");
        newRoot.setLeft(left);
        newRoot.setRight(right);
        root = newRoot;
    }

    protected void concat() {
        Node right = nodeStack.pop();
        Node left = nodeStack.pop();
        Node newRoot = pushNode("&");
        newRoot.setLeft(left);
        newRoot.setRight(right);
        root = newRoot;
    }

    protected void closure() {
        Node left = nodeStack.pop();
        Node newRoot = pushNode("*");
        newRoot.setLeft(left);
        root = newRoot;
    }

    protected void makeGroup() {
        Node left = nodeStack.pop();
        Node newRoot = pushNode("()");
        newRoot.setLeft(left);
        root = newRoot;
    }

    protected Node pushNode(String symbol) {
        if (isSymbol(symbol)) {
            LeafNode newNode = new LeafNode(symbol, ++leafNodeId);
            nodeStack.push(newNode);
            leaves.add(newNode);
            return newNode;
        } else {
            Node newNode = new Node(symbol);
            nodeStack.push(newNode);
            return newNode;
        }
    }

    protected void printInOrder(Node node) {
        if (node != null) {
            printInOrder(node.getLeft());
            System.out.println(node.getSymbol() + ":-" + node.getFirstPos() + "(FP);" + node.getLastPos() + "(LP)");
            printInOrder(node.getRight());
        }
    }

    protected void printDataFollowPos(Node node) {
        if (node != null) {
            printDataFollowPos(node.getLeft());
            if (node instanceof LeafNode) {
                LeafNode temp = (LeafNode) node;
                System.out.println("(" + temp.getSymbol() + "," + temp.getId() + "):-" + temp.getFollowPos());
            }
            printDataFollowPos(node.getRight());
        }
    }

    void print(String prefix, Node node, boolean isLeft) {
        if (node != null) {
            System.out.print(prefix);
            System.out.print((isLeft ? "├──" : "└──"));
            System.out.println(node.getSymbol());
            print(prefix + (isLeft ? "│   " : "    "), node.getLeft(), true);
            print(prefix + (isLeft ? "│   " : "    "), node.getRight(), false);
        }
    }

    public void print() {
        print("", root, false);
    }

    public Node getRoot() {
        return root;
    }

    public Set<LeafNode> getLeaves() {
        return leaves;
    }
}
