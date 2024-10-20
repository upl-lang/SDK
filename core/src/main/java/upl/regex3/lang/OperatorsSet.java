package upl.regex3.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OperatorsSet {
    protected List<Character> operators;

    public OperatorsSet() {
        operators = new ArrayList<>(Arrays.asList('(', ')', '*', '&', '|', '[', ']'));
    }

    public boolean contains(char ch) {
        return operators.contains(ch);
    }
}
