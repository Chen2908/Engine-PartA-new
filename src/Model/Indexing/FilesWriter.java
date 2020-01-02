package Model.Indexing;

import Model.Model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;

public class FilesWriter implements Runnable {

    //<editor-fold "Fields">

    private ConcurrentLinkedDeque<String> filesPath;//Queue of all the files paths
    private ConcurrentHashMap<String, List<StringBuilder>> fileLines;//contains all the lines of each file
    private ConcurrentHashMap<String, Boolean> appendToFile;//contains the information if to append to a file or not
    private ConcurrentHashMap<String, Semaphore> fileStatus;//contains semaphore to protect the files
    private ConcurrentHashMap<String, Boolean> isInLine;//contains the information if a certain file is in line(going to be written soon)
    private Semaphore fieldsLock = new Semaphore(1);//semaphore that protects all the data structures

    //</editor-fold>

    //<editor-fold "Constructor & Reset>

    /**
     * Constructor
     */
    public FilesWriter() {
        reset();
    }

    /**
     * This methods reset all the data structures(fields)
     */
    public void reset(){
        fileLines = new ConcurrentHashMap<>();
        filesPath = new ConcurrentLinkedDeque<>();
        appendToFile = new ConcurrentHashMap<>();
        fileStatus = new ConcurrentHashMap<>();
        isInLine = new ConcurrentHashMap<>();
    }

    //</editor-fold>

    //<editor-fold "Add Files">

    /**
     * This method gets a file path and a list of the file content
     * and adds it to the data structures
     * @param filePath - the path of the file
     * @param lines - the content of the file
     * @param append - if to append to the file or override it
     */
    public void addFilesToWrite(String filePath, List<StringBuilder> lines, boolean append){
        try {
            fieldsLock.acquire();
            if (!fileStatus.containsKey(filePath))
                fileStatus.put(filePath, new Semaphore(1));
            filesPath.addLast(filePath);
            fileLines.put(filePath, lines);
            appendToFile.put(filePath, append);
            isInLine.put(filePath, true);
            fieldsLock.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //</editor-fold>

    //<editor-fold "Lock & Release>

    /**
     * This method locks the given file(file path)
     * if the file is occupied the thread will be blocked until the file is free again
     * @param file - the path of the file to lock
     */
    public void acquire(String file){
        try {
            fileStatus.get(file).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method releases the given file(file path)
     * @param file - the path of the file to release
     */
    public void release(String file){
        fileStatus.get(file).release();
    }

    //</editor-fold>

    //<editor-fold "Getters">

    /**
     * This method returns true if the file is waiting to be written or
     * if it being written write now, and return false other wise
     * @param filePath - the file path
     * @return -if the file is waiting to be written or if it being written write now
     */
    public boolean isInLine(String filePath){
        return isInLine.containsKey(filePath);
    }

    /**
     * This method returns how many files is waiting in line
     * @return the number of files in the line
     */
    public int numOfFilesToWrite(){
        return filesPath.size();
    }


    /**
     * This method returns the given file content if the file is waiting in line
     * and null in case the writing process already started
     * @param filePath - the wanted file
     * @return the file content if the writing process didn't start already
     */
    public List<StringBuilder> getFileToUpdate(String filePath){
        List<StringBuilder> fileLines = new ArrayList<>();
        try {
            fieldsLock.acquire();
            if(this.fileLines.containsKey(filePath) && fileStatus.get(filePath).tryAcquire()){
                filesPath.remove(filePath);
                appendToFile.remove(filePath);
                fileLines = this.fileLines.remove(filePath);
                isInLine.remove(filePath);
                fileStatus.get(filePath).release();
            }
            fieldsLock.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return fileLines;
    }


    //</editor-fold>

    /**
     * This method execute the writing, it polls a file
     * and write it to the disk at the given path
     */
    @Override
    public void run() {
//        Thread.currentThread().setPriority(3);
        String filePath = "";
        try {
            fieldsLock.acquire();
            if(numOfFilesToWrite() == 0) {
                fieldsLock.release();
                return;
            }
            filePath = filesPath.removeFirst();
            List<StringBuilder> toWrite = fileLines.remove(filePath);
            boolean toAppend = appendToFile.remove(filePath);
            fieldsLock.release();

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
