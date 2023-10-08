package custom;

import javax.swing.*;
import java.awt.*;

public class JDockspace extends JPanel {
    private boolean showOverlay;
    private int dockDistance = 50;


    public JDockspace() {
        setLayout(new BorderLayout());
    }

    public void addWindow(JDockableWindow jDockableWindow, Object args) {
        jDockableWindow.setLayoutInfo(args);
        add(jDockableWindow, args);
        jDockableWindow.setRequiredDockDistance(dockDistance);
    }


    public void showOverlay() {
        showOverlay = true;
        repaint();
    }

    private AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);

        Graphics2D graphics2D = (Graphics2D) graphics.create();

        if(showOverlay) {

            graphics2D.setColor(Color.CYAN);
            graphics2D.setComposite(alphaComposite);



            //East
            graphics2D.fillRect(getWidth() - dockDistance, (getHeight() / 2 - dockDistance / 2), dockDistance, dockDistance);
            //West
            graphics2D.fillRect(0, (getHeight() / 2 - dockDistance / 2), dockDistance, dockDistance);
            //North
            graphics2D.fillRect((getWidth() / 2 - dockDistance / 2), 0, dockDistance, dockDistance);
            //South
            graphics2D.fillRect((getWidth() / 2 - dockDistance / 2), (getHeight() - dockDistance), dockDistance, dockDistance);
        }

        graphics2D.dispose();
    }

    public void hideOverlay() {
        showOverlay = false;
        repaint();
    }
}
