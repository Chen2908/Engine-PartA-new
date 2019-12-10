import java.util.HashMap;
import java.util.List;

public class Manager {

    private static final int BATCH_SIZE = 2000;
    private ReadFile reader;
    private String corpusPath;
    private String postingPath ;
    private Parse parser;
    private Indexer inverter;
    private HashMap<String, Term> allTerms;


    public Manager(String corpusPath, String postingPath, boolean stemming) {
        this.reader = new ReadFile(corpusPath);
        this.corpusPath = corpusPath;
        this.postingPath = postingPath;
        this.parser = new Parse(corpusPath, stemming);
        this.inverter = new Indexer();

        //init paths

    }

    public void callReaderAndParser(){
        HashMap<String, Term> docTerms;
        List<Document> docs= reader.getNextDocs(BATCH_SIZE);
        while(docs!=null){
            docTerms=parser.parse(docs);
            docs= reader.getNextDocs(BATCH_SIZE);
        }
    }




}
