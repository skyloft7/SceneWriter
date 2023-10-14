package scene.ui;

import flatlaf.FlatLafUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class JDockspace extends JPanel {
    private boolean showOverlay;
    private int dockDistance = 50;

    private Map<String, JDockableWindow> windows = new HashMap();

    public JDockspace() {
        setLayout(new BorderLayout());
    }

    public void addWindow(JDockableWindow jDockableWindow, Object args) {
        jDockableWindow.setDockPos(args);
        add(jDockableWindow, args);
        jDockableWindow.setRequiredDockDistance(dockDistance);
        windows.put(jDockableWindow.getTitle(), jDockableWindow);
    }


    public void showOverlay() {
        showOverlay = true;
        repaint();
    }

    public void hideOverlay() {
        showOverlay = false;
        repaint();
    }

    private AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f);
    private AlphaComposite hover = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);

        Graphics2D graphics2D = (Graphics2D) graphics.create();

        if(showOverlay) {

            graphics2D.setColor(FlatLafUtils.accentColor);
            graphics2D.setComposite(alphaComposite);



            //East
            Rectangle east = new Rectangle(getWidth() - dockDistance, (getHeight() / 2 - dockDistance / 2), dockDistance, dockDistance);
            graphics2D.fill(east);
            //West
            Rectangle west = new Rectangle(0, (getHeight() / 2 - dockDistance / 2), dockDistance, dockDistance);
            graphics2D.fill(west);
            //North
            Rectangle north = new Rectangle((getWidth() / 2 - dockDistance / 2), 0, dockDistance, dockDistance);
            graphics2D.fill(north);
            //South
            Rectangle south = new Rectangle((getWidth() / 2 - dockDistance / 2), (getHeight() - dockDistance), dockDistance, dockDistance);
            graphics2D.fill(south);



            //"Place Here" Indicator
            graphics2D.setComposite(hover);
            graphics2D.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

            if(east.contains(getMousePosition())){
                graphics2D.drawLine(getWidth() - dockDistance, (getHeight() / 2 - dockDistance / 2), getWidth() - dockDistance, (getHeight() / 2 - dockDistance / 2) + dockDistance);
            }
            if(west.contains(getMousePosition())){
                graphics2D.drawLine(dockDistance, (getHeight() / 2 - dockDistance / 2), dockDistance, (getHeight() / 2 - dockDistance / 2) + dockDistance);
            }
            if(north.contains(getMousePosition())){
                graphics2D.drawLine((getWidth() / 2 - dockDistance / 2), dockDistance, (getWidth() / 2 - dockDistance / 2) + dockDistance, dockDistance);
            }
            if(south.contains(getMousePosition())){
                graphics2D.drawLine((getWidth() / 2 - dockDistance / 2), getHeight() - dockDistance, (getWidth() / 2 - dockDistance / 2) + dockDistance, getHeight() - dockDistance);
            }




        }

        graphics2D.dispose();
    }

    public Map<String, JDockableWindow> getWindows() {
        return windows;
    }
}
