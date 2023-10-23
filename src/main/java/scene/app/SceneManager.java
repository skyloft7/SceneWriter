package scene.app;

import javax.swing.*;
import java.io.File;

public class SceneManager {
    private static File file;
    private static boolean zenMode;
    private static JFrame frame;

    public static File getFile() {
        return file;
    }
    public static void setFile(File file) {
        SceneManager.file = file;
    }
    public static boolean isZenMode() {
        return zenMode;
    }
    public static void setZenMode(boolean zenMode) {
        SceneManager.zenMode = zenMode;
    }


    public static void setFrame(JFrame frame) {
        SceneManager.frame = frame;
    }

    public static JFrame getFrame() {
        return frame;
    }
}
