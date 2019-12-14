package Model;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class FilesCache {

    private static Instrumentation instrumentation;
    private HashMap<String, List<StringBuilder>> files;
    private HashMap<String, Integer> timeUsed;
    private Queue<String> filesQueue;
    private String lastRemovedPath;
    private int MaxSize;
    private double hit;
    private double miss;

    public FilesCache(int size){
        files = new HashMap<>(size);
        timeUsed = new HashMap<>(size);
        if(size > 0)
            filesQueue = new ArrayBlockingQueue<>(size);
        MaxSize = size;
        hit = 0;
        miss = 0;
    }

    public Queue<String> getFilesQueue(){
        return filesQueue;
    }

    public boolean isInCache(String fileName){
        return files.containsKey(fileName);
    }

    public List<StringBuilder> getFile(String fileName){
        hit++;
        timeUsed.put(fileName, (int)(timeUsed.get(fileName)*0.5 + clacSize(files.get(fileName))));
        return files.get(fileName);
    }

    public List<StringBuilder> add(String fileName, List<StringBuilder> fileLines){
        if(MaxSize == 0){
            lastRemovedPath = fileName;
            return fileLines;
        }
        if(isInCache(fileName))
            return null;
        List<StringBuilder> toReturn = null;
        if (files.size() == MaxSize)
            toReturn = removeMin();
        files.put(fileName, fileLines);
        timeUsed.put(fileName, clacSize(fileLines));
        filesQueue.add(fileName);
        miss++;
        return toReturn;
    }

    private int clacSize(List<StringBuilder> lines){
        int size = 0;
        for(StringBuilder line: lines)
            size += line.length();
        return size;
    }

    public String getLastRemovedPath(){
        return lastRemovedPath;
    }

    private List<StringBuilder> removeMin(){
        String toRemove = null;
        int minVal = Integer.MAX_VALUE;
        for(String file: filesQueue)
            if(timeUsed.get(file) < minVal){
                minVal = timeUsed.get(file);
                toRemove = file;
                if(minVal == 1)
                    break;
            }
        setRemovedPath(toRemove);
        filesQueue.remove(toRemove);
        timeUsed.remove(toRemove);
        return files.remove(toRemove);
    }

    private void setRemovedPath(String removedPath){
        lastRemovedPath = removedPath;
    }

    @Override
    public String toString() {
        return "Hit: " + hit + "\nMiss: " + miss + "\nHitRate: " + hitRate() + "\n";
    }

    private double hitRate(){
        return hit/(hit+miss);
    }
}
