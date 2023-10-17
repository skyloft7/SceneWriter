package scene.ui;

import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;

public abstract class ASpellchecker {
    public JEditor editor;
    public Highlighter.HighlightPainter errorHighlighter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);//new ErrorHighlightPainter();
    //public List<RuleMatch> matches;


    public ASpellchecker(JEditor editor) {
        this.editor = editor;
    }
}
