package scene.ui;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class FileChoosers {
    private FileChoosers(){}

    private static File currentPath = FileSystemView.getFileSystemView().getHomeDirectory();

    public static File showOpenDialog(){
        JFileChooser jFileChooser = new JFileChooser(currentPath);

        if(jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            currentPath = jFileChooser.getSelectedFile().getParentFile();
            return jFileChooser.getSelectedFile();
        }

        return null;
    }
}
