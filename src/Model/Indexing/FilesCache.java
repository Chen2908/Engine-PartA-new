package Model.Indexing;

import Model.Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * This object represent a file cache memory
 */
public class FilesCache {

    //<editor-fold "Fields">

    private HashMap<String, List<StringBuilder>> files;//saved files
    private HashMap<String, Integer> filesImportance;//importance value of the files
    private HashSet<String> isGoingToBeNeeded;//saves the files that going to be needed in the close future
    private Queue<String> filesQueue;//entrance to the cache queue
    private String lastRemovedFileName;//the path of the last removed file
    private int MaxSize;//cache size - number of files saved
    private double hit;//the amount of times the wanted file was found in the cache memory
    private double miss;//the amount of times the file wasn't found in the cache memory
    private final double FUTURE_USE_PERCENT = 1;//the percentage of the size of the files that going to be used in the close future

    //</editor-fold>

    //<editor-fold "Constructor">

    /**
     * Constructor
     * @param size - cache size
     */
    public FilesCache(int size){
        files = new HashMap<>(size);
        filesImportance = new HashMap<>(size);
        isGoingToBeNeeded = new HashSet<>((int)(size*FUTURE_USE_PERCENT));
        if(size > 0)
            filesQueue = new ArrayBlockingQueue<>(size);
        MaxSize = size;
        hit = 0;
        miss = 0;
    }

    //</editor-fold>

    //<editor-fold "Getters">

    /**
     * This method returns the files queue
     * @return the files queue
     */
    public Queue<String> getFilesQueue(){
        return filesQueue;
    }

    /**
     * This method returns the wanted file (need to check first)
     * @param fileName - the wanted file
     * @return the content of the wanted file
     */
    public List<StringBuilder> getFile(String fileName){
        hit++;
        isGoingToBeNeeded.remove(fileName);
        filesImportance.put(fileName, (int)(filesImportance.get(fileName)*0.5 + calcSize(files.get(fileName))));
        return files.get(fileName);
    }

    /**
     * This method returns the cache memory max size
     * @return the cache memory max size
     */
    public int getMaxSize(){
        return MaxSize;
    }

    /**
     * This method returns the name of the last removed file
     * @return the name of the last removed file
     */
    public String getLastRemovedFileName(){
        return lastRemovedFileName;
    }

    //</editor-fold>

    //<editor-fold "Add & Setters">

    /**
     * This method adds a new file to the cache memory
     * in case the memory if already full the methods return
     * the content of the file that was removed from the memory.
     * @param fileName - file name
     * @param fileLines - file content
     * @return the removed file
     */
    public List<StringBuilder> add(String fileName, List<StringBuilder> fileLines){
        if (MaxSize == 0){
            lastRemovedFileName = fileName;
            return fileLines;
        }
        if (isInCache(fileName))
            return null;
        List<StringBuilder> toReturn = null;
        if (files.size() == MaxSize)
            toReturn = removeMin();
        files.put(fileName, fileLines);
        if (filesImportance.containsKey(fileName))
            filesImportance.put(fileName, (int)(filesImportance.get(fileName)*0.5 + calcSize(files.get(fileName))));
        else
            filesImportance.put(fileName, calcSize(fileLines));
        filesQueue.add(fileName);
        miss++;
        return toReturn;
    }

    /**
     * This methods add to the future use data structure
     * @param fileName - the file name to add
     */
    public void addToFutureUse(String fileName){
        if (!isFutureDataFull())
            isGoingToBeNeeded.add(fileName);
    }

    /**
     * This method set a the last remove file name
     * @param removedPath - the removed file name
     */
    private void setRemovedPath(String removedPath){
        lastRemovedFileName = removedPath;
    }

    //</editor-fold>

    //<editor-fold "Informative Methods">

    /**
     * This method checks if the given file is found in the cache memory
     * @param fileName - file name
     * @return true if the file is the the memory false if not
     */
    public boolean isInCache(String fileName){
        return files.containsKey(fileName);
    }

    /**
     * This method checks if the future used data capacity is full
     * @return true if full, false other wise
     */
    public boolean isFutureDataFull(){
        return isGoingToBeNeeded.size() >= MaxSize*FUTURE_USE_PERCENT;
    }

    /**
     * This method checks if the future used data is empty
     * @return true if empty, false other wise
     */
    public boolean isFutureEmpty(){
        return isGoingToBeNeeded.isEmpty();
    }

    //</editor-fold>

    //<editor-fold "Calculate Methods">

    /**
     * This method calculate the importance degree of the given file content
     * @param lines - file content
     * @return the importance degree
     */
    private int calcSize(List<StringBuilder> lines){
        int size = 0;
        for(StringBuilder line: lines)
            size += line.length();
        return size;
    }

    /**
     * This method calculate the hitrate value.
     *
     * @return
     */
    private double hitRate(){
        return hit/(hit+miss);
    }

    //</editor-fold>

    /**
     * This methods remove the file with the lowest value that found in the memory
     * @return the content of the removed file
     */
    private List<StringBuilder> removeMin(){
        String toRemove = null;
        int minVal = Integer.MAX_VALUE;
        for(String file: filesQueue)
            if(filesImportance.get(file) < minVal && !isGoingToBeNeeded.contains(file)){
                minVal = filesImportance.get(file);
                toRemove = file;
            }
        setRemovedPath(toRemove);
        filesQueue.remove(toRemove);
//        filesImportance.remove(toRemove);//remove and check
        return files.remove(toRemove);
    }

    /**
     * This method return a String of the hit rate information
     * @return number of hits, number of misses, anf the hit rate
     */
    @Override
    public String toString() {
        return "Hit: " + hit + "\nMiss: " + miss + "\nHitRate: " + hitRate();
    }

}
