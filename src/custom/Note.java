package custom;

public class Note {
    public String text = "";
    public int startOffset = 0, endOffset = 0;

    public Note(String text, int startOffset, int endOffset) {
        this.text = text;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }
}
