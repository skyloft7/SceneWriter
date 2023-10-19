package scene.app;

import javax.swing.text.Highlighter;
import java.util.List;

public class Error {
    public int startOffset, endOffset;
    //public int startOffsetDoc, endOffsetDoc;
    public String message;
    public Highlighter.Highlight highlight;
    public int line;
    public List<String> suggestions;

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

        //return (error.highlight.getStartOffset() == highlight.getStartOffset() && error.highlight.getEndOffset() == highlight.getEndOffset());
    }
}
