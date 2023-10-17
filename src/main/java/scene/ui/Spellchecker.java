package scene.ui;

import com.formdev.flatlaf.util.SystemInfo;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;
import scene.app.Error;
import thirdparty.Utils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Spellchecker {

    private final SyncSpellcheckLock updateAllLock = new SyncSpellcheckLock();

    private String info = "";


    public Spellchecker(){

    }

    public void start(JEditor jEditor){
        SwingWorker swingWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {


                JLanguageTool languageTool = new JLanguageTool(Languages.getLanguageForShortCode("en-GB"));



                while(true){


                    //System.out.println("Before Wait: " + System.currentTimeMillis());

                    System.out.println("Start");

                    //stuck waiting here because now updateAllLock is owned by EDT
                    synchronized (updateAllLock){
                        if(!info.isEmpty()){

                            //System.out.println("Got message!");
                            spellcheckEverything(languageTool, jEditor, jEditor.getText());

                            info = "";
                        }
                    }

                    spellcheckLine(languageTool, jEditor);

                    System.out.println("End");


                }




                //return null;
            }
        };

        swingWorker.execute();




    }

    //FIXME: NOT ON EDT!!!!!
    private ArrayList<Error> errors = new ArrayList<>();

    public Highlighter.HighlightPainter errorHighlighter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);//new ErrorHighlightPainter();


    private void spellcheckEverything(JLanguageTool languageTool, JEditor jEditor, String text){
        if(SystemInfo.isWindows)
            text = text.replaceAll("\r\n", "\n");


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
                error.startOffsetDoc = ruleMatch.getFromPos();
                error.endOffsetDoc = ruleMatch.getToPos();

                if(!errors.contains(error)){
                    errors.add(error);

                    SwingUtilities.invokeLater(() -> {
                        try {
                            error.highlight = (Highlighter.Highlight) jEditor.getHighlighter().addHighlight(error.startOffsetDoc, error.endOffsetDoc, errorHighlighter);
                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                        jEditor.repaint();
                    });

                }


                //Thread.sleep() here?




            }

        }
    }

    private void spellcheckLine(JLanguageTool languageTool, JEditor jEditor) {

        String currentLineText = Utils.currentLine(jEditor);

        if(SystemInfo.isWindows)
            currentLineText = currentLineText.replaceAll("\r\n", "\n");




        List<RuleMatch> matches = null;
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
                    Element lineElement = root.getElement(line);

                    int start = lineElement.getStartOffset() + match.getFromPos();
                    int end = lineElement.getStartOffset() + match.getToPos();


                    Error error = new Error(match.getFromPos(), match.getToPos(), match.getMessage());
                    error.startOffsetDoc = start;
                    error.endOffsetDoc = end;
                    error.line = line;



                    if(!errors.contains(error)){
                        errors.add(error);

                        SwingUtilities.invokeLater(() -> {
                            try {
                                error.highlight = (Highlighter.Highlight) jEditor.getHighlighter().addHighlight(error.startOffsetDoc, error.endOffsetDoc, errorHighlighter);
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




                });




            }
        }

        //Removing stale Highlights
        {


            List<RuleMatch> finalMatches1 = matches;
            errors.removeIf(error -> {



                for(RuleMatch match : finalMatches1){
                    if(error.startOffset == match.getFromPos() && error.endOffset == match.getToPos()) return false;
                }



                SwingUtilities.invokeLater(() -> jEditor.getHighlighter().removeHighlight(error.highlight));

                return true;
            });





        }
    }

    public void updateAll(String entireText){
        synchronized (updateAllLock){
            info = entireText;
            updateAllLock.notifyAll();
        }
    }

    private class SyncSpellcheckLock {}
}
