package Model.Indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObjectWriter {

    //<editor-fold "Fields & Constructor">

    private FilesWriter filesWriter;
    private ExecutorService threadPool;

    /**
     * Constructor
     * @param poolSize - number of threads
     */
    public ObjectWriter(int poolSize){
        filesWriter = new FilesWriter();
        threadPool = Executors.newFixedThreadPool(poolSize);
    }

    //</editor-fold>

    //<editor-fold des="Writing Functions">

    //<editor-fold des="OverLoading Functions">

    /**
     * This method gets an object to write and a path
     * and writes the object at the given path
     * @param toWrite - IWritable
     * @param filePath - file path
     */
    public void write(IWritable toWrite, String filePath){
        write(toWrite, filePath, false);
    }

    /**
     * This method gets a List of Strings to write and a path
     * and writes the object at the given path
     * @param toWrite - List of Strings
     * @param filePath - file path
     */
    public void write(List<StringBuilder> toWrite, String filePath){
        write(toWrite, filePath, false);
    }

    //</editor-fold>

    /**
     * This method gets an object to write and a path
     * and writes the object at the given path
     * @param toWrite - IWritable
     * @param filePath - file path
     */
    public void write(IWritable toWrite, String filePath, boolean toAppend){
        this.filesWriter.addFilesToWrite(filePath, toWrite.toFile(), toAppend);
        start();
    }

    /**
     * This method gets a List of Strings to write and a path
     * and writes the object at the given path
     * @param toWrite - List of Strings
     * @param filePath - file path
     */
    public void write(List<StringBuilder> toWrite, String filePath, boolean toAppend){
        this.filesWriter.addFilesToWrite(filePath, toWrite, toAppend);
        start();
    }

    //</editor-fold>

    //<editor-fold des="Inner Methods">

    /**
     * This method execute a thread that writes a file
     */
    private void start(){
        threadPool.execute(filesWriter);
    }

    /**
     * This method returns a list with the given file content
     * @param filePath - the file path to read
     * @return the content of the file
     */
    public List<StringBuilder> readFile(String filePath){

        List<StringBuilder> lines = new ArrayList<>();
        boolean tried = false;
        try {
            while(filesWriter.isInLine(filePath))
                if (!tried) {
                    lines = filesWriter.getFileToUpdate(filePath);
                    tried = true;
                }

            File file = new File(filePath);
            if (!file.exists() || lines.size() > 0)
                return lines;

            filesWriter.acquire(filePath);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "";

            while ((line = reader.readLine()) != null)
                lines.add(new StringBuilder(line));

            reader.close();
            filesWriter.release(filePath);

            return lines;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method closes the thread pool
     */
    public void close(){
        boolean firstTime = true;
        while (filesWriter.numOfFilesToWrite() > 0)
            if (firstTime) {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                firstTime = false;
            }
        threadPool.shutdown();
        while (!threadPool.isTerminated()){}
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    }

    //</editor-fold>

}
