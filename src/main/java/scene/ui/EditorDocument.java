package scene.ui;

import javax.swing.text.DefaultStyledDocument;

public class EditorDocument extends DefaultStyledDocument {

    public EditorDocument(){
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
