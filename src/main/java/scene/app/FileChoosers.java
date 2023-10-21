package scene.app;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class FileChoosers {
    private FileChoosers(){}

    private static File currentPath = FileSystemView.getFileSystemView().getHomeDirectory();

    public static File showOpenDialog(){
        return showOpenDialog(null);
    }

    public static File showSaveDialog(){
        return showSaveDialog(null);
    }

    public static File showOpenDialog(FileFilter fileFilter){
        JFileChooser jFileChooser = new JFileChooser(currentPath);

        if(fileFilter != null) jFileChooser.setFileFilter(fileFilter);

        if(jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            currentPath = jFileChooser.getSelectedFile().getParentFile();
            return jFileChooser.getSelectedFile();
        }

        return null;
    }

    public static File showSaveDialog(FileFilter fileFilter){
        JFileChooser jFileChooser = new JFileChooser(currentPath);

        if(fileFilter != null) jFileChooser.setFileFilter(fileFilter);

        if(jFileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            currentPath = jFileChooser.getSelectedFile().getParentFile();
            return jFileChooser.getSelectedFile();
        }

        return null;
    }
}
