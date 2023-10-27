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
                matches = languageTool.check(currentLineText);
            } catch (IOException | BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        //Removing stale highlights
        {

            synchronized (spellingErrors) {

                int line = textEditor
                        .getDocument()
                        .getDefaultRootElement()
                        .getElementIndex(textEditor.getCaretPosition());

                ArrayList<SpellingError> invalidErrors = new ArrayList<>();


                for (SpellingError spellingError : spellingErrors) {
                    if (spellingError.line != line) continue;


                    if (!isValidError(spellingError, matches, element)) {

                        try {
                            System.out.println("Removing " + textEditor.getText(spellingError.getStartOffset(), spellingError.getEndOffset() - spellingError.getStartOffset()));



                            SwingUtilities.invokeAndWait(() -> {
                                textEditor.getHighlighter().removeHighlight(spellingError.highlight);
                                textEditor.repaint();
                            });

                            invalidErrors.add(spellingError);
                            break;
                        } catch (BadLocationException | InvocationTargetException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                for (SpellingError s : invalidErrors) {
                    spellingErrors.remove(s);
                }


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

                    System.out.println("New Error");
                }


            }
        }

    }

    private boolean isValidError(SpellingError spellingError, List<RuleMatch> matches, Element element){


        /*
        This has a problem:

        This has an eror in typing

        This has an error in typing

        Because a new letter was added, all the errors next to it are invalidated because their start/end indexes don't match anymore

        The RuleMatch indexes need to be updated!



         */




        for (RuleMatch match : matches) {
            int startOffset = element.getStartOffset() + match.getFromPos();
            int endOffset = element.getStartOffset() + match.getToPos();

            if (startOffset == spellingError.getStartOffset() && endOffset == spellingError.getEndOffset()) {
                return true;
            }
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
