import java.io.*;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ReadFile {

    private File mainDir;
    private List<File> files;

    private ExecutorService executorWriteFile;
    private WriteFile fileWriter;

    private BufferedReader curFileToRead;
    private List<String> nextFile;
    private List<String> nextFileCopy;

    private final String startOfDocStr = "<DOC>";
    private final String endOfDocStr = "</DOC>";

    public ReadFile(String dirPath) throws FileNotFoundException {

        this.files = new LinkedList<>();
        this.nextFile = new LinkedList<>();
        this.nextFileCopy = new LinkedList<>();
        this.executorWriteFile = Executors.newFixedThreadPool(5);

        getAllFiles(new File(dirPath));
        this.fileWriter = new WriteFile(Paths.get("").toAbsolutePath().toString() + "/corpusDocs");
        this.curFileToRead = new BufferedReader(new FileReader(this.files.remove(0)));

        System.out.println("");
    }

    private void getAllFiles(File file){
        if (file.isDirectory()){
            File[] dirFiles = file.listFiles();
            for (File curFileToRead : dirFiles)
                getAllFiles(curFileToRead);
        }
        else if (file.isFile()){
            files.add(file);
        }
    }

    public List<String> getNextFile() {
        try {
            saveNextFile();
            executorWriteFile.execute(fileWriter);
            return this.nextFileCopy;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.nextFileCopy;
    }

    private void saveNextFile() throws IOException, InterruptedException {

        String line = "";
        this.nextFile = new LinkedList<>();
        this.nextFileCopy = new LinkedList<>();
        // Finds the start of the next Document
        while (true){
            if ((line= this.curFileToRead.readLine()) == null) {
                this.curFileToRead = new BufferedReader(new FileReader(this.files.remove(0)));
                line= this.curFileToRead.readLine();
            }
            if (line.contains(this.startOfDocStr))
                break;
        }
        // Saves all the Document lines
        addStringToFileLists(line);
        while(!(line = this.curFileToRead.readLine()).contains(this.endOfDocStr))
            addStringToFileLists(line);
        addStringToFileLists(line);

        this.fileWriter.addLinesToWrite(this.nextFile);
    }

    private void addStringToFileLists(String s){
        this.nextFile.add(s);
        this.nextFileCopy.add(s);
    }

    public int getNumberOfFiles(){
        return this.files.size();
    }

    public void closePool(){
        this.executorWriteFile.shutdown();
    }

    public boolean isFinished(){
        return this.executorWriteFile.isTerminated();
    }

}
