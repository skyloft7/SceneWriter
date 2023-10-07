package custom;

import javax.swing.*;
import java.awt.*;

public class JDockspace extends JPanel {

    public JDockspace() {
        setLayout(new BorderLayout());

    }

    public void addWindow(JDockableWindow jDockableWindow, Object args) {
        add(jDockableWindow, args);
    }


}
