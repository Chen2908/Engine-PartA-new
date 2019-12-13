package Model;

import Model.DocCorpusInfo;

import java.io.File;
import java.util.*;

public class Indexer {

    /**
     * TODO:
     * finish writing term directory and check it
     * write Documents info files
     * create dictionary
     * create corpus info file
     */
    private final int HASH_SIZE = 8000;
    private int numOfCorpusDocs;
    private int indexIfCopy;
    private int threshHold;

    private File outputDir;
    private File termDir;
    private File docsDir;

    private HashMap<String, Integer> dictionary;
    private HashMap<String, Integer> fileLastLine;
    private HashMap<String, Term> terms;
    private List<Term> termsList;
    private HashMap<String, DocCorpusInfo> docsIndexInfo;
    private HashMap<String, Term> bellowThreshHold;

    //<editor-fold des="Read Write">

    private ObjectWriter objectWriter;

    //</editor-fold>




    public Indexer(String outputDirPath, int numOfDocs, int poolSize, int threshHold){

        this.numOfCorpusDocs = numOfDocs;
        this.threshHold = threshHold;

        this.docsIndexInfo = new HashMap<>();
        this.fileLastLine = new HashMap<>();
        this.dictionary = new HashMap<>();
        this.bellowThreshHold = new HashMap<>();

        this.objectWriter = new ObjectWriter(outputDirPath, poolSize);
        this.indexIfCopy = 0;

        this.outputDir = new File(outputDirPath + "\\Index");
        creatFolders();

    }

    private void creatFolders(){

        String outputDirPath = this.outputDir.getAbsolutePath();
        while(this.outputDir.exists() && this.outputDir.isDirectory())
            this.outputDir = new File(outputDirPath + "-" + ++indexIfCopy);

        this.docsDir = new File(this.outputDir.getAbsolutePath() + "\\Docs");
        this.termDir = new File(this.outputDir.getAbsolutePath() + "\\Terms");

        this.outputDir.mkdir();
        this.docsDir.mkdir();
        this.termDir.mkdir();
    }

    public void setTerms(HashMap<String, Term> terms){
        this.terms = terms;
        createIndex();
    }

    public void createIndex(){

        Term term;
        termsList = new ArrayList<>(this.terms.values());
        termsList.sort(Comparator.comparing(o -> filesNameHashFunction(o.getValue())));

        while (termsList.size() > 0){
            term = termsList.remove(0);

            if(term.isEntity() && term.getDf() == 1 || term.isBellowThreshHold(1, 5)){
                this.bellowThreshHold.put(term.getValue(), term);
                continue;
            }

            updateFiles(term);
            
        }
    }

    private void updateFiles(Term term){

        String filePath = getPath(termDir, term.getValue());
        List<StringBuilder> fileLines = objectWriter.readFile(filePath);
        List<String> fileTerm = getTermValues(term);
        boolean isNew = false;
        String termLower;
        String termUpper;
        String dicKey = null;

        for(String termValue: fileTerm){
            isNew = false;

            isNew = false;
            if(terms.get(termValue).isEntity() && terms.get(termValue).getDf() == 1 || terms.get(termValue).isBellowThreshHold(1, 5)){
                this.bellowThreshHold.put(term.getValue(), term);
                continue;
            }

            termLower = termValue.toLowerCase();
            termUpper = termValue.toUpperCase();

            if(!termLower.equals(termUpper) && terms.containsKey(termLower) && dictionary.containsKey(termUpper))
                updateDictionary(termValue);

            if(dictionary.containsKey(termLower))
                dicKey = termLower;
            else if(dictionary.containsKey(termUpper))
                dicKey = termUpper;
            else if(dictionary.containsKey(termValue))
                dicKey = termValue;
            else
                isNew = true;


            if(isNew) {
                addToDictionary(terms.get(termValue));
                fileLines.add(terms.get(termValue).toFileString());
            } else
                terms.get(termValue).update(fileLines, dictionary.get(dicKey));

            termsList.remove(terms.get(termValue));
        }
        objectWriter.write(fileLines, filePath);
    }

    private void updateDictionary(String term){
        dictionary.put(term.toLowerCase(), dictionary.get(term.toUpperCase()));
        dictionary.remove(term.toUpperCase());
    }

    private List<String> getTermValues(Term term){
        int hashCode = filesNameHashFunction(term.getValue());
        List<String> terms = new ArrayList<>();
        terms.add(term.getValue());
        while(termsList.size() > 0 && filesNameHashFunction(termsList.get(0).getValue()) == hashCode)
            terms.add(termsList.remove(0).getValue());

        return terms;
    }

    private void writeNewTerm(Term term){
        addToDictionary(term);
        objectWriter.write(term, getPath(termDir, term.getValue()), true);
    }

    private void addToDictionary(Term term){
        String fileName = getFileName(term.getValue());

        if(fileLastLine.containsKey(fileName))
            fileLastLine.put(getFileName(term.getValue()), fileLastLine.get(fileName)+1);
        else
            fileLastLine.put(fileName, 0);

        dictionary.put(term.getValue(), fileLastLine.get(fileName));
    }

    private String getPath(File dir, String term){
        return dir.getAbsolutePath() + "\\" + getFileName(term) + ".txt";
    }

    private String getFileName(String str){
        return filesNameHashFunction(str) + "";
    }

    private int filesNameHashFunction(String fileName){
        return Math.abs(fileName.toLowerCase().hashCode()) % HASH_SIZE;
    }

    public void closeWriter(){
        objectWriter.close();
    }

    public HashMap<String, Integer> getDictionary(){
        return dictionary;
    }

    public HashMap<String, Term> getBellowThreshHold() {
        return bellowThreshHold;
    }
}
