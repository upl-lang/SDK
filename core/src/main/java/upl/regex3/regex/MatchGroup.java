package upl.regex3.regex;

public class MatchGroup {
    String value;
    int matchStart;
    int matchEnd;

    public MatchGroup(int matchStart, int matchEnd, String value) {
        this.matchStart = matchStart;
        this.matchEnd = matchEnd;
        this.value = value;
    }
}
