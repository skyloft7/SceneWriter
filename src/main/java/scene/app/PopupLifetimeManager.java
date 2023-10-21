package scene.app;

import scene.app.PopupContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;

public class PopupLifetimeManager {
    private HashMap<String, PopupContext> popups = new HashMap<>();

    public void install(JComponent j){
        j.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for(PopupContext c : popups.values()){
                    Point p = SwingUtilities.convertPoint(j, e.getPoint(), c.component);

                    if(!c.component.contains(p)){
                        if(c.popupListener != null) c.popupListener.popupClosing(c.popup);
                        c.popup.hide();
                    }
                }
            }
        });
    }

    public void register(String id, PopupContext popupContext) {
        popups.put(id, popupContext);
    }
}
