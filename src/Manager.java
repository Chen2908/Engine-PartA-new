import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Manager {

    private static final int BATCH_SIZE = 2000;
    private static final int THRESHOLD = 2 ;

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
       // this.inverter = new Indexer(this.indexPath, corpusSize, THRESHOLD);
    }


    private void setPaths() {
        String path = createIndexFolders();
        this.dictionaryPath = path + "/dictionary.txt";
        this.indexPath = path + "Index";
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
        HashMap<String, Term> docTerms;
        List<Document> docs= reader.getNextDocs(BATCH_SIZE);
        while(docs!=null){
            docTerms=parser.parse(docs);
            callIndexBuild(docTerms);
            docs= reader.getNextDocs(BATCH_SIZE);
        }
    }

    private void callIndexBuild(HashMap<String,Term> docTerms) {

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
        String dir = postingPath + stem;
        File mainFolder = new File(dir);
        if (!mainFolder.exists()) {
            mainFolder.mkdir();
            File indexFolder = new File(dir + "/Index");
            if (!indexFolder.exists())
                indexFolder.mkdir();
        }
        return dir;
    }


}
