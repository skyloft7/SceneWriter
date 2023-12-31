package scene.ui;

import javax.swing.text.DefaultStyledDocument;

public class TextDocument extends DefaultStyledDocument {

    public TextDocument(){
        super();
    }

    public void lockWriter(){
        writeLock();
    }
    public void unlockWriter(){
        writeUnlock();
    }

    public boolean hasWriteLock(){
        return getCurrentWriter() != null;
    }
}
