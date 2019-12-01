import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;

public class FilesWriter implements Runnable {

    private static ConcurrentLinkedDeque<List<String>> lines;
    private static ConcurrentLinkedDeque<String> filesPath;
    private static ConcurrentLinkedDeque<Boolean> appendToFile;
    private static ConcurrentHashMap<String,Semaphore> fileStatus;
    private static Semaphore semaphore = new Semaphore(1);
    // Files Status options
    private final String WAITING = "Waiting";
    private final String READING = "Reading";
    private final String WRITING = "Writing";

    private static int count = 0;

    public FilesWriter() {
        reset();
    }

    public void reset(){
        lines = new ConcurrentLinkedDeque<>();
        filesPath = new ConcurrentLinkedDeque<>();
        appendToFile = new ConcurrentLinkedDeque<>();
        fileStatus = new ConcurrentHashMap<>();
    }

    public void addFilesToWrite(String filePath, List<String> toAdd, boolean append){
        try {
            semaphore.acquire();
            filesPath.addLast(filePath);
            lines.addLast(toAdd);
            appendToFile.addLast(append);
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addFilesToWrite(String filePath, List<String> lines){
        addFilesToWrite(filePath, lines, false);
    }

    public int numOfFilesToWrite(){
        return filesPath.size();
    }

    public void acquire(String file){
        try {
            if (!fileStatus.containsKey(file))
                fileStatus.put(file, new Semaphore(1));
            fileStatus.get(file).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release(String file){
        fileStatus.get(file).release();
    }

    @Override
    public void run() {

        try {
            semaphore.acquire();
            List<String> toWrite = lines.removeFirst();
            String filePath = filesPath.removeFirst();
            boolean toAppend = appendToFile.removeFirst();
            semaphore.release();

            // in case the file is being written it will go back to the end of the line
            acquire(filePath);

            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, toAppend));

            for (String line : toWrite)
                writer.append(line + "\n");

            writer.close();
            System.out.println("finish " + ++count);

            release(filePath);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
