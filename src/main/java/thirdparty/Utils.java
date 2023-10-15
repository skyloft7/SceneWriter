package thirdparty;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

public class Utils {
    public static String currentLine(JTextPane textTx)
    {
        // Get section element
        Element section = textTx.getDocument().getDefaultRootElement();

        // Get number of paragraphs.
        // In a text pane, a span of characters terminated by single
        // newline is typically called a paragraph.
        int paraCount = section.getElementCount();

        int position = textTx.getCaret().getDot();

        // Get index ranges for each paragraph
        for (int i = 0; i < paraCount; i++)
        {
            Element e1 = section.getElement(i);

            int rangeStart = e1.getStartOffset();
            int rangeEnd = e1.getEndOffset();

            try
            {
                String para = textTx.getText(rangeStart, rangeEnd-rangeStart);

                if (position >= rangeStart && position <= rangeEnd)
                    return para;
            }
            catch (BadLocationException ex)
            {
                System.err.println("Get current line from editor error: " + ex.getMessage());
            }
        }
        return null;
    }
}
