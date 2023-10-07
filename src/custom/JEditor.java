package custom;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.undo.UndoManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.*;

public class JEditor extends JTextPane {
    public JEditor() {

        UndoManager manager = new UndoManager();

        getStyledDocument().addUndoableEditListener(manager);


        final SimpleAttributeSet[] currentAttr = {new SimpleAttributeSet()};
        getStyledDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    //For live typing with Italics/Bold/Underline/etc.
                    getStyledDocument().setCharacterAttributes(e.getOffset(), 1, currentAttr[0], false);
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });


        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int selectedLength = getSelectionEnd() - getSelectionStart();
                boolean selectingText = selectedLength > 0;

                AttributeSet attributeSet = currentAttr[0];

                if (selectingText) {
                    currentAttr[0].removeAttributes(currentAttr[0]);
                    attributeSet = getStyledDocument().getCharacterElement(getSelectionStart()).getAttributes();
                }


                AttributeSet preChangeAttr = null;

                if (e.isControlDown()) {


                    preChangeAttr = currentAttr[0].copyAttributes();


                    if (e.getKeyCode() == VK_I) {
                        StyleConstants.setItalic(currentAttr[0], !StyleConstants.isItalic(attributeSet));
                    }
                    if (e.getKeyCode() == VK_B) {
                        StyleConstants.setBold(currentAttr[0], !StyleConstants.isBold(attributeSet));
                    }
                    if (e.getKeyCode() == VK_U) {
                        StyleConstants.setUnderline(currentAttr[0], !StyleConstants.isUnderline(attributeSet));
                    }
                    if (e.getKeyCode() == VK_COMMA) {
                        StyleConstants.setSubscript(currentAttr[0], !StyleConstants.isSubscript(attributeSet));
                    }
                    if (e.getKeyCode() == VK_PERIOD) {
                        StyleConstants.setSuperscript(currentAttr[0], !StyleConstants.isSuperscript(attributeSet));
                    }
                    if (e.getKeyCode() == VK_UP) {
                        int oldSize = StyleConstants.getFontSize(attributeSet);
                        StyleConstants.setFontSize(currentAttr[0], oldSize + 1);
                    }
                    if (e.getKeyCode() == VK_DOWN) {
                        int oldSize = StyleConstants.getFontSize(attributeSet);
                        StyleConstants.setFontSize(currentAttr[0], oldSize - 1);
                    }


                }

                if (selectingText) {
                    getStyledDocument().setCharacterAttributes(
                            getSelectionStart(),
                            selectedLength,
                            currentAttr[0],
                            false
                    );

                    if (preChangeAttr != null)
                        currentAttr[0] = (SimpleAttributeSet) preChangeAttr;
                }


                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    manager.undo();
                }
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
                    manager.redo();
                }


            }
        });


    }
}
