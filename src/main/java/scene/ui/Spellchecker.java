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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
                Thread.currentThread().setName("Spellchecker");

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

    public Highlighter.HighlightPainter errorHighlighter = new SpellingErrorHighlightPainter();


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
                try {
                    spellingError.text = textEditor.getText(spellingError.getStartOffset(), spellingError.getEndOffset() - spellingError.getStartOffset());
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }

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

    private void spellcheckLine(JLanguageTool languageTool, JTextEditor textEditor) {
        Element element = UIUtil.currentLine(textEditor);
        String currentLineText;
        List<RuleMatch> matches;





        //LanguageTool check
        {
            try {
                currentLineText = textEditor.getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset());
                if(SystemInfo.isWindows)
                    currentLineText = currentLineText.replaceAll("\r\n", "\n");

                //This is not the currentLine! Everything's all jumbled together!
                matches = languageTool.check(currentLineText);
            } catch (IOException | BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        int line = textEditor
                .getDocument()
                .getDefaultRootElement()
                .getElementIndex(textEditor.getCaretPosition());

        //Removing stale highlights
        {


            Optional<SpellingError> stale = Optional.empty();

            for (SpellingError spellingError : spellingErrors) {
                if (spellingError.line != line) continue;


                if (!isValidError(spellingError, matches, element)) {
                    try {
                        SwingUtilities.invokeAndWait(() -> {
                            textEditor.getHighlighter().removeHighlight(spellingError.highlight);
                            textEditor.repaint();
                        });

                        stale = Optional.of(spellingError);

                        break;
                    } catch (InvocationTargetException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            stale.ifPresent(spellingError -> spellingErrors.remove(spellingError));





        }



        //Creating Highlights
        {

            for (RuleMatch match : matches) {


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
                }



            }
        }

    }

    private boolean isValidError(SpellingError spellingError, List<RuleMatch> matches, Element element){

        for (RuleMatch match : matches) {
            int startOffset = element.getStartOffset() + match.getFromPos();
            int endOffset = element.getStartOffset() + match.getToPos();

            if (startOffset == spellingError.getStartOffset() && endOffset == spellingError.getEndOffset())
                return true;
        }










        return false;
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
