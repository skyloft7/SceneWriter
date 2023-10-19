package scene.app;

import java.io.File;

public class AppManager {
    private static File file;

    public static File getFile() {
        return file;
    }

    public static void setFile(File file) {
        AppManager.file = file;
    }

}
