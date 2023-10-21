package scene.ui;

import com.formdev.flatlaf.util.SystemInfo;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;
import scene.app.SpellingError;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Spellchecker {


    private ReentrantLock updateAllLock = new ReentrantLock();
    private String info = "";


    public Spellchecker(){

    }

    public void start(JTextEditor textEditor){
        SwingWorker swingWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() {


                JLanguageTool languageTool = new JLanguageTool(Languages.getLanguageForShortCode("en-GB"));



                while(true){
                    updateAllLock.lock();

                    try {

                        if(!info.isEmpty()){
                            spellcheckEverything(languageTool, textEditor, textEditor.getText());
                            info = "";
                        }


                    }
                    finally { updateAllLock.unlock(); }

                    spellcheckLine(languageTool, textEditor);
                }
            }
        };
        swingWorker.execute();
    }

    private List<SpellingError> spellingErrors = Collections.synchronizedList(new ArrayList<>());

    public Highlighter.HighlightPainter errorHighlighter = new ErrorHighlightPainter();


    private void spellcheckEverything(JLanguageTool languageTool, JTextEditor textEditor, String text){
        if(SystemInfo.isWindows)
            text = text.replaceAll("\r\n", "\n");

        spellingErrors.clear();


        List<RuleMatch> m;
        try {
            m = languageTool.check(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Creating Highlights
        {

            for(RuleMatch ruleMatch : m){

                SpellingError spellingError = new SpellingError(ruleMatch.getFromPos(), ruleMatch.getToPos(), ruleMatch.getMessage());
                Element root = textEditor.getDocument().getDefaultRootElement();

                spellingError.line = root.getElementIndex(ruleMatch.getFromPos());
                spellingError.suggestions = ruleMatch.getSuggestedReplacements();

                if(!spellingErrors.contains(spellingError)){
                    spellingErrors.add(spellingError);

                    SwingUtilities.invokeLater(() -> {
                        try {
                            spellingError.highlight = (Highlighter.Highlight) textEditor.getHighlighter().addHighlight(ruleMatch.getFromPos(), ruleMatch.getToPos(), errorHighlighter);
                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                        textEditor.repaint();
                    });

                }
            }
        }
    }

    /*
    Lots of race conditions here and assumptions that just explode because data is fetched,
    the thread is suspended, the data is changed, and now the copy of the data is invalidated
     */
    private void spellcheckLine(JLanguageTool languageTool, JTextEditor textEditor) {


        Element element = UIUtil.currentLine(textEditor);
        String currentLineText = null;
        try {
            currentLineText = textEditor.getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }





        List<RuleMatch> matches;
        //LanguageTool check
        {
            try {
                if(SystemInfo.isWindows)
                    currentLineText = currentLineText.replaceAll("\r\n", "\n");
                matches = languageTool.check(currentLineText);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        //Creating Highlights
        {

            for (RuleMatch match : matches) {
                int line = textEditor
                        .getDocument()
                        .getDefaultRootElement()
                        .getElementIndex(textEditor.getCaretPosition());

                int start = element.getStartOffset() + match.getFromPos();
                int end = element.getStartOffset() + match.getToPos();


                SpellingError spellingError = new SpellingError(start, end, match.getMessage());
                spellingError.line = line;
                spellingError.suggestions = match.getSuggestedReplacements();


                if (!spellingErrors.contains(spellingError)) {
                    spellingErrors.add(spellingError);

                    SwingUtilities.invokeLater(() -> {
                        try {
                            spellingError.highlight = (Highlighter.Highlight) textEditor.getHighlighter().addHighlight(start, end, errorHighlighter);
                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                        textEditor.repaint();
                    });
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


            }
        }


        //Remove stale highlights
        {



            int caretPosition = textEditor.getCaretPosition();
            Element root = textEditor.getDocument().getDefaultRootElement();
            int lineNum = root.getElementIndex(caretPosition);


            for (Iterator<SpellingError> iterator = spellingErrors.iterator(); iterator.hasNext(); ) {
                SpellingError spellingError = iterator.next();
                if (spellingError.line == lineNum) {
                    boolean matchFound = false;

                    for (RuleMatch match : matches) {
                        int startOffset = element.getStartOffset() + match.getFromPos();
                        int endOffset = element.getStartOffset() + match.getToPos();

                        if (startOffset == spellingError.getStartOffset() && endOffset == spellingError.getEndOffset()) {
                            matchFound = true;
                            break;
                        }
                    }


                    if (!matchFound) {
                        SwingUtilities.invokeLater(() -> {
                            textEditor.getHighlighter().removeHighlight(spellingError.highlight);
                            textEditor.repaint();
                        });

                        iterator.remove();
                    }



                }
            }
        }

        //Let the UI stuff sync up, so we grab the latest UI
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAll(String entireText){

        updateAllLock.lock();

        try {
            info = entireText;
        }
        finally {
            updateAllLock.unlock();
        }

    }

    public List<SpellingError> getErrors() {
        return spellingErrors;
    }
}
