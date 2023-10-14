import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import custom.JDockableWindow;
import custom.JDockspace;
import custom.JEditor;
import custom.JEditorScrollPane;
import custom.diagram.JDiagramEditor;
import flatlaf.FlatLafUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {

        System.out.println("Scene - (Community Edition). Copyright 2023 Mohammed Baig");
        System.out.println("\tMachine Info: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("\tJVM Info: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));




        SwingUtilities.invokeLater(() -> {

            UILayoutSerializer uiLayoutSerializer = new UILayoutSerializer();

            UIManager.put("RootPane.background", Color.BLACK);
            UIManager.put("MenuBar.foreground", Color.WHITE);

            UIManager.put("TitlePane.foreground", Color.WHITE);
            UIManager.put("TitlePane.embeddedForeground", Color.WHITE);


            FlatLightLaf.setup();
            FlatLafUtils.setup();

            UIManager.put("TitlePane.buttonHoverBackground", FlatLafUtils.accentColor);


            JFrame jFrame = new JFrame("Scene");



            JMenuBar jMenuBar = new JMenuBar();
            {
                JMenu settings = new JMenu("Settings");
                {
                    JCheckBoxMenuItem darkMode = new JCheckBoxMenuItem("Dark Mode");
                    {
                        darkMode.setSelected(FlatLaf.isLafDark());
                        darkMode.addActionListener(e -> {

                            if (darkMode.isSelected()) FlatArcDarkIJTheme.setup();
                            else FlatLightLaf.setup();

                            SwingUtilities.updateComponentTreeUI(jFrame);
                            jFrame.repaint();
                        });


                    }
                    settings.add(darkMode);

                    JCheckBoxMenuItem zenMode = new JCheckBoxMenuItem("Zen Mode (WIP)");
                    {

                        zenMode.addActionListener(e -> {
                            GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
                            graphicsDevice.setFullScreenWindow(zenMode.isSelected() ? jFrame : null);
                        });
                    }
                    settings.add(zenMode);
                }
                jMenuBar.add(settings);

            }
            jFrame.setJMenuBar(jMenuBar);


            JDockspace jDockspace = new JDockspace();
            {

                JDockableWindow sourceEditor = new JDockableWindow("Source"), characterMap = new JDockableWindow("Character Map");

                sourceEditor.add(new JEditorScrollPane(new JEditor(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
                characterMap.add(new JDiagramEditor());


                //Overrides preferredSize
                jDockspace.addWindow(sourceEditor, BorderLayout.WEST);
                jDockspace.addWindow(characterMap, BorderLayout.EAST);

                sourceEditor.setPreferredSize(new Dimension(sourceEditor.getPreferredSize().width + 400, sourceEditor.getPreferredSize().height));

            }
            jFrame.add(jDockspace);


            uiLayoutSerializer.restore(jDockspace);



            jFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    uiLayoutSerializer.save(jDockspace);
                }
            });







            jFrame.setSize(980, 580);
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrame.setVisible(true);


        });
    }
}