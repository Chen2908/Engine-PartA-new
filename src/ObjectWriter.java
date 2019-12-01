import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ObjectWriter {

    //<editor-fold des="Fields & Constructor">

    private String dirPath;
    private FilesWriter filesWriter;
    private ExecutorService threadPool;
    CountDownLatch latch = new CountDownLatch(5);

    public ObjectWriter(String directoryPath, int poolSize){
        filesWriter = new FilesWriter();
        threadPool = Executors.newCachedThreadPool();
        dirPath = directoryPath;
    }

    //</editor-fold>

    //<editor-fold des="Writing Functions">

    //<editor-fold des="OverLoading Functions">

    public void write(IWritable toWrite, String subFilePath){
        write(toWrite, subFilePath, false);
    }

    public void write(List<IWritable> toWrite, String subFilePath){
        write(toWrite, subFilePath, false);
    }

    //</editor-fold>

    public void write(IWritable toWrite, String subFilePath, boolean toAppend){
        this.filesWriter.addFilesToWrite(dirPath + subFilePath, toWrite.toFile(), toAppend);
        start();
    }

    public void write(List<IWritable> toWrite, String subFilePath, boolean toAppend){
        this.filesWriter.addFilesToWrite(dirPath + subFilePath, getCombinedList(toWrite), toAppend);
        start();
    }

    public void update(IWritable toWrite, String subFilePath) {
        List<String> toUpdate = readFile(subFilePath);

        if (toUpdate == null || toUpdate.size() == 0)
            return;

        this.filesWriter.addFilesToWrite(dirPath + subFilePath, toWrite.update(toUpdate), false);

        start();
    }

    //</editor-fold>

    //<editor-fold des="Inner Methods">

    private void start(){
        threadPool.execute(filesWriter);
    }

    private List<String> getCombinedList(List<IWritable> toCombine){
        List<String> combined = toCombine.remove(0).toFile();
        for (IWritable w: toCombine)
            combined.addAll(w.toFile());
        return combined;
    }

    private List<String> readFile(String subPath){
        List<String> lines = new ArrayList<>();

        try {
            File file = new File(dirPath + subPath);
            if (!file.exists())
                return null;

            filesWriter.acquire(dirPath + subPath);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "";

            while ((line = reader.readLine()) != null)
                lines.add(line);

            reader.close();
            filesWriter.release(dirPath + subPath);

            return lines;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close(){
        threadPool.shutdown();
        while (!threadPool.isTerminated()){}
    }

    //</editor-fold>

}
