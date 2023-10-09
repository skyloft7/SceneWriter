package custom;

import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import flatlaf.FlatLafUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static java.awt.event.KeyEvent.*;

public class JEditor extends JTextPane {

    private UndoManager manager = new UndoManager();
    private ArrayList<Note> notes = new ArrayList<>();
    private DefaultHighlighter.DefaultHighlightPainter noteHighlighter;
    private HashMap<String, PopupContext> popups = new HashMap<>();


    public JEditor() {

        //TODO: This can be moved to the class-level scope and then we don't have to do this whole array thing
        final SimpleAttributeSet[] currentAttr = {new SimpleAttributeSet()};

        noteHighlighter = new DefaultHighlighter.DefaultHighlightPainter(FlatLafUtils.accentColor);


        //Context Menu
        {
            addMouseListener(new MouseAdapter() {


                @Override
                public void mousePressed(MouseEvent e) {
                    //Menu
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu jPopupMenu = new JPopupMenu();

                        if (getSelectedText() != null) {

                            JMenuItem makeNote = new JMenuItem("Make Note");
                            {
                                makeNote.addActionListener(e12 -> {
                                    Note note = new Note("Empty Note", getSelectionStart(), getSelectionEnd());
                                    notes.add(note);
                                    showNotePopup(e.getPoint(), note);

                                    try {
                                        getHighlighter().addHighlight(note.startOffset, note.endOffset, noteHighlighter);
                                    } catch (BadLocationException ex) {
                                        throw new RuntimeException(ex);
                                    }

                                });

                            }


                            jPopupMenu.add(makeNote);
                        }

                        if (jPopupMenu.getComponents().length == 0)
                            jPopupMenu.add(new JLabel("Nothing to do!"));

                        jPopupMenu.show(JEditor.this, e.getX(), e.getY());
                    }


                    //See contents of a Note
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        int offset = viewToModel2D(e.getPoint());

                        for (Iterator<Note> iterator = notes.iterator(); iterator.hasNext(); ) {
                            Note note = iterator.next();
                            if (offset >= note.startOffset && offset <= note.endOffset) {
                                Point point = e.getPoint();

                                showNotePopup(point, note);

                            }
                        }
                    }


                }
            });
        }


        //Popup Drama (Close-on-exit)
        {
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    for(PopupContext c : popups.values()){
                        Point p = SwingUtilities.convertPoint(JEditor.this, e.getPoint(), c.component);

                        if(!c.component.contains(p)){
                            if(c.popupListener != null) c.popupListener.popupClosing(c.popup);
                            c.popup.hide();
                        }
                    }
                }
            });
        }

        //Live Typing with new attributes
        {
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
        }

        //Keyboard I/U/and B shortcuts and Ctrl+Z, and Ctrl+Y
        {

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

        getStyledDocument().addUndoableEditListener(manager);
    }

    public void showNotePopup(Point point, Note note){
        SwingUtilities.convertPointToScreen(point, JEditor.this);
        JTextField noteText = new JTextField(note.text);
        JButton remove = new JButton(new FlatTabbedPaneCloseIcon());
        remove.setContentAreaFilled(false);
        remove.setToolTipText("Delete Note");


        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BorderLayout());
        panel.add(noteText, BorderLayout.CENTER);
        panel.add(remove, BorderLayout.EAST);

        Popup popup = Popups.create(JEditor.this, panel, point);


        remove.addActionListener(e1 -> {

            notes.remove(note);
            popup.hide();

            //Remove the Highlight
            for(Highlighter.Highlight highlight : getHighlighter().getHighlights()) {
                if(highlight.getStartOffset() == note.startOffset && highlight.getEndOffset() == note.endOffset){
                    getHighlighter().removeHighlight(highlight);
                }

            }

        });


        popups.put("newNotePopup", new PopupContext(popup, panel, popup1 -> note.text = noteText.getText()));


        popup.show();
    }


    public ArrayList<Note> getNotes() {
        return notes;
    }
}
