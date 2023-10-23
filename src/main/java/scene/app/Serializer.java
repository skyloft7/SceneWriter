package scene.app;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatGradiantoDeepOceanIJTheme;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import scene.ui.JDockableWindow;
import scene.ui.JDockspace;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Serializer {

    public void save(JDockspace jDockspace){
        //TODO: Move this to EDT


        try {
            YamlFile preferencesFile = new YamlFile("Settings.yaml");
            preferencesFile.createNewFile(true);



            preferencesFile.set("DarkMode", FlatLaf.isLafDark());
            ConfigurationSection dockspacePrefs = preferencesFile.createSection("Dockspace");

            for(JDockableWindow jDockableWindow : jDockspace.getWindows().values()){
                ConfigurationSection windowPrefs = dockspacePrefs.createSection(jDockableWindow.getTitle());
                windowPrefs.set("DockPosition", jDockableWindow.getDockPos());
                windowPrefs.set("Width", jDockableWindow.getSize().width);
                windowPrefs.set("Height", jDockableWindow.getSize().height);
            }

            ConfigurationSection projectInfo = preferencesFile.createSection("Files");
            projectInfo.set("Project", SceneManager.getFile().getAbsolutePath());
            projectInfo.set("FilePath", FileChoosers.getCurrentPath().getAbsolutePath());



            preferencesFile.save();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void restore(JDockspace jDockspace){
        YamlFile preferencesFile = new YamlFile("Settings.yaml");

        if(!preferencesFile.exists()) return;


        try {
            preferencesFile.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(preferencesFile.exists()){


            boolean darkMode = preferencesFile.getBoolean("DarkMode");
            {

                if (darkMode) FlatGradiantoDeepOceanIJTheme.setup();
                else FlatLightLaf.setup();

                Window window = SwingUtilities.getWindowAncestor(jDockspace);
                SwingUtilities.updateComponentTreeUI(window);
                window.repaint();
            }

            ConfigurationSection dockspace = preferencesFile.getConfigurationSection("Dockspace");
            for(JDockableWindow jDockableWindow : jDockspace.getWindows().values()){
                ConfigurationSection c = dockspace.getConfigurationSection(jDockableWindow.getTitle());


                Object dockPos = c.get("DockPosition");
                jDockableWindow.setPreferredSize(new Dimension(c.getInt("Width"), c.getInt("Height")));

                jDockspace.remove(jDockableWindow);
                jDockspace.add(jDockableWindow, dockPos);
                jDockspace.revalidate();
                jDockspace.repaint();

                jDockableWindow.setDockPos(dockPos);




            }

            ConfigurationSection projectInfo = preferencesFile.getConfigurationSection("Files");
            File projectFile = new File(projectInfo.getString("Project"));

            System.out.println(projectFile);

            if(projectFile.exists()) {
                SceneManager.setFile(projectFile);
                Workspaces.get("Source").getSignal(EditorPanel.OpenFileSignal.class).open(projectFile);
            }

            File currentPath = new File(projectInfo.getString("FilePath"));
            if(currentPath.exists()) FileChoosers.setCurrentPath(currentPath);




        }
    }



}
