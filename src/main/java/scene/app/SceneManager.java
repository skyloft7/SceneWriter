package scene.app;

import java.io.File;

public class SceneManager {
    private static File file;
    private static boolean zenMode;
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







}
