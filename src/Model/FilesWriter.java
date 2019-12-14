package Model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;

public class FilesWriter implements Runnable {

    private ConcurrentLinkedDeque<String> filesPath;
    private ConcurrentHashMap<String, List<StringBuilder>> lines;
    private ConcurrentHashMap<String, Boolean> appendToFile;
    private ConcurrentHashMap<String, Semaphore> fileStatus;
    private ConcurrentHashMap<String, Boolean> isInLine;
    private Semaphore semaphore = new Semaphore(1);
    private Semaphore inLine = new Semaphore(1);

    private static int count = 0;

    public FilesWriter() {
        reset();
    }

    public void reset(){
        lines = new ConcurrentHashMap<>();
        filesPath = new ConcurrentLinkedDeque<>();
        appendToFile = new ConcurrentHashMap<>();
        fileStatus = new ConcurrentHashMap<>();
        isInLine = new ConcurrentHashMap<>();
    }

    public void addFilesToWrite(String filePath, List<StringBuilder> toAdd, boolean append){
        try {
            semaphore.acquire();
            if (!fileStatus.containsKey(filePath))
                fileStatus.put(filePath, new Semaphore(1));
            filesPath.addLast(filePath);
            lines.put(filePath, toAdd);
            appendToFile.put(filePath, append);
            isInLine.put(filePath, true);
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
            fileStatus.get(file).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release(String file){
        fileStatus.get(file).release();
    }

    public boolean isInLine(String filePath){
        return isInLine.containsKey(filePath);
    }

    public List<StringBuilder> getFileToUpdate(String filePath){
        List<StringBuilder> fileLines = new LinkedList<>();
        try {
            semaphore.acquire();
            if(lines.containsKey(filePath) && fileStatus.get(filePath).tryAcquire()){
                filesPath.remove(filePath);
                appendToFile.remove(filePath);
                fileLines = lines.remove(filePath);
                isInLine.remove(filePath);
                fileStatus.get(filePath).release();
            }
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return fileLines;
    }

    @Override
    public void run() {

        String filePath = "";
        try {
            semaphore.acquire();
            if(numOfFilesToWrite() == 0) {
                semaphore.release();
                return;
            }
            filePath = filesPath.removeFirst();
            List<StringBuilder> toWrite = lines.remove(filePath);
            boolean toAppend = appendToFile.remove(filePath);
            semaphore.release();

            acquire(filePath);

            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath), toAppend));

            for (StringBuilder line : toWrite)
                writer.append(line + "\n");

            writer.close();

            release(filePath);
            isInLine.remove(filePath);


        } catch (IOException | InterruptedException e) {
            System.out.println(filePath.substring(filePath.lastIndexOf('\\')+1,filePath.lastIndexOf('.')));
//            e.printStackTrace();
        }
    }
}
