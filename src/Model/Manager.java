package Model;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Manager {

    private static final int BATCH_SIZE = 2000;
    private static final int THRESHOLD = 2 ;
    private static final int THREAD_POOL_SIZE = 3;

    private ReadFile reader;
    private String corpusPath;
    private String postingPath ;
    private Parse parser;
    private Indexer inverter;
    private String dictionaryPath;
    private String indexPath;
    private boolean stemming;
    private int corpusSize;


    public Manager(String corpusPath, String postingPath, boolean stemming) {
        this.corpusPath = corpusPath;
        this.postingPath = postingPath;
        this.stemming = stemming;
        this.reader = new ReadFile(corpusPath);
        this.parser = new Parse(corpusPath, stemming);
        setPaths();
        this.corpusSize= findCorpusSize(corpusPath);
        this.inverter = new Indexer(this.indexPath, corpusSize, THRESHOLD, THREAD_POOL_SIZE);
    }


    private void setPaths() {
        String path = createIndexFolders();
        this.dictionaryPath = path + "\\dictionary.txt";
        this.indexPath = path;
    }

    private static int findCorpusSize(String corpusPath) {
        int countDocs = 0;
        ReadFile reader = new ReadFile(corpusPath);
        Document doc = reader.getNextDoc();
        while (doc!=null){
            countDocs++;
            doc = reader.getNextDoc();
        }
        return countDocs;
    }


    public void callReaderAndParser(){
        //measures
        Runtime runtime = Runtime.getRuntime();
        double usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        double startTime = System.currentTimeMillis();

        //keep only this
        HashMap<String, Term> docTerms=null;
        List<Document> docs= reader.getNextDocs(BATCH_SIZE);
        while(docs!=null){
            docTerms=parser.parse(docs);
            callIndexBuild(docTerms);
            docs= reader.getNextDocs(BATCH_SIZE);
        }
        inverter.closeWriter();

        double usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        double endTime = System.currentTimeMillis();
        System.out.println("Number Of Words: " + inverter.getDictionary().size());
        System.out.println("Running Time: " + (endTime - startTime) / 60000 + " Min");
        System.out.println("Memory increased: " + (usedMemoryAfter - usedMemoryBefore) / Math.pow(2, 20) + " MB");
    }

    private void callIndexBuild(HashMap<String, Term> docTerms) {
        this.inverter.setTerms(docTerms);
    }

    public String getDictionaryPath(){
        return dictionaryPath;
    }

    private String createIndexFolders() {
        String stem;
        if(stemming){
            stem = "With Stemming";
        }
        else {
            stem = "Without Stemming";
        }
        String dir = postingPath + "\\"+ stem;
        File mainFolder = new File(dir);
        if (!mainFolder.exists()) {
            mainFolder.mkdir();
            File indexFolder = new File(dir + "\\Index");
            if (!indexFolder.exists())
                indexFolder.mkdir();
        }
        return dir;
    }


}
