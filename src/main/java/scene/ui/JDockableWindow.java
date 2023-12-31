package scene.ui;

import thirdparty.ComponentResizer;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;


public class JDockableWindow extends JPanel {


    private Object myDockspacePos;
    private int myDockDistance = 0;
    private Dimension vPreferredSize, hPreferredSize, windowPreferredSize;
    private int amnestySize = 50;
    private String title;
    private JDialog window;
    private boolean windowMode;

    private ResizeListener resizeListener = resizeAxis -> {
        if (resizeAxis == ResizeListener.ResizeAxis.HORIZONTAL) hPreferredSize = getSize();
        if (resizeAxis == ResizeListener.ResizeAxis.VERTICAL) vPreferredSize = getSize();
    };

    public JDockableWindow(String title){
        this.title = title;
        //NOTE(Shayan): this might compete with getPreferredSize() that a user has already set
        vPreferredSize = new Dimension(getPreferredSize().width, getPreferredSize().height + amnestySize);
        hPreferredSize = new Dimension(getPreferredSize().width + amnestySize, getPreferredSize().height);

        ComponentResizer componentResizer = new ComponentResizer();
        componentResizer.registerComponent(this, resizeListener);
        setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {

                if(isWindowMode()) return;


                Graphics2D graphics2D = (Graphics2D) g;



                graphics2D.setStroke(new BasicStroke(3));
                graphics2D.setColor(Color.LIGHT_GRAY);

                int offset = 5;


                if(getDockPos().equals(BorderLayout.WEST)){
                    graphics2D.drawLine(x + width, offset, x + width, y + height - offset);
                }



                if(getDockPos().equals(BorderLayout.EAST)){
                    graphics2D.drawLine(0, offset, 0, y + height - offset);
                }



                if(getDockPos().equals(BorderLayout.NORTH)){
                    graphics2D.drawLine(offset, y + height, x + width - offset, y + height);
                }
                if(getDockPos().equals(BorderLayout.SOUTH)){
                    graphics2D.drawLine(offset, 0, x + width - offset, 0);
                }




            }

            @Override
            public Insets getBorderInsets(Component c) {

                Insets insets = new Insets(0, 0, 0, 0);
                int inset = 3;

                if(getDockPos().equals(BorderLayout.WEST)) insets.right = inset;
                if(getDockPos().equals(BorderLayout.EAST)) insets.left = inset;
                if(getDockPos().equals(BorderLayout.NORTH)) insets.bottom = inset;
                if(getDockPos().equals(BorderLayout.SOUTH)) insets.top = inset;


                return insets;
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });


        setLayout(new BorderLayout());

        JPanel dockHeader = new JPanel(new BorderLayout());
        dockHeader.add(new JLabel(getTitle()), BorderLayout.CENTER);
        dockHeader.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        dockHeader.setBackground(Color.LIGHT_GRAY);

        add(dockHeader, BorderLayout.NORTH);

