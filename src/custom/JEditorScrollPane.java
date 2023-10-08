package custom;

import com.formdev.flatlaf.ui.FlatRoundBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class JEditorScrollPane extends JScrollPane {

    private PopupFactory popupFactory = new PopupFactory();
    private Popup popup = null;
    private BufferedImage previewImage;
    private JLabel previewComponent = new JLabel();
    private JEditor editor;

    public JEditorScrollPane(JEditor jEditor, int vsbPolicy, int hsbPolicy) {
        super(jEditor, vsbPolicy, hsbPolicy);
        this.editor = jEditor;


        previewComponent.setBorder(new FlatRoundBorder());



        getVerticalScrollBar().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);


                Point p = new Point(0, 0);
                SwingUtilities.convertPointToScreen(p, JEditorScrollPane.this);

                render(jEditor, e.getY());
                popup = popupFactory.getPopup(JEditorScrollPane.this, previewComponent, p.x, p.y);
                popup.show();

            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);


                if(popup != null)
                    popup.hide();
            }
        });
        getVerticalScrollBar().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int offset = e.getY();
                render(jEditor, offset);
            }
        });



    }

    public void render(JEditor jEditor, int offset){
        Rectangle view = new Rectangle(0, 0, jEditor.getWidth(), 100);


        //FIXME: This is really expensive on memory
        previewImage = new BufferedImage(view.width, view.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = previewImage.createGraphics();

        float scaleF = (float) editor.getHeight() / getVerticalScrollBar().getHeight();
        g.translate(0, -offset * scaleF);

        jEditor.paint(g);
        g.dispose();


        previewComponent.setIcon(new ImageIcon(previewImage));
        previewComponent.repaint();
    }

    
}
