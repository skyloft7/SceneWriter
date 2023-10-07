package custom;

import thirdparty.ComponentResizer;

import javax.swing.*;
import java.awt.*;


public class JDockableWindow extends JToolBar {



    public JDockableWindow(String title){
        super(title);
        setFloatable(true);



        ComponentResizer componentResizer = new ComponentResizer();
        componentResizer.registerComponent(this);

        setBorder(BorderFactory.createTitledBorder(getName()));
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
}
