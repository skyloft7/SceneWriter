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

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);

        Graphics2D graphics2D = (Graphics2D) graphics.create();

        if(showOverlay) {

            graphics2D.setColor(FlatLafUtils.accentColor);
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

    public Map<String, JDockableWindow> getWindows() {
        return windows;
    }
}
