package scene.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DocumentAdapter implements DocumentListener {
    @Override
    public void insertUpdate(DocumentEvent e) {
        textUpdated(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        textUpdated(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

    public void textUpdated(DocumentEvent e){}
}
