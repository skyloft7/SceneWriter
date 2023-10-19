package scene.app;

import javax.swing.text.Highlighter;
import java.util.List;

public class Error {
    private final int startOffsetDoc;
    private final int endOffsetDoc;
    public String message;
    public Highlighter.Highlight highlight;
    public int line;
    public List<String> suggestions;


    public Error(int startOffsetDoc, int endOffsetDoc, String message){
        this.startOffsetDoc = startOffsetDoc;
        this.endOffsetDoc = endOffsetDoc;
        this.message = message;
    }

    public int getStartOffset(){
        if(highlight == null) return startOffsetDoc;
        else return highlight.getStartOffset();
    }
    public int getEndOffset(){
        if(highlight == null) return endOffsetDoc;
        else return highlight.getEndOffset();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Error)) return false;
        Error error = (Error) obj;
        return (error.getStartOffset() == getStartOffset() && error.getEndOffset() == getEndOffset());
    }
}
