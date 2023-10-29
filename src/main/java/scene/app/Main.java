package scene.app;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatGradiantoDeepOceanIJTheme;
import flatlaf.FlatLafUtils;
import scene.ui.JDockableWindow;
import scene.ui.JDockspace;
import scene.ui.UIUtil;

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

            Serializer serializer = new Serializer();





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
            SceneManager.setFrame(jFrame);



            JMenuBar jMenuBar = new JMenuBar();
            {

                JMenu file = new JMenu("File");
                {


                    JMenuItem create = new JMenuItem("Create");
                    {
                        create.addActionListener(e -> Workspaces.get("Source")
                                .getSignal(EditorPanel.OpenFileSignal.class)
                                .openNew());

                    }
                    file.add(create);

                    JMenuItem open = new JMenuItem("Open");
                    {
                        open.addActionListener(e -> Workspaces.get("Source")
                                .getSignal(EditorPanel.OpenFileSignal.class)
                                .open());
                    }
                    file.add(open);



                }
                jMenuBar.add(file);

                JMenu settings = new JMenu("Settings");
                {
                    JCheckBoxMenuItem darkMode = new JCheckBoxMenuItem("Dark Mode");
                    {
                        darkMode.setSelected(FlatLaf.isLafDark());
                        darkMode.addActionListener(e -> {

                            if (darkMode.isSelected()) FlatGradiantoDeepOceanIJTheme.setup();
                            else FlatLightLaf.setup();

                            UIUtil.updateComponentTreeUI(jFrame);
                            jFrame.repaint();
                        });


                    }
                    settings.add(darkMode);

                    JCheckBoxMenuItem zenMode = new JCheckBoxMenuItem("Zen Mode");
                    {

                        zenMode.addActionListener(e -> {

                            SceneManager.setZenMode(zenMode.isSelected());

                            GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
                            graphicsDevice.setFullScreenWindow(SceneManager.isZenMode() ? jFrame : null);
                        });
                    }
                    settings.add(zenMode);
                }


                jMenuBar.add(settings);


            }


            jFrame.setJMenuBar(jMenuBar);


            JDockspace jDockspace = new JDockspace();
            {

                JDockableWindow sourceEditor = new EditorPanel();




                //Overrides preferredSize
                jDockspace.addWindow(sourceEditor, BorderLayout.WEST);

                sourceEditor.setPreferredSize(new Dimension(sourceEditor.getPreferredSize().width + 400, sourceEditor.getPreferredSize().height));

            }
            jFrame.add(jDockspace);


            serializer.restore(jDockspace);



            jFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    serializer.save(jDockspace);
                }
            });









            jFrame.setSize(980, 580);
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrame.setVisible(true);


        });
    }
}