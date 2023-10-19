package scene.app;

import java.io.File;

public class AppManager {
    private static File file;

    private static boolean zenMode;

    public static File getFile() {
        return file;
    }

    public static void setFile(File file) {
        AppManager.file = file;
    }

    public static boolean isZenMode() {
        return zenMode;
    }

    public static void setZenMode(boolean zenMode) {
        AppManager.zenMode = zenMode;
    }
}
