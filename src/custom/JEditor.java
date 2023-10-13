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
    private MutableAttributeSet cursorAttributes = new SimpleAttributeSet();



    public JEditor() {
        super();
        setStyledDocument(new EditorDocument());

        noteHighlighter = new DefaultHighlighter.DefaultHighlightPainter(FlatLafUtils.accentColor);
        StyleConstants.setFontFamily(cursorAttributes, getFont().getFamily());


        //Context Menu
        {
            addMouseListener(new MouseAdapter() {


                @Override
                public void mousePressed(MouseEvent e) {
                    //Menu
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu jPopupMenu = new JPopupMenu();

                        JFontBoxMenuItem jFontBoxMenuItem = new JFontBoxMenuItem(15, Font.PLAIN, GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
                        {
                            jFontBoxMenuItem.setSelectedFont(isSelectingText() ? StyleConstants.getFontFamily(getStyledDocument().getCharacterElement(getSelectionStart()).getAttributes()) : StyleConstants.getFontFamily(cursorAttributes));
                            jFontBoxMenuItem.addFontListener(f -> {
                                if(isSelectingText()){
                                    SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
                                    StyleConstants.setFontFamily(simpleAttributeSet, f.getFamily());
                                    getStyledDocument().setCharacterAttributes(getSelectionStart(), getSelectionEnd() - getSelectionStart(), simpleAttributeSet, false);

                                }
                                else {
                                    StyleConstants.setFontFamily(cursorAttributes, f.getFamily());
                                }
                            });
                        }


                        jPopupMenu.add(jFontBoxMenuItem);





                        JMenu notesMenu = new JMenu("Notes");
                        {
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
                                notesMenu.add(makeNote);
                            }


                            notesMenu.addSeparator();

                            for (Note note : notes) {
                                JMenuItem m = new JMenuItem(note.text);
                                m.addActionListener(e1 -> {
                                    try {
                                        scrollRectToVisible(modelToView2D(note.startOffset).getBounds());
                                    } catch (BadLocationException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                });
                                notesMenu.add(m);
                            }


                        }


                        jPopupMenu.add(notesMenu);

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
                        if(e.getLength() > 1){
                            for (int i = e.getOffset(); i < e.getOffset() + e.getLength(); i++) {
                                getStyledDocument().setCharacterAttributes(i, 1, cursorAttributes, false);
                            }
                        }
                        else getStyledDocument().setCharacterAttributes(e.getOffset(), 1, cursorAttributes, false);
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

                    boolean selectingText = getSelectedText() != null;

                    MutableAttributeSet currentAttributes = cursorAttributes;


                    if(selectingText) {
                        ((EditorDocument) getStyledDocument()).lockWriter();


                        /*
                        The first time this runs, it returns the paragraph style because there's only 1 AttributeSet (for a whole paragraph) at that getSelectionStart().
                        Then when the change is applied, the AttributeSet structure is changed and every character now has it's own, which means that next time this runs,
                        it only changes those specific letters.
                         */
                        currentAttributes = (MutableAttributeSet) getStyledDocument().getCharacterElement(getSelectionStart()).getAttributes();

                    }



                    if (e.isControlDown()) {

                        boolean actionDone = false;

                        //Actions
                        {

                            if (e.getKeyCode() == VK_I) {
                                StyleConstants.setItalic(currentAttributes, !StyleConstants.isItalic(currentAttributes));
                                actionDone = true;
                            }
                            if (e.getKeyCode() == VK_B) {
                                StyleConstants.setBold(currentAttributes, !StyleConstants.isBold(currentAttributes));
                                actionDone = true;
                            }
                            if (e.getKeyCode() == VK_U) {
                                StyleConstants.setUnderline(currentAttributes, !StyleConstants.isUnderline(currentAttributes));
                                actionDone = true;
                            }
                            if (e.getKeyCode() == VK_COMMA) {
                                StyleConstants.setSubscript(currentAttributes, !StyleConstants.isSubscript(currentAttributes));
                                actionDone = true;
                            }
                            if (e.getKeyCode() == VK_PERIOD) {
                                StyleConstants.setSuperscript(currentAttributes, !StyleConstants.isSuperscript(currentAttributes));
                                actionDone = true;
                            }
                            if (e.getKeyCode() == VK_UP) {
                                int oldSize = StyleConstants.getFontSize(currentAttributes);
                                StyleConstants.setFontSize(currentAttributes, oldSize + 1);
                                actionDone = true;
                            }
                            if (e.getKeyCode() == VK_DOWN) {
                                int oldSize = StyleConstants.getFontSize(currentAttributes);
                                StyleConstants.setFontSize(currentAttributes, oldSize - 1);
                                actionDone = true;
                            }
                        }

                        if(actionDone){
                            if(selectingText){
                                int selectionLength = getSelectionEnd() - getSelectionStart();
                                getStyledDocument().setCharacterAttributes(getSelectionStart(), selectionLength, currentAttributes, false);
                                ((EditorDocument) getStyledDocument()).unlockWriter();

                            }
                        }


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

        noteText.setColumns(20);


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

    private boolean isSelectingText(){
        return getSelectionEnd() - getSelectionStart() > 0;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }
}
