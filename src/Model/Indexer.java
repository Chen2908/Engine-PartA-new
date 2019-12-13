package Model;

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
    private List<String> termsList;
    private HashMap<String, DocCorpusInfo> docsIndexInfo;
    private HashMap<String, Term> bellowThreshHold;
    private FilesCache filesCache;

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
        termsList = new LinkedList<>(this.terms.keySet());
        termsList.sort(Comparator.comparing(o -> filesNameHashFunction(o)));

        while (termsList.size() > 0){
            term = terms.get(termsList.remove(0));

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
        boolean isNew;
        String termLower;
        String termUpper;
        String dicKey = null;

        for(String termValue: fileTerm){

            isNew = false;
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

            if(isNew && bellowThreshHold.containsKey(termValue.toLowerCase())) {
                terms.get(termValue).marge(bellowThreshHold.get(termValue.toLowerCase()));
                bellowThreshHold.remove(termValue.toLowerCase());
            }

            if(terms.get(termValue).isBellowThreshHold(1, 6)){
                bellowThreshHold.put(termValue.toLowerCase(), terms.get(termValue));
                continue;
            }

            if(isNew) {
                fileLines.add(terms.get(termValue).toFileString());
                addToDictionary(termValue);
            }
            else
                try {
                    terms.get(termValue).update(fileLines, dictionary.get(dicKey));
                } catch (IndexOutOfBoundsException e){
                    System.out.println(termValue);
                    System.out.println(filePath);
                }
        }
        objectWriter.write(fileLines, filePath);
    }

    private void updateDictionary(String term){
        dictionary.put(term.toLowerCase(), dictionary.get(term.toUpperCase()));
        dictionary.remove(term.toUpperCase());
    }

    private List<String> getTermValues(Term term){
        int hashCode = filesNameHashFunction(term.getValue());
        List<String> terms = new LinkedList<>();
        terms.add(term.getValue());
        while(termsList.size() > 0 && filesNameHashFunction(termsList.get(0)) == hashCode)
            terms.add(termsList.remove(0));

        return terms;
    }

    private void addToDictionary(String term){
        String fileName = getFileName(term);

        if(fileLastLine.containsKey(fileName))
            fileLastLine.put(fileName, fileLastLine.get(fileName)+1);
        else
            fileLastLine.put(fileName, 0);

        dictionary.put(term, fileLastLine.get(fileName));
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

    public void writeDictionary(){
        StringBuilder dic = new StringBuilder();
        List<String> words = new ArrayList<>(dictionary.keySet());
        words.sort(String::compareTo);
        for(String word: words)
            dic.append(word + " - " + dictionary.get(word) + "\n");
        List<StringBuilder> toWrite = new ArrayList<>();
        toWrite.add(dic);
        objectWriter.write(toWrite, outputDir + "\\dictionary.txt");
    }

    public void writeBellowThreshHold(){
        StringBuilder dic = new StringBuilder();
        List<Term> words = new ArrayList<>(bellowThreshHold.values());
        words.sort(Comparator.comparing(Term::getValue));
        for(Term term: words)
            dic.append(term.getValue() + "\n");
        List<StringBuilder> toWrite = new ArrayList<>();
        toWrite.add(dic);
        objectWriter.write(toWrite, outputDir + "\\bellowThreshHold.txt");
    }
}
