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
import java.util.List;

public class AnalyticASpellchecker extends ASpellchecker {


    public AnalyticASpellchecker(JEditor editor) {
        super(editor);
    }

    public void start(JEditor jEditor){
        SwingWorker spellcheckThread = new SwingWorker() {
            @Override
            protected Object doInBackground() {
                JLanguageTool languageTool = new JLanguageTool(Languages.getLanguageForShortCode("en-GB"));
                while(true) AnalyticASpellchecker.this.run(jEditor, languageTool);
            }
        };



        spellcheckThread.execute();
    }





    private void run(JEditor jEditor, JLanguageTool languageTool){

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



                    if (!editor.getErrors().contains(error)) {
                        try {
                            error.highlight = (Highlighter.Highlight) jEditor.getHighlighter().addHighlight(start, end, errorHighlighter);
                            jEditor.repaint();


                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                        editor.getErrors().add(error);
                    }
                });


                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        //Removing stale Highlights
        {
            List<RuleMatch> finalMatches = matches;

            SwingUtilities.invokeLater(() -> editor.getErrors().removeIf(error -> {


                for(RuleMatch match : finalMatches){
                    if(error.startOffset == match.getFromPos() && error.endOffset == match.getToPos()) return false;
                }

                System.out.println("someone leave");



                return true;
            }));





        }
    }
}
