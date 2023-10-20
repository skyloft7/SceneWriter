package scene.ui;

import com.formdev.flatlaf.util.SystemInfo;
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

    public void start(JEditor jEditor){
        SwingWorker swingWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() {


                JLanguageTool languageTool = new JLanguageTool(Languages.getLanguageForShortCode("en-GB"));



                while(true){
                    updateAllLock.lock();

                    try {

                        if(!info.isEmpty()){
                            spellcheckEverything(languageTool, jEditor, jEditor.getText());
                            info = "";
                        }


                    }
                    finally { updateAllLock.unlock(); }

                    spellcheckLine(languageTool, jEditor);
                }
            }
        };
        swingWorker.execute();
    }

    private List<Error> errors = Collections.synchronizedList(new ArrayList<>());

    public Highlighter.HighlightPainter errorHighlighter = new ErrorHighlightPainter();


    private void spellcheckEverything(JLanguageTool languageTool, JEditor jEditor, String text){
        if(SystemInfo.isWindows)
            text = text.replaceAll("\r\n", "\n");

        errors.clear();


        List<RuleMatch> m;
        try {
            m = languageTool.check(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Creating Highlights
        {

            for(RuleMatch ruleMatch : m){

                Error error = new Error(ruleMatch.getFromPos(), ruleMatch.getToPos(), ruleMatch.getMessage());
                Element root = jEditor.getDocument().getDefaultRootElement();

                error.line = root.getElementIndex(ruleMatch.getFromPos());
                error.suggestions = ruleMatch.getSuggestedReplacements();

                if(!errors.contains(error)){
                    errors.add(error);

                    SwingUtilities.invokeLater(() -> {
                        try {
                            error.highlight = (Highlighter.Highlight) jEditor.getHighlighter().addHighlight(ruleMatch.getFromPos(), ruleMatch.getToPos(), errorHighlighter);
                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                        jEditor.repaint();
                    });

                }
            }
        }
    }

    private void spellcheckLine(JLanguageTool languageTool, JEditor jEditor) {


        Element element = Utils.currentLine(jEditor);
        String currentLineText = null;
        try {
            currentLineText = jEditor.getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset());
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
                int line = jEditor
                        .getDocument()
                        .getDefaultRootElement()
                        .getElementIndex(jEditor.getCaretPosition());

                int start = element.getStartOffset() + match.getFromPos();
                int end = element.getStartOffset() + match.getToPos();


                Error error = new Error(start, end, match.getMessage());
                error.line = line;
                error.suggestions = match.getSuggestedReplacements();


                if (!errors.contains(error)) {
                    errors.add(error);

                    SwingUtilities.invokeLater(() -> {
                        try {
                            error.highlight = (Highlighter.Highlight) jEditor.getHighlighter().addHighlight(start, end, errorHighlighter);
                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                        jEditor.repaint();
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

            int caretPosition = jEditor.getCaretPosition();
            Element root = jEditor.getDocument().getDefaultRootElement();
            int lineNum = root.getElementIndex(caretPosition);


            for (Iterator<Error> iterator = errors.iterator(); iterator.hasNext(); ) {
                Error error = iterator.next();
                if (error.line == lineNum) {
                    boolean matchFound = false;

                    for (RuleMatch match : matches) {
                        int startOffset = element.getStartOffset() + match.getFromPos();
                        int endOffset = element.getStartOffset() + match.getToPos();

                        if (startOffset == error.getStartOffset() && endOffset == error.getEndOffset()) {
                            matchFound = true;
                            break;
                        }
                    }


                    if (!matchFound) {
                        SwingUtilities.invokeLater(() -> {
                            jEditor.getHighlighter().removeHighlight(error.highlight);
                            jEditor.repaint();
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






        /*
        {





            //Removing stale Highlights
            {


                for (Iterator<Error> iterator = errors.iterator(); iterator.hasNext(); ) {
                    Error error = iterator.next();

                    int caretPosition = jEditor.getCaretPosition();
                    Element root = jEditor.getDocument().getDefaultRootElement();

                    int line = root.getElementIndex(caretPosition);


                    //Leave errors from other lines intact, this doesn't do much
                    //to stop invalidating errors that are on the same "line"
                    if (line != error.line) continue;


                    if (!isValidError(matches, error, element)) {

                        SwingUtilities.invokeLater(() -> {
                            jEditor.getHighlighter().removeHighlight(error.highlight);
                            jEditor.repaint();
                        });
                        iterator.remove();
                    }
                }
            }
        }



         */


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

    public List<Error> getErrors() {
        return errors;
    }
}
