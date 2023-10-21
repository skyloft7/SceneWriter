package thirdparty;

import scene.ui.JDockableWindow;
import scene.ui.JDockspace;

import javax.swing.*;
import javax.swing.text.Element;

public class Utils {


    public static Element currentLine(JTextPane j){
        Element root = j.getDocument().getDefaultRootElement();
        int caret = j.getCaretPosition();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element e = root.getElement(i);
            if(caret >= e.getStartOffset() && caret <= e.getEndOffset()) return e;
        }
        return null;
    }

    public static void updateComponentTreeUI(JFrame topLevelFrame){
        SwingUtilities.updateComponentTreeUI(topLevelFrame);

        for(JDockableWindow window : JDockspace.getAllFloatingWindows()){
            if(window.getWindow() != null) SwingUtilities.updateComponentTreeUI(window.getWindow());
        }
    }






}
