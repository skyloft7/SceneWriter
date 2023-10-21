package scene.ui;

import scene.app.SceneManager;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class ErrorHighlightPainter implements Highlighter.HighlightPainter {

    public ErrorHighlightPainter() {

    }

    @Override
    public void paint(Graphics g, int p0, int p1, Shape shape, JTextComponent c) {
        Rectangle2D r1, r2;

        try {
            r1 = c.modelToView2D(p0);
            r2 = c.modelToView2D(p1);

            Rectangle2D highlight = r1.createUnion(r2);

            Graphics2D graphics2D = (Graphics2D) g.create();
            graphics2D.setStroke(new BasicStroke(1.3f));
            graphics2D.setColor(SceneManager.isZenMode() ? Color.DARK_GRAY : Color.YELLOW);

            Rectangle2D bounds = ((Rectangle) shape).getBounds2D();

            //Same line
            if(r1.getY() == r2.getY()){
                graphics2D.drawLine(
                        (int) highlight.getX(),
                        (int) (highlight.getY() + highlight.getHeight()),
                        (int) (highlight.getX() + highlight.getWidth()),
                        (int) (highlight.getY() + highlight.getHeight()));
            }
            else {

                graphics2D.setColor(new Color(234, 128, 252));
                graphics2D.drawLine(
                        (int) r1.getX(),
                        (int) ((int) r1.getY() + r1.getHeight()),
                        (int) (bounds.getWidth()),
                        (int) ((int) r1.getY() + r1.getHeight())
                );


                graphics2D.drawLine(
                        (int) bounds.getX(),
                        (int) ((int) r2.getY() + r2.getHeight()),
                        (int) r2.getX(),
                        (int) ((int) r2.getY() + r2.getHeight()));





            }




        }
        catch (BadLocationException e){
            //The given highlight might not be valid anymore because Spellchecker
            //is constantly recomputing them, just return
            return;
        }

    }
}
