package scene.ui;

import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import flatlaf.FlatLafUtils;
import scene.app.PopupContext;
import scene.app.PopupLifetimeManager;
import scene.app.Popups;
import scene.app.SpellingError;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

import static java.awt.event.KeyEvent.*;

public class JTextEditor extends JTextPane {

    private UndoManager manager = new UndoManager();
    private ArrayList<Note> notes = new ArrayList<>();

    private DefaultHighlighter.DefaultHighlightPainter noteHighlighter;
    private PopupLifetimeManager popupLifetimeManager = new PopupLifetimeManager();
    private MutableAttributeSet cursorAttributes = new SimpleAttributeSet();
    private Analyzer analyzer = new Analyzer();
    private Spellchecker spellchecker = new Spellchecker();


    public JTextEditor() {
        super();
        setStyledDocument(new TextDocument());
        getStyledDocument().addUndoableEditListener(manager);
        popupLifetimeManager.install(this);
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

                        JFontBoxMenuItem jFontBoxMenuItem = new JFontBoxMenuItem(13, Font.PLAIN, GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
                        {
                            jFontBoxMenuItem.setSelectedFont(StyleConstants.getFontFamily(getCurrentAttributes()));
                            jFontBoxMenuItem.addFontListener(f -> {

                                SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
                                StyleConstants.setFontFamily(simpleAttributeSet, f.getFamily());
                                updateCurrentAttribWith(simpleAttributeSet);


                            });
                        }
                        jPopupMenu.add(jFontBoxMenuItem);

                        JSpinner fontSize = new JSpinner(new SpinnerNumberModel(
                                StyleConstants.getFontSize(getCurrentAttributes()),
                                0,
                                100,
                                1
                        ));
                        {
                            fontSize.addChangeListener(e13 -> {
                                SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
                                StyleConstants.setFontSize(simpleAttributeSet, (Integer) fontSize.getValue());
                                updateCurrentAttribWith(simpleAttributeSet);
                            });
                        }
                        jPopupMenu.add(fontSize);

                        jPopupMenu.addSeparator();




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
                                            note.highlight = (Highlighter.Highlight) getHighlighter().addHighlight(note.getStartOffset(), note.getEndOffset(), noteHighlighter);
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
                                try {
                                    m.setToolTipText("<html><p style='font-style:italic;color:gray;'>" + getText(note.getStartOffset(), note.getEndOffset() - note.getStartOffset()) + "</p></html>");
                                } catch (BadLocationException ex) {
                                    throw new RuntimeException(ex);
                                }

                                m.addActionListener(e1 -> {
                                    try {
                                        scrollRectToVisible(modelToView2D(note.getStartOffset()).getBounds());
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

                        jPopupMenu.show(JTextEditor.this, e.getX(), e.getY());
                    }


                    //See contents of a Note
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        int offset = viewToModel2D(e.getPoint());

                        for (Iterator<Note> iterator = notes.iterator(); iterator.hasNext(); ) {
                            Note note = iterator.next();
                            if (offset >= note.getStartOffset() && offset <= note.getEndOffset()) {
                                Point point = e.getPoint();

                                showNotePopup(point, note);

                            }
                        }
                    }


                }
            });
        }
        //Live Typing with new attributes
        {
            getStyledDocument().addDocumentListener(new DocumentAdapter() {
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
                        ((TextDocument) getStyledDocument()).lockWriter();


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
                                ((TextDocument) getStyledDocument()).unlockWriter();

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
        //List Auto-Indent
        {
            getStyledDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        Document document = getDocument();

                        if(document.getLength() != 0){

                            try {


                                //Only on blank lines

                                Element paragraph = getStyledDocument().getParagraphElement(e.getOffset());
                                boolean isIndentReady = analyzer.shouldIndentLine(document.getText(paragraph.getStartOffset(), paragraph.getEndOffset() - paragraph.getStartOffset()));



                                if(isIndentReady) {

                                    if (document.getText(e.getOffset(), 1).equals("-")) {
                                        document.insertString(e.getOffset(), "    ", new SimpleAttributeSet());
                                    }
                                }





                            } catch (BadLocationException ex) {
                                throw new RuntimeException(ex);
                            }




                        }






                    });
                }
            });
        }
        //Spellcheck
        {


            spellchecker.start(this);

            Action pasteAction = getActionMap().get("paste-from-clipboard");
            getActionMap().put("paste-from-clipboard", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pasteAction.actionPerformed(e);
                    spellchecker.updateAll(JTextEditor.this.getText());
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int pos = viewToModel2D(e.getPoint());

                    if(e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {

                        for (SpellingError spellingError : spellchecker.getErrors()) {
                            if (pos >= spellingError.highlight.getStartOffset() && pos <= spellingError.highlight.getEndOffset())
                                showSpellcheckErrorPopup(e.getPoint(), spellingError);
                        }


                    }
                }
            });









        }


    }

    @Override
    public void updateUI() {
        super.updateUI();

        if(spellchecker != null) {
            for (SpellingError spellingError : spellchecker.getErrors()) {
                try {
                    spellingError.highlight = (Highlighter.Highlight) getHighlighter().addHighlight(spellingError.getStartOffset(), spellingError.getEndOffset(), spellingError.highlight.getPainter());
                } catch (BadLocationException ignored) {}
            }
        }

        if(notes != null) {
            for (Note note : notes) {
                try {
                    note.highlight = (Highlighter.Highlight) getHighlighter().addHighlight(note.getStartOffset(), note.getEndOffset(), note.highlight.getPainter());
                } catch (BadLocationException ignored) {
                }

            }
        }




    }

    public void showSpellcheckErrorPopup(Point point, SpellingError spellingError){
        SwingUtilities.convertPointToScreen(point, JTextEditor.this);


        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BorderLayout());

        JLabel errorMessage = new JLabel(spellingError.message);
        panel.add(errorMessage, BorderLayout.CENTER);

        if(spellingError.suggestions != null){


            JPanel s = new JPanel();
            s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));

            for(String r : spellingError.suggestions){

                JLabel label = new JLabel(r);
                label.setForeground(FlatLafUtils.accentColor);


                s.add(label);
            }


            panel.add(s, BorderLayout.SOUTH);
        }



        Popup popup = Popups.create(JTextEditor.this, panel, point);

        popupLifetimeManager.register("spellcheckErrorPopup", new PopupContext(popup, panel, popup1 -> {}));

        popup.show();
    }

    public void showNotePopup(Point point, Note note){
        SwingUtilities.convertPointToScreen(point, JTextEditor.this);
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

        Popup popup = Popups.create(JTextEditor.this, panel, point);


        remove.addActionListener(e1 -> {

            notes.remove(note);
            popup.hide();

            //Remove the Highlight
            for(Highlighter.Highlight highlight : getHighlighter().getHighlights()) {
                if(highlight.getStartOffset() == note.getStartOffset() && highlight.getEndOffset() == note.getEndOffset()){
                    getHighlighter().removeHighlight(highlight);
                }

            }

        });


        popupLifetimeManager.register("newNotePopup", new PopupContext(popup, panel, popup1 -> note.text = noteText.getText()));


        popup.show();
    }


    private boolean isSelectingText(){
        return getSelectionEnd() - getSelectionStart() > 0;
    }

    private AttributeSet getCurrentAttributes(){
        return isSelectingText() ? getStyledDocument().getCharacterElement(getSelectionStart()).getAttributes() : cursorAttributes;
    }

    private void updateCurrentAttribWith(AttributeSet a){
        if(isSelectingText()){
            getStyledDocument().setCharacterAttributes(getSelectionStart(), getSelectionEnd() - getSelectionStart(), a, false);
        }
        else {
            cursorAttributes.addAttributes(a);
        }
    }


    public ArrayList<Note> getNotes() {
        return notes;
    }


    private class Analyzer {
        public boolean shouldIndentLine(String text){
            return text.replaceAll("-", "").isBlank();
        }
    }
}
