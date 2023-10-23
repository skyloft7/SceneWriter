package scene.markdown;

import scene.app.Files;

import javax.swing.*;
import java.io.File;

public class MarkdownWriter {
    public static void write(File file, String markdown){

        SwingWorker<Nothing, Nothing> swingWorker = new SwingWorker<>() {
            @Override
            protected Nothing doInBackground() {
                Files.saveFile(file, markdown);
                return null;
            }
        };
        swingWorker.execute();


    }
}
