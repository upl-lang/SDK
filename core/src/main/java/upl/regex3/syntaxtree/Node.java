package upl.regex3.syntaxtree;

import java.util.HashSet;
import java.util.Set;

public class Node {
    protected Node left = null;
    protected Node right = null;
    protected String symbol;

    protected Set<Integer> firstPos = new HashSet<>();
    protected Set<Integer> lastPos = new HashSet<>();
    protected boolean nullable = false;

    public Node(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public void addToFirstPos(int number) {
        firstPos.add(number);
    }
    public void addAllToFirstPos(Set<Integer> set) {
        firstPos.addAll(set);
    }

    public void addToLastPos(int number) {
        lastPos.add(number);
    }
    public void addAllToLastPos(Set<Integer> set) {
        lastPos.addAll(set);
    }

    public Set<Integer> getFirstPos() {
        return firstPos;
    }

    public Set<Integer> getLastPos() {
        return lastPos;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
}
