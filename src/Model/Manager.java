package Model;

import Model.Indexing.*;
import Model.Retrieval.Searcher;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

/**
 * This class represents a corpus manager, which is an objects that controls the whole process of reading the documents
 * parsing then and indexing the terms.
 * It holds the following objects for this purpose:  ReadFile, Parser, Indexer, Calculator
 */
public class Manager {

    private static final int BATCH_SIZE = 6000;
    private static final int THRESHOLD = 0 ;
    private static final int THREAD_POOL_SIZE = 5;
    private static final int HASHSIZE = 20000;

    private ReadFile reader;
    private String corpusPath;
    private String postingPath ;
    private Parse parser;
    private Indexer inverter;
    private String dictionaryPath;
    private String indexPath;
    private boolean stemming;
    private int corpusSize;
    private int vocabularySize;
    private double processTime;
    private HashMap<String, Term> docTerms;
    private HashMap<String, List<String>> entities;

    //partB
    private Searcher searcher;

    /**
     * Constructor with parameters for parsing
     * @param corpusPath - the path where the corpus files and stop words is saved
     * @param postingPath - the path where to create the posting files in
     * @param stemming - true if to apply stemming, otherwise false
     */
    public Manager(String corpusPath, String postingPath, boolean stemming) {
        this.corpusPath = corpusPath;
        this.postingPath = postingPath;
        this.stemming = stemming;
        this.reader = new ReadFile(corpusPath);
        this.parser = new Parse(corpusPath, stemming);
        setPaths();
        this.corpusSize= findCorpusSize(corpusPath);
        this.inverter = new Indexer(this.indexPath, THREAD_POOL_SIZE, THRESHOLD, 20000, HASHSIZE);
        Calculator.setCorpusSize(this.corpusSize);
    }

    /**
     * Constructor with parameters for searching
     * @param indexPath - the path where to create the posting files in
     */
    public Manager (String indexPath, boolean stemming){
        this.searcher = new Searcher(indexPath, indexPath, HASHSIZE, stemming);
    }


    public void setSearcherSemantics(int semanticsNum) {
        this.searcher.setSemanticModel(semanticsNum);
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
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        //measures
        Runtime runtime = Runtime.getRuntime();
        double usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        double startTime = System.currentTimeMillis();

        List<Document> docs= reader.getNextDocs(BATCH_SIZE);
        int counter=0;
        while(docs!=null){
            System.out.println(counter++);
            docTerms = parser.parse(docs);
            callIndexBuild(docTerms);
//            System.out.println(i++ + " - " + (System.currentTimeMillis() - lastTime) / 1000);
//            lastTime = System.currentTimeMillis();
            docs = reader.getNextDocs(BATCH_SIZE);
        }

        //write all dictionaries
        inverter.closeWriter();

        double endTime = System.currentTimeMillis();
        setVocabularySize(inverter.getDictionary().size());
        setProcessTime((endTime - startTime) / 1000);
        writeStopWordsFile(parser.getStopWords());
    }

    private void writeStopWordsFile(HashSet<String> stopWords) {
        try {
            String path;
            if (stemming)
                path = postingPath + "With Stemming/Index/stop_words.txt";
            else
                path = postingPath + "Without Stemming/Index/stop_words.txt";
            File file = new File(path);
            BufferedWriter bf = new BufferedWriter(new FileWriter(file));
            for(String st: stopWords){
                bf.write(st+ "\n");
            }
            bf.close();
        }catch(IOException e){
        }
    }

    private void callIndexBuild(HashMap<String, Term> docTerms){
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
        }
        return dir;
    }

    // term, df
    public HashMap<String, Integer> getDictionaryToShow(){
        return inverter.getDictionary();
    }

    public int getVocabularySize() {
        return vocabularySize;
    }

    public double getProcessTime() {
        return processTime;
    }

    public void setVocabularySize(int vocabularySize) {
        this.vocabularySize = vocabularySize;
    }

    //time in seconds
    public void setProcessTime(double processTime) {
        this.processTime = processTime;
    }

    public int getCorpusSize() {
        return corpusSize;
    }

    public void resetObjects() {
        this.reader = null;
        this.parser = null;
        this.inverter = null;
        this.docTerms = null;
    }

    public List<Pair<String, Double>> search(String queryText) {
        List<Pair<String, Double>> searched= searcher.search(queryText);
        if (entities == null)
            entities = searcher.getEntities();
        return searched;
    }

    public HashMap<String, List<String>> getEntities(){
        return entities;
    }


}
