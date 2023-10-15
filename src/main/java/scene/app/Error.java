package scene.app;

import javax.swing.text.Highlighter;

public class Error {
    public int startOffset, endOffset;
    public int startOffsetDoc, endOffsetDoc;
    public String message;
    public Highlighter.Highlight highlight;
    public int line;

    public Error(int startOffset, int endOffset, String message) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.message = message;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Error)) return false;
        Error error = (Error) obj;


        return error.startOffset == startOffset && error.endOffset == endOffset;
    }
}
