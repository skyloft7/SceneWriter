package custom;

import thirdparty.ComponentResizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class JDockableWindow extends JPanel {


    private Object layoutArgs;
    private int myDockDistance = 0;

    public JDockableWindow(String title){

        ComponentResizer componentResizer = new ComponentResizer();
        componentResizer.registerComponent(this);
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
                        setPosition(parent, BorderLayout.EAST);
                    }
                    //West
                    if(p.getX() < myDockDistance){
                        setPosition(parent, BorderLayout.WEST);
                    }

                    //North
                    if(p.getY() < myDockDistance){
                        setPosition(parent, BorderLayout.NORTH);
                    }
                    //South
                    if(p.getY() > parent.getBounds().height - myDockDistance){
                        setPosition(parent, BorderLayout.SOUTH);
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

                            parent.add(JDockableWindow.this, layoutArgs);
                            parent.revalidate();
                            parent.repaint();
                        }
                    });

                }




            }
        });
    }

    private void setPosition(Container parent, Object args){
        parent.remove(this);
        parent.add(this, args);
        layoutArgs = args;
        parent.revalidate();
        parent.repaint();
    }



    public int getMyDockDistance() {
        return myDockDistance;
    }

    public void setRequiredDockDistance(int myDockDistance) {
        this.myDockDistance = myDockDistance;
    }

    public void setEnabledRecursively(Component component, boolean enabled) {
        if(!(component instanceof JDockableWindow))
            component.setEnabled(enabled);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setEnabledRecursively(child, enabled);
            }
        }
    }



    @Override
    public void setEnabled(boolean enabled) {
        setEnabledRecursively(this, enabled);
    }

    public void setLayoutInfo(Object args) {
        this.layoutArgs = args;
    }
}
