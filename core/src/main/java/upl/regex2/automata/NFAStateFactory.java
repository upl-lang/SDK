package upl.regex2.automata;

/**
 * Created on 2015/5/10.
 */
public class NFAStateFactory {
    protected int nextID;

    public NFAStateFactory() {
        nextID = 0;
    }

    public synchronized NFAState newState() {
        return new NFAState(nextID++);
    }
}
