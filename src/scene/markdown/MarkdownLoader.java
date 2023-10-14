package scene.markdown;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class MarkdownLoader {
    public static void load(String markdown, Document swingDocument) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);

        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Text text) {

                SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();

                Node parentNode = text.getParent();

                try {

                    if (parentNode instanceof Emphasis) StyleConstants.setItalic(simpleAttributeSet, true);
                    if (parentNode instanceof StrongEmphasis) StyleConstants.setBold(simpleAttributeSet, true);
                    if (parentNode instanceof Heading) {

                        int level = ((Heading) parentNode).getLevel();
                        int size = 0;

                        if (level == 1) size = 60;
                        if (level == 2) size = 40;
                        if (level == 3) size = 20;

                        StyleConstants.setFontSize(simpleAttributeSet, size);
                    }
                    if (parentNode instanceof Heading) {

                        int level = ((Heading) parentNode).getLevel();
                        int size = 0;

                        if (level == 1) size = 60;
                        if (level == 2) size = 40;
                        if (level == 3) size = 20;

                        StyleConstants.setFontSize(simpleAttributeSet, size);
                    }

                    if (parentNode instanceof HardLineBreak) {
                        swingDocument.insertString(swingDocument.getLength(), "\n", simpleAttributeSet);
                    }




                    swingDocument.insertString(swingDocument.getLength(), text.getLiteral(), simpleAttributeSet);
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }


                visitChildren(text);
            }
        });


    }
}
