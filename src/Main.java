import com.formdev.flatlaf.FlatDarkLaf;
import custom.JDockableWindow;
import custom.JDockspace;
import custom.JEditor;
import custom.designer.JDiagramEditor;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {


        SwingUtilities.invokeLater(() -> {

            System.setProperty("flatlaf.menuBarEmbedded", "false");
            UIManager.put("RootPane.background", new Color(0xFFCC99));
            UIManager.put("TitlePane.foreground", Color.BLACK);
            FlatDarkLaf.setup();





            JFrame jFrame = new JFrame("Scene");


            JDockspace jDockspace = new JDockspace();

            JDockableWindow a = new JDockableWindow("Test Window 1"), b = new JDockableWindow("Test Window 2");

            a.add(new JScrollPane(new JEditor(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
            b.add(new JDiagramEditor());

            jDockspace.addWindow(a, BorderLayout.WEST);


            b.setPreferredSize(new Dimension(b.getPreferredSize().width + 400, b.getPreferredSize().height));
            jDockspace.addWindow(b, BorderLayout.EAST);




            jFrame.add(jDockspace);

            jFrame.setSize(640, 480);
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrame.setVisible(true);

        });



        System.out.println("Hello world!");
    }
}