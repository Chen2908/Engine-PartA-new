package Model;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class FilesCache {

    private HashMap<String, List<StringBuilder>> files;
    private HashMap<String, Integer> timeUsed;
    private Queue<String> filesQueue;
    private String lastRemovedPath;
    private int MaxSize;

    public FilesCache(int size){
        files = new HashMap<>(size);
        filesQueue = new ArrayBlockingQueue<>(size);
        timeUsed = new HashMap<>(size);
        MaxSize = size;
    }

    public boolean isInCache(String fileName){
        return files.containsKey(fileName);
    }

    public List<StringBuilder> getFile(String fileName){
        timeUsed.put(fileName, timeUsed.get(fileName)+1);
        return files.get(fileName);
    }

    public List<StringBuilder> add(String fileName, List<StringBuilder> fileLines){
        List<StringBuilder> toReturn = null;
        if (files.size() == MaxSize)
            toReturn = removeMin();
        files.put(fileName, fileLines);
        timeUsed.put(fileName, 1);
        filesQueue.add(fileName);
        return toReturn;
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

}
