package scene.ui;

import javax.swing.*;
import java.awt.*;

public class PopupContext {
    public Popup popup;
    public Component component;
    public PopupListener popupListener;

    public PopupContext(Popup popup, Component component) {
        this.popup = popup;
        this.component = component;
    }
    public PopupContext(Popup popup, Component component, PopupListener popupListener) {
        this.popup = popup;
        this.component = component;
        this.popupListener = popupListener;
    }

    public interface PopupListener {
        void popupClosing(Popup popup);
    }
}
