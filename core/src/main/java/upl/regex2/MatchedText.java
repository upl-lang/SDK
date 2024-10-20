package upl.regex2;

/**
 * Created on 5/25/15.
 */
public class MatchedText {
    protected String text;
    protected int Pos;

    MatchedText(String text, int Pos) {
        this.text = text;
        this.Pos = Pos;
    }

    public String getText() {
        return text;
    }

    public int getPos() {
        return Pos;
    }

    @Override
    public String toString() {
        return "MatchedText{" +
                "text='" + text + '\'' +
                ", Pos=" + Pos +
                '}';
    }
}
