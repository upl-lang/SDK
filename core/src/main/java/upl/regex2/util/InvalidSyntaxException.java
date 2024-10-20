package upl.regex2.util;

import upl.regex2.tree.node.Node;

/**
 * Created on 2015/5/9.
 */
public class InvalidSyntaxException extends IllegalArgumentException {

    public InvalidSyntaxException() {
    }

    public InvalidSyntaxException(String s) {
        super(s);
    }

    public InvalidSyntaxException(Throwable cause) {
        super(unknownErrMsg(), cause);
    }

    public InvalidSyntaxException(Node node) {
        super(nodeErrMsg(node));
    }

    public InvalidSyntaxException(Node node, Throwable cause) {
        super(nodeErrMsg(node), cause);
    }

    protected static String unknownErrMsg() {
        return "Invalid syntax found. ";
    }

    protected static String nodeErrMsg(Node node) {
        return "Syntax error at node: " + node;
    }
}
