package upl.regex3.automaton;

public class StatePair {
    final DFAState s1;
    final DFAState s2;

    public StatePair(DFAState s1, DFAState s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public DFAState getFirstState() {
        return s1;
    }

    public DFAState getSecondState() {
        return s2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatePair) {
            StatePair p = (StatePair)obj;
            return p.s1 == s1 && p.s2 == s2;
        }
        else
            return false;
    }

    @Override
    public int hashCode() {
        return s1.hashCode() + s2.hashCode();
    }

    @Override
    public String toString() {
        return "StatePair{" +
                "s1=" + s1 +
                ", s2=" + s2 +
                '}';
    }
}
