package scene.app;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import flatlaf.FlatLafUtils;
import scene.markdown.MarkdownFileFilter;
import scene.ui.FileChoosers;
import scene.ui.JDockableWindow;
import scene.ui.JDockspace;
import scene.ui.MenuAdapter;
import scene.ui.diagram.JDiagramEditor;

import javax.swing.*;
import javax.swing.event.MenuEvent;
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




            FlatLightLaf.setup();
            FlatLafUtils.setup();

            UIManager.put("RootPane.background", Color.BLACK);
            UIManager.put("MenuBar.foreground", Color.WHITE);

            UIManager.put("TitlePane.foreground", Color.WHITE);
            UIManager.put("TitlePane.embeddedForeground", Color.WHITE);

            UIManager.put("TitlePane.buttonHoverBackground", FlatLafUtils.accentColor);
            UIManager.put("MenuBar.hoverBackground", FlatLafUtils.accentColor);
            UIManager.put("TitlePane.buttonPressedBackground", FlatLafUtils.accentColor.darker());


            JFrame jFrame = new JFrame("Scene");



            JMenuBar jMenuBar = new JMenuBar();
            {

                JMenu open = new JMenu("Open");
                {

                    open.addMenuListener(new MenuAdapter(){
                        @Override
                        public void menuSelected(MenuEvent e) {
                            Workspaces.get("Source")
                                    .getSignal(EditorPanel.OpenFileSignal.class)
                                    .open(FileChoosers.showOpenDialog(MarkdownFileFilter.filter));

                        }
                    });
                }
                jMenuBar.add(open);

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

                JDockableWindow sourceEditor = new EditorPanel(), characterMap = new JDockableWindow("Character Map");

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