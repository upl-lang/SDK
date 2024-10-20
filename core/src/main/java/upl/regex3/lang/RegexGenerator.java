package upl.regex3.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RegexGenerator {
    static Map<String, State> fsm = new HashMap<>();
    static String startState;
    static List<String> acceptStates;
    static List<String> allStates;

    public static String generateFromLangFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        startState = reader.readLine().split("=")[1];
        acceptStates = new ArrayList<>(Arrays.asList(reader.readLine().split("=")[1].split(",")));
        reader.readLine();
        allStates = new ArrayList<>(Arrays.asList(reader.readLine().split("=")[1].split(",")));

        for (String str : allStates) {
            fsm.put(str, new State(str, acceptStates.contains(str), str.equals(startState)));
        }

        String st;
        while ((st = reader.readLine()) != null && !st.equals("")) {
            String[] tmp = st.split(",");
            String[] tmp1 = tmp[1].split("=");
            String from = tmp[0];
            String to = tmp1[1];
            String value = tmp1[0];
            if (from.equals(to)) {
                fsm.get(from).addSelfLoop(value);
            } else {
                fsm.get(from).addOutTransition(to, value);
                fsm.get(to).addInTransition(from, value);
            }
        }
        System.out.println("Adding new start state (Qs)");
        System.out.println("Adding new final state (Qf)");
        addNewAccept();
        addNewStart();

        while (fsm.size() > 2) {
            eliminateState();
        }
        System.out.println("Regular Expression: " + fsm.get("Qs").outTransitions.get("Qf").value);
        return fsm.get("Qs").outTransitions.get("Qf").value;
    }

    public static void eliminateState() {
        removeDeadState();
        String removeState = pickState();
        State state = fsm.get(removeState);
        ArrayList<String> removeIn = new ArrayList<>();
        ArrayList<String> removeOut = new ArrayList<>();
        System.out.println("--------------------------------------------------");
        System.out.println("Removing " + state.label + "...");
        for (State.Transition transIn : state.inTransitions.values()) {
            for (State.Transition transOut : state.outTransitions.values()) {
                transIn.value = transIn.value.equals("ε") ? "": transIn.value;
                transOut.value = transOut.value.equals("ε") ? "": transOut.value;
                if (transIn.from.equals(transOut.to)) {
                    if (fsm.get(state.label).selfLoop == null) {
                        fsm.get(transIn.from).addSelfLoop(transIn.value + transOut.value);
                    } else {
                        String selfLoopValue = state.selfLoop.value.length() == 1 ? state.selfLoop.value : "(" + state.selfLoop.value + ")";
                        fsm.get(transIn.from).addSelfLoop(transIn.value + selfLoopValue + "*" + transOut.value);
                    }
                } else {
                    if (fsm.get(state.label).selfLoop == null) {
                        fsm.get(transIn.from).addOutTransition(transOut.to, transIn.value + transOut.value);
                        fsm.get(transOut.to).addInTransition(transIn.from, transIn.value + transOut.value);
                    } else {
                        String selfLoopValue = state.selfLoop.value.length() == 1 ? state.selfLoop.value : "(" + state.selfLoop.value + ")";
                        fsm.get(transIn.from).addOutTransition(transOut.to, transIn.value + selfLoopValue + "*" + transOut.value);
                        fsm.get(transOut.to).addInTransition(transIn.from, transIn.value + selfLoopValue + "*" + transOut.value);
                    }
                }
                removeOut.add(transOut.to);
            }
            removeIn.add(transIn.from);
        }
        allStates.remove(removeState);
        fsm.remove(removeState);
        for (String str : removeOut) {
            fsm.get(str).removeInTransition(removeState);
        }
        for (String str : removeIn) {
            fsm.get(str).removeOutTransition(removeState);
        }
    }
    public static String pickState() {
        PriorityQueue<Map.Entry<String, Integer>> queue = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());
        int sum;
        for (State state : fsm.values()) {
            sum = 0;
            if (!state.isAccept && !state.isStart) {
                if (state.selfLoop != null) {
                    sum++;
                }
                sum += state.inTransitions.size() + state.outTransitions.size();
                queue.offer(new AbstractMap.SimpleEntry<>(state.label, sum));
            }
        }
        String minimumState = null;
        for (Map.Entry<String, Integer> value : queue) {
            minimumState = value.getKey();
        }
        return minimumState;
    }
    public static void removeDeadState() {
        boolean removed = true;
        while (removed) {
            String label = "";
            removed = false;
            for (State state : fsm.values()) {
                if (state.outTransitions.size() == 0 && !state.isAccept) {
                    for (State.Transition trans : state.inTransitions.values()) {
                        fsm.get(trans.from).outTransitions.remove(trans.to);
                        removed = true;
                    }
                    label = state.label;
                    System.out.println("-------------------------------------------");
                    System.out.println("Dead State " + label + " is deleted.");
                    System.out.println("-------------------------------------------");
                }
            }
            fsm.remove(label);
            allStates.remove(label);
        }
    }

    // Add new start state and replace previous start
    public static void addNewStart() {
        State newStart = new State("Qs", false, true);
        newStart.addOutTransition(fsm.get(startState).label, "ε");
        fsm.put(newStart.label, newStart);
        fsm.get(startState).isStart = false;
        fsm.get(startState).addInTransition(newStart.label, "ε");
        startState = newStart.label;
        allStates.add(0, newStart.label);
    }
    // Add new accept state and replace previous accepts
    public static void addNewAccept() {
        State newAccept = new State("Qf", true, false);
        for (State state : fsm.values()) {
            if (acceptStates.contains(state.label)) {
                newAccept.addInTransition(state.label, "ε");
                state.addOutTransition(newAccept.label, "ε");
                state.isAccept = false;
                acceptStates.remove(state.label);
            }
        }
        fsm.put(newAccept.label, newAccept);
        acceptStates.add(newAccept.label);
        allStates.add(newAccept.label);
    }
}
