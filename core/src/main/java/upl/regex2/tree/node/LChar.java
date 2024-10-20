package upl.regex2.tree.node;

import upl.regex2.automata.NFA;
import upl.regex2.stack.OperatingStack;
import upl.regex2.stack.ShuntingStack;

/**
 * Created on 2015/5/10.
 */
public class LChar extends LeafNode {

    public final char c;

    public LChar(char c) {
        this.c = c;
    }

    @Override
    public void accept(NFA nfa) {
        nfa.visit(this);
    }

    @Override
    public String toString() {
        String result;
        if (c == ' ') {
            result = "\\s";
        } else if (c == '\t') {
            result = "\\t";
        } else {
            result = String.valueOf(c);
        }
        return result;
    }

    @Override
    public Node copy() {
        return new LChar(c);
    }

    @Override
    public void accept(OperatingStack operatingStack) {
        operatingStack.visit(this);
    }

    @Override
    public void accept(ShuntingStack shuntingStack) {
        shuntingStack.visit(this);
    }
}
