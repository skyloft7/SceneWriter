package scene.markdown;

import scene.app.Files;

import java.io.File;

public class MarkdownWriter {
    public static void write(File file, String markdown){
        Files.saveFile(file, markdown);
    }
}
