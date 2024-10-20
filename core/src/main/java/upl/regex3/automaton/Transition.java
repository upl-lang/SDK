package upl.regex3.automaton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Transition {
    protected DFAState state;
    protected Map<String, Set<DFAState>> moves = new HashMap<>();

    public DFAState getState() {
        return state;
    }

    public Transition(DFAState state, String sym, Set<DFAState> states) {
        this.state = state;
        moves.put(sym, states);
    }

    public void addTransition(Transition t) {
        for (Map.Entry<String, Set<DFAState>> entry : t.moves.entrySet()) {
            if (!moves.containsKey(entry.getKey())) {
                moves.put(entry.getKey(), entry.getValue());
            } else {
                moves.get(entry.getKey()).addAll(entry.getValue());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Set<DFAState>> move : moves.entrySet())
            sb.append(String.format("[%s] x '%s' → %s\t", state, move.getKey(), Arrays.toString(move.getValue().toArray())));
        return sb.toString().trim();
    }
}
