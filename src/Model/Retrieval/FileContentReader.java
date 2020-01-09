package Model.Retrieval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileContentReader {

    public FileContentReader(){
    }

    public List<String> getFileContent(String path){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            List<String> lines = new ArrayList<>();
            String line = reader.readLine();

            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }

            reader.close();
            return lines;

        } catch (IOException e) {
            return null;
        }
    }
}
