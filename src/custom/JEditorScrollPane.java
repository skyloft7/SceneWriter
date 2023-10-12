package custom;

import com.formdev.flatlaf.ui.FlatRoundBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class JEditorScrollPane extends JScrollPane {

    private PopupFactory popupFactory = new PopupFactory();
    private Popup popup = null;
    private JLabel previewComponent = new JLabel();

    private PreviewImage previewImage;
    private JEditor editor;

    public JEditorScrollPane(JEditor jEditor, int vsbPolicy, int hsbPolicy) {
        super(jEditor, vsbPolicy, hsbPolicy);
        this.editor = jEditor;
        this.previewImage = new PreviewImage(jEditor);

        previewComponent.setIcon(this.previewImage);
        previewComponent.setBorder(new FlatRoundBorder());





        getVerticalScrollBar().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);


                Point p = new Point(0, 0);
                SwingUtilities.convertPointToScreen(p, JEditorScrollPane.this);

                previewImage.setPosition(e.getY());
                previewComponent.repaint();
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
                previewImage.setPosition(e.getY());
                previewComponent.repaint();
            }
        });



    }


    private class PreviewImage implements Icon {

        private int pos;

        private Component component;

        public PreviewImage(Component component) {
            this.component = component;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D graphics2D = (Graphics2D) g;
            float scaleF = (float) editor.getHeight() / getVerticalScrollBar().getHeight();
            graphics2D.translate(0, (int) (-pos * scaleF));
            component.paint(graphics2D);
        }

        @Override
        public int getIconWidth() {
            return component.getWidth();
        }

        @Override
        public int getIconHeight() {
            return 100;
        }

        public void setPosition(int y) {
            this.pos = y;
        }

        public int getPosition() {
            return pos;
        }
    }


}
