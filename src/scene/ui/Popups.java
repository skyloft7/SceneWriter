package scene.ui;

import javax.swing.*;
import java.awt.*;

public class Popups {
    private static PopupFactory popupFactory = new PopupFactory();

    public static Popup create(Component owner, JComponent component, Point pos){
        Popup popup = popupFactory.getPopup(owner, component, pos.x, pos.y);
        return popup;
    }
}
