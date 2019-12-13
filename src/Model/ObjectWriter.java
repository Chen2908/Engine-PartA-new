package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObjectWriter {

    //<editor-fold des="Fields & Constructor">

    private String dirPath;
    private FilesWriter filesWriter;
    private ExecutorService threadPool;

    public ObjectWriter(String directoryPath, int poolSize){
        filesWriter = new FilesWriter();
        threadPool = Executors.newFixedThreadPool(poolSize);
        dirPath = directoryPath;
    }

    //</editor-fold>

    //<editor-fold des="Writing Functions">

    //<editor-fold des="OverLoading Functions">

    public void write(IWritable toWrite, String filePath){
        write(toWrite, filePath, false);
    }

    public void write(List<StringBuilder> toWrite, String filePath){
        write(toWrite, filePath, false);
    }

    //</editor-fold>

    public void write(IWritable toWrite, String filePath, boolean toAppend){
        this.filesWriter.addFilesToWrite(filePath, toWrite.toFile(), toAppend);
        start();
    }

    public void write(List<StringBuilder> toWrite, String filePath, boolean toAppend){
        this.filesWriter.addFilesToWrite(filePath, toWrite, toAppend);
        start();
    }

    //</editor-fold>

    //<editor-fold des="Inner Methods">

    private void start(){
        threadPool.execute(filesWriter);
    }

    public List<StringBuilder> readFile(String filePath){

        List<StringBuilder> lines = new ArrayList<>();
        try {
            while (filesWriter.isInLine(filePath))
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            File file = new File(filePath);
            if (!file.exists())
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

    public void acquire(String filePath){
        filesWriter.acquire(filePath);
    }

    public void release(String filePath){
        filesWriter.release(filePath);
    }

    public void close(){
        threadPool.shutdown();
        while (!threadPool.isTerminated()){}
    }

    //</editor-fold>

}
