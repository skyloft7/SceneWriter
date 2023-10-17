package scene.ui;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class ErrorHighlightPainter implements Highlighter.HighlightPainter {

    public ErrorHighlightPainter() {

    }

    @Override
    public void paint(Graphics g, int p0, int p1, Shape shape, JTextComponent c) {
        Graphics2D graphics2D = (Graphics2D) g.create();
        graphics2D.setStroke(new BasicStroke(1.5f));
        graphics2D.setColor(Color.RED);

        TextUI mapper = c.getUI();
        try {

            System.out.println("p0:" + p0);

            Rectangle r1 = mapper.modelToView(c, p0);
            Rectangle r2 = mapper.modelToView(c, p1);

            Rectangle bounds = r1.union(r2);

            graphics2D.drawLine(bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y + bounds.height);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }


    }
}
