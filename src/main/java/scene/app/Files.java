package scene.app;

import java.io.*;

public class Files {
    public static String readText(File path){
        StringBuilder total = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new java.io.FileReader(path));

            String line = "";
            while((line = bufferedReader.readLine()) != null){

                total.append(line);
                total.append("\n");
            }



            return total.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveFile(File path, String text) {

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path));
            bufferedWriter.write(text);

            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
