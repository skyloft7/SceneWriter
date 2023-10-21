package scene.ui;

import javax.swing.text.Highlighter;

public class Note {
    public String text = "";
    private final int startOffsetDoc, endOffsetDoc;
    public Highlighter.Highlight highlight;

    public Note(String text, int startOffsetDoc, int endOffsetDoc) {
        this.text = text;
        this.startOffsetDoc = startOffsetDoc;
        this.endOffsetDoc = endOffsetDoc;
    }

    public int getStartOffset(){
        if(highlight == null) return startOffsetDoc;
        else return highlight.getStartOffset();
    }
    public int getEndOffset(){
        if(highlight == null) return endOffsetDoc;
        else return highlight.getEndOffset();
    }


}
