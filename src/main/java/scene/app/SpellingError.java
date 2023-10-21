package scene.app;

import javax.swing.text.Highlighter;
import java.util.List;

public class SpellingError {
    private final int startOffsetDoc;
    private final int endOffsetDoc;
    public String message;
    public Highlighter.Highlight highlight;
    public int line;
    public List<String> suggestions;


    public SpellingError(int startOffsetDoc, int endOffsetDoc, String message){
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
        if(!(obj instanceof SpellingError)) return false;
        SpellingError spellingError = (SpellingError) obj;
        return (spellingError.getStartOffset() == getStartOffset() && spellingError.getEndOffset() == getEndOffset());
    }
}