        dockHeader.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                if(getParent() instanceof JDockspace){
                    ((JDockspace) getParent()).showOverlay();
                }

            }
        });
        dockHeader.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                getParent().requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);




                if(getParent() instanceof JDockspace){
                    ((JDockspace) getParent()).hideOverlay();
                }

                Container parent = getParent();
                Point p = SwingUtilities.convertPoint(JDockableWindow.this, e.getPoint(), parent);



                if(p.x >= 0 && p.x <= parent.getWidth() && p.y >= 0 && p.y <= parent.getHeight()){



                    //East
                    if(p.getX() > parent.getBounds().width - myDockDistance){
                        setPreferredSize(hPreferredSize);
                        setPosition(parent, BorderLayout.EAST);
                    }
                    //West
                    if(p.getX() < myDockDistance){
                        setPreferredSize(hPreferredSize);
                        setPosition(parent, BorderLayout.WEST);
                    }

                    //North
                    if(p.getY() < myDockDistance){
                        setPreferredSize(vPreferredSize);
                        setPosition(parent, BorderLayout.NORTH);
                    }
                    //South
                    if(p.getY() > parent.getBounds().height - myDockDistance){
                        setPreferredSize(vPreferredSize);
                        setPosition(parent, BorderLayout.SOUTH);
                    }





                }
                else {

                    if(!windowMode) {
                        window = new JDialog((Window) null);

                        window.setTitle(title);


                        parent.remove(JDockableWindow.this);
                        parent.revalidate();
                        parent.repaint();



                        windowPreferredSize = getPreferredSize();
                        window.setSize(windowPreferredSize);

                        window.addComponentListener(new ComponentAdapter() {
                            @Override
                            public void componentResized(ComponentEvent e) {
                                windowPreferredSize = e.getComponent().getSize();
                            }
                        });

                        Dimension oldSize = getPreferredSize();


                        remove(dockHeader);
                        revalidate();
                        repaint();

                        window.setLayout(new BorderLayout());
                        window.add(JDockableWindow.this);
                        window.setVisible(true);
                        window.setLocation(MouseInfo.getPointerInfo().getLocation());

                        JDockspace.getAllFloatingWindows().add(JDockableWindow.this);


                        window.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {

                                add(dockHeader, BorderLayout.NORTH);
                                revalidate();
                                repaint();

                                JDockableWindow.this.setPreferredSize(window.getSize());

                                componentResizer.registerComponent(JDockableWindow.this, resizeListener);

                                setPreferredSize(oldSize);
                                parent.add(JDockableWindow.this, myDockspacePos);
                                parent.revalidate();
                                parent.repaint();

                                windowMode = false;

                                JDockspace.getAllFloatingWindows().remove(JDockableWindow.this);
                            }
                        });

                        windowMode = true;
                        componentResizer.deregisterComponent(JDockableWindow.this);
                    }

                }




            }
        });


    }


    private void setPosition(Container parent, Object newDockspacePos){

        if(parent instanceof JDockspace){
            JDockspace jDockspace = (JDockspace) parent;

            //Is there another component already here?
            JDockableWindow existingWindow = (JDockableWindow) ((BorderLayout) jDockspace.getLayout()).getLayoutComponent(newDockspacePos);

            if(existingWindow != null){

                if(myDockspacePos == BorderLayout.NORTH || myDockspacePos == BorderLayout.SOUTH)
                    existingWindow.setPreferredSize(existingWindow.getVerticallyDockedPreferredSize());
                if(myDockspacePos == BorderLayout.WEST || myDockspacePos == BorderLayout.EAST)
                    existingWindow.setPreferredSize(existingWindow.getHorizontallyDockedPreferredSize());


                parent.remove(existingWindow);
                parent.add(existingWindow, myDockspacePos);
                existingWindow.myDockspacePos = myDockspacePos;

            }
        }

        if(newDockspacePos == BorderLayout.NORTH || newDockspacePos == BorderLayout.SOUTH)
            setPreferredSize(getVerticallyDockedPreferredSize());
        if(newDockspacePos == BorderLayout.WEST || newDockspacePos == BorderLayout.EAST)
            setPreferredSize(getHorizontallyDockedPreferredSize());







        parent.remove(this);
        parent.add(this, newDockspacePos);
        myDockspacePos = newDockspacePos;
        parent.revalidate();
        parent.repaint();
    }
    public void setRequiredDockDistance(int myDockDistance) {
        this.myDockDistance = myDockDistance;
    }

    private void setEnabledRecursively(Component component, boolean enabled) {
        if(!(component instanceof JDockableWindow))
            component.setEnabled(enabled);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setEnabledRecursively(child, enabled);
            }
        }
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    public Dimension getVerticallyDockedPreferredSize() {
        return vPreferredSize;
    }

    public Dimension getHorizontallyDockedPreferredSize() {
        return hPreferredSize;
    }

    @Override
    public void setEnabled(boolean enabled) {
        setEnabledRecursively(this, enabled);
    }

    public void setDockPos(Object args) {
        this.myDockspacePos = args;
    }

    public Object getDockPos() {
        return myDockspacePos;
    }

    public boolean isWindowMode() {
        return windowMode;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }


    public JDialog getWindow() {
        return window;
    }

    public void dockOver() {

    }
}
