package scene.ui;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;
import scene.app.Error;
import thirdparty.Utils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Spellchecker {
    private List<RuleMatch> matches;
    private ArrayList<Error> errors = new ArrayList<>();
    private Highlighter.HighlightPainter errorHighlighter;
    public Spellchecker(){
        errorHighlighter = new ErrorHighlightPainter();

    }

    public void start(JEditor jEditor, boolean watcher, boolean runOnce){
        SwingWorker spellcheckThread = new SwingWorker() {
            @Override
            protected Object doInBackground() {
                JLanguageTool languageTool = new JLanguageTool(Languages.getLanguageForShortCode("en-GB"));

                if(runOnce){
                    runAll(languageTool, jEditor);
                }
                else {
                    if(watcher){
                        while(true) Spellchecker.this.runOnCurrentLine(languageTool, jEditor);
                    }
                    else Spellchecker.this.runOnCurrentLine(languageTool, jEditor);
                }

                return null;
            }
        };
        spellcheckThread.execute();
    }

    private void runAll(JLanguageTool languageTool, JEditor jEditor){
        run(jEditor, languageTool, jEditor.getText(), false);
    }

    private void runOnCurrentLine(JLanguageTool languageTool, JEditor jEditor){
        run(jEditor, languageTool, Utils.currentLine(jEditor), true);
    }

    private void run(JEditor jEditor, JLanguageTool languageTool, String currentLineText, boolean l){

        try {
            matches = languageTool.check(currentLineText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //Creating Highlights
        {

            for (RuleMatch match : matches) {

                SwingUtilities.invokeLater(() -> {

                    int caretPosition = jEditor.getCaretPosition();
                    Element root = jEditor.getDocument().getDefaultRootElement();



                    //Needs to change for all the text
                    int line = root.getElementIndex(caretPosition);
                    Element lineElement;

                    if(l) lineElement = root.getElement(line);
                    else lineElement = root;

                    int start = lineElement.getStartOffset() + match.getFromPos();
                    int end = lineElement.getStartOffset() + match.getToPos();


                    Error error = new Error(match.getFromPos(), match.getToPos(), match.getMessage());
                    error.startOffsetDoc = start;
                    error.endOffsetDoc = end;
                    error.line = line;

                    System.out.println(match.getMessage());

                    if (!errors.contains(error)) {
                        try {
                            error.highlight = (Highlighter.Highlight) jEditor.getHighlighter().addHighlight(start, end, errorHighlighter);
                            jEditor.repaint();


                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                        errors.add(error);
                    }
                });


                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        //Removing stale Highlights
        {
            List<RuleMatch> finalMatches = matches;
            errors.removeIf(error -> {

                //Needs to change for all the text

                if(l) {
                    int caretPosition = jEditor.getCaretPosition();
                    Element root = jEditor.getDocument().getDefaultRootElement();
                    int line = root.getElementIndex(caretPosition);

                    if (line != error.line) return false;
                }





                for(RuleMatch match : finalMatches){
                    if(error.startOffset == match.getFromPos() && error.endOffset == match.getToPos()) return false;
                }

                //Stale

                try {
                    SwingUtilities.invokeAndWait(() -> {
                        jEditor.getHighlighter().removeHighlight(error.highlight);
                        jEditor.repaint();
                    });
                }
                catch (InterruptedException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

                return true;
            });

        }
    }
}
