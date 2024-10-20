package upl.regex3.syntaxtree;

import java.util.HashSet;
import java.util.Set;

public class LeafNode extends Node {
    protected int id;
    protected Set<Integer> followPos;

    public LeafNode(String symbol, int id) {
        super(symbol);
        this.id = id;
        followPos = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public Set<Integer> getFollowPos() {
        return followPos;
    }

    public void addAllToFollowPos(Set<Integer> followPos) {
        this.followPos.addAll(followPos);
    }
}
