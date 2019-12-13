package Model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;

public class FilesWriter implements Runnable {

    private ConcurrentLinkedDeque<List<StringBuilder>> lines;
    private ConcurrentLinkedDeque<String> filesPath;
    private ConcurrentLinkedDeque<Boolean> appendToFile;
    private ConcurrentHashMap<String,Semaphore> fileStatus;
    private Semaphore semaphore = new Semaphore(1);

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

    public void addFilesToWrite(String filePath, List<StringBuilder> toAdd, boolean append){
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

    public void addFilesToWriteAtStart(String filePath, List<StringBuilder> toAdd, boolean append){
        try {
            semaphore.acquire();
            filesPath.addFirst(filePath);
            lines.addFirst(toAdd);
            appendToFile.addFirst(append);
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addFilesToWrite(String filePath, List<StringBuilder> lines){
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
        String filePath = "";
        try {
            semaphore.acquire();
            List<StringBuilder> toWrite = lines.removeFirst();
            filePath = filesPath.removeFirst();
            boolean toAppend = appendToFile.removeFirst();
            semaphore.release();

            // in case the file is being written it will go back to the end of the line
            acquire(filePath);

            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath), toAppend));

            for (StringBuilder line : toWrite)
                writer.append(line + "\n");

            writer.close();

            release(filePath);

        } catch (IOException | InterruptedException e) {
            System.out.println(filePath.substring(filePath.lastIndexOf('\\')+1,filePath.lastIndexOf('.')));
//            e.printStackTrace();
        }
    }
}
