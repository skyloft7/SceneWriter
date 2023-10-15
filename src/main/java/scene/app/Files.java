package scene.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class Files {
    public static String readText(File path){
        String total = "";

        try {
            BufferedReader bufferedReader = new BufferedReader(new java.io.FileReader(path));

            String line = "";
            while((line = bufferedReader.readLine()) != null){
                total += line + "\n";
            }

            return total;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
