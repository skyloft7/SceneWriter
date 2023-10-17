package scene.ui;

import com.formdev.flatlaf.util.SystemInfo;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;
import scene.app.Error;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.List;

public class EverythingASpellchecker extends ASpellchecker {


    public EverythingASpellchecker(JEditor editor) {
        super(editor);
    }

    public void start(JEditor jEditor){

        String text = jEditor.getText();


        SwingWorker spellcheckThread = new SwingWorker() {
            @Override
            protected Object doInBackground() {
                JLanguageTool languageTool = new JLanguageTool(Languages.getLanguageForShortCode("en-GB"));

                EverythingASpellchecker.this.run(jEditor, languageTool, text);
                return null;
            }
        };


        spellcheckThread.execute();
    }

    private void run(JEditor jEditor, JLanguageTool languageTool, String text){
        //Shoot, LanguageTool doesn't recognize CRLF Windows line endings!

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




                SwingUtilities.invokeLater(() -> {
                    Error error = new Error(ruleMatch.getFromPos(), ruleMatch.getToPos(), ruleMatch.getMessage());
                    error.startOffsetDoc = ruleMatch.getFromPos();
                    error.endOffsetDoc = ruleMatch.getToPos();



                    error.line = 0;




                    if (!editor.getErrors().contains(error)) {

                        try {
                            error.highlight = (Highlighter.Highlight) jEditor.getHighlighter().addHighlight(error.startOffsetDoc, error.endOffsetDoc, errorHighlighter);
                            jEditor.repaint();


                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                        editor.getErrors().add(error);
                    }
                });

                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }

        }























    }
}
