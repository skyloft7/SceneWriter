package custom;

import thirdparty.ComponentResizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class JDockableWindow extends JPanel {


    private Object myDockspacePos;
    private int myDockDistance = 0;

    private Dimension vPreferredSize, hPreferredSize;
    private int amnestySize = 50;

    //Test replacement docking with axis-specific sizes

    public JDockableWindow(String title){
        //NOTE(Shayan): this might compete with getPreferredSize() that a user has already set
        vPreferredSize = new Dimension(getPreferredSize().width, getPreferredSize().height + amnestySize);
        hPreferredSize = new Dimension(getPreferredSize().width + amnestySize, getPreferredSize().height);

        ComponentResizer componentResizer = new ComponentResizer();



        componentResizer.registerComponent(this, resizeAxis -> {
            if(resizeAxis == ResizeListener.ResizeAxis.HORIZONTAL){
                hPreferredSize = getSize();
            }
            if(resizeAxis == ResizeListener.ResizeAxis.VERTICAL){
                vPreferredSize = getSize();
            }
        });





        setLayout(new BorderLayout());

        setBorder(BorderFactory.createTitledBorder(title));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                if(getParent() instanceof JDockspace){
                    ((JDockspace) getParent()).showOverlay();
                }

            }
        });

        addMouseListener(new MouseAdapter() {

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
                    }
                    //West
                    if(p.getX() < myDockDistance){
                        setPreferredSize(hPreferredSize);
                    }

                    //North
                    if(p.getY() < myDockDistance){
                        setPreferredSize(vPreferredSize);
                    }
                    //South
                    if(p.getY() > parent.getBounds().height - myDockDistance){
                        setPreferredSize(vPreferredSize);
                    }





                }
                else {

                    JDialog jDialog = new JDialog();
                    jDialog.setTitle(title);



                    parent.remove(JDockableWindow.this);
                    parent.revalidate();
                    parent.repaint();

                    jDialog.setLayout(new BorderLayout());
                    jDialog.add(JDockableWindow.this);
                    jDialog.setSize(getSize());
                    jDialog.setVisible(true);

                    jDialog.setLocation(MouseInfo.getPointerInfo().getLocation());


                    jDialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {

                            JDockableWindow.this.setPreferredSize(jDialog.getSize());

                            parent.add(JDockableWindow.this, myDockspacePos);
                            parent.revalidate();
                            parent.repaint();
                        }
                    });

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

    public void setLayoutInfo(Object args) {
        this.myDockspacePos = args;
    }
}
