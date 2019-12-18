package Model;

import java.io.File;
import java.util.*;

public class Indexer {

    private final int HASH_SIZE = 800;
    private final int TF_INDEX = 0;
    private final int FILE_LINE_INDEX = 1;

    private int numOfCorpusDocs;
    private int indexIfCopy;
    private int threshHold;

    private File outputDir;
    private File termDir;
    private File docsDir;

    private HashMap<String, int[]> dictionary;
    private HashMap<String, Integer> fileLastLine;
    private HashMap<String, Term> terms;
    private List<String> termsList;
    private HashMap<String, DocCorpusInfo> docsIndexInfo;
    private HashMap<String, Term> bellowThreshHold;
    private StringBuilder docsInfoString;
    private FilesCache filesCache;

    //<editor-fold des="Read Write">

    private ObjectWriter objectWriter;

    //</editor-fold>


    public Indexer(String outputDirPath, int poolSize, int threshHold, int cacheSize){

        this.threshHold = threshHold;

        this.docsIndexInfo = new HashMap<>();
        this.fileLastLine = new HashMap<>();
        this.dictionary = new HashMap<>();
        this.bellowThreshHold = new HashMap<>();

        this.docsInfoString = new StringBuilder();
        this.filesCache = new FilesCache(cacheSize);
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
        addDocsCorpusInfo();
        docsIndexInfo = new HashMap<>();
        createIndex();
    }

    private void addDocsCorpusInfo(){
        List<String> docs = new ArrayList<>(docsIndexInfo.keySet());
        docs.sort(String::compareTo);
        for(String docNum: docs){
            docsInfoString.append(docNum + ";" + docsIndexInfo.get(docNum).toFileString() + "\n");
        }
    }

    public void createIndex(){

        Term term;
        termsList = new ArrayList<>(this.terms.keySet());
        termsList.sort(Comparator.comparing(o -> filesNameHashFunction(o)));
        addTCacheFutureUse(termsList);

        while (termsList.size() > 0){
            term = terms.get(termsList.remove(0));

            if(term.isEntity() && term.getDf() == 1 || term.isBellowThreshHold(threshHold, threshHold)){
                this.bellowThreshHold.put(term.getValue(), term);
                continue;
            }

            updateFiles(term);
        }
    }

    private void addTCacheFutureUse(List<String> terms){
        for (int i = 0 ; i < terms.size() && !filesCache.isFutureDataFull() ; i++)
            filesCache.addToFutureUse(getPath(termDir, terms.get(i)));
    }

    private void updateFiles(Term term){

        String filePath = getPath(termDir, term.getValue());
        List<StringBuilder> fileLines = getFileLines(filePath);
        List<String> fileTerm = getTermValues(term);

        if (filesCache.isFutureEmpty())
            addTCacheFutureUse(termsList);

        String termLower;
        String termUpper;
        String dicKey = null;

        for(String termValue: fileTerm){

            boolean isNew = false;
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

            if(terms.get(termValue).isBellowThreshHold(threshHold, threshHold)){
                bellowThreshHold.put(termValue.toLowerCase(), terms.get(termValue));
                continue;
            }

            if(isNew) {
                fileLines.add(terms.get(termValue).toFileString());
                addToDictionary(termValue);
            }
            else {
                terms.get(termValue).update(fileLines, dictionary.get(dicKey)[FILE_LINE_INDEX]);
                dictionary.get(dicKey)[TF_INDEX] += terms.get(termValue).getTF();
            }

            getDocsInfo(terms.get(termValue));
        }
//        addToCache(filePath, fileLines);
    }

    private void addToCache(String filePath, List<StringBuilder> fileLines){
        List<StringBuilder> toWrite = filesCache.add(filePath, fileLines);
        if(toWrite != null)
            objectWriter.write(toWrite, filesCache.getLastRemovedPath());
    }

    private List<StringBuilder> getFileLines(String filePath){

        List<StringBuilder> fileLines;

        if(!filesCache.isInCache(filePath))
            addToCache(filePath, objectWriter.readFile(filePath));
        fileLines = filesCache.getFile(filePath);

        return fileLines;
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

    private void addToDictionary(String term) {
        String fileName = getFileName(term);

        if (fileLastLine.containsKey(fileName))
            fileLastLine.put(fileName, fileLastLine.get(fileName) + 1);
        else
            fileLastLine.put(fileName, 0);

        dictionary.put(term, new int[2]);
        dictionary.get(term)[TF_INDEX] = terms.get(term).getTF();
        dictionary.get(term)[FILE_LINE_INDEX] = fileLastLine.get(fileName);
    }

    private String getPath(File dir, String term){
        return dir.getAbsolutePath() + "\\" + getFileName(term) + ".txt";
    }

    private String getFileName(String str){
        return filesNameHashFunction(str) + "";
    }

    private int filesNameHashFunction(String fileName){
        return (Math.abs(fileName.toLowerCase().hashCode())) % HASH_SIZE;
    }

    private void getDocsInfo(Term term){
        for(DocTermInfo doc: term.getDocs().values()){
            if(!docsIndexInfo.containsKey(doc.getDocNum()))
                docsIndexInfo.put(doc.getDocNum(), new DocCorpusInfo());
            docsIndexInfo.get(doc.getDocNum()).updateDoc(doc.getTfi());
        }
    }

    public void closeWriter(){
        writeCache();
        System.out.println(filesCache.toString());
        writeDictionary();
        writeBellowThreshHold();
        writeDocsInfo();
        objectWriter.close();
    }

    private void writeDocsInfo(){
        List<StringBuilder> docInfo = new ArrayList<>(1);
        docInfo.add(docsInfoString);
        objectWriter.write(docInfo, docsDir.getAbsolutePath() + "\\docsInfo.txt");

    }

    private void writeCache(){
        Queue<String> filesNames = filesCache.getFilesQueue();
        if(filesNames == null)
            return;
        for(String file: filesNames){
            objectWriter.write(filesCache.getFile(file), file);
        }
    }

    public HashMap<String, Integer> getDictionary(){
        HashMap<String, Integer> newDic = new HashMap<>();
        Iterator it = dictionary.keySet().iterator();
        while (it.hasNext()){
            String term = (String) it.next();
            newDic.put(term, dictionary.get(term)[TF_INDEX]);
        }
        return newDic;
    }

    public HashMap<String, Term> getBellowThreshHold() {
        return bellowThreshHold;
    }

    private void writeDictionary(){
        StringBuilder dic = new StringBuilder();
        List<String> words = new ArrayList<>(dictionary.keySet());
        words.sort(String::compareTo);
        for(String word: words)
            dic.append(word + " ; " + dictionary.get(word)[TF_INDEX] + " ; " + dictionary.get(word)[FILE_LINE_INDEX] + "\n");
        List<StringBuilder> toWrite = new ArrayList<>();
        toWrite.add(dic);
        objectWriter.write(toWrite, outputDir + "\\dictionary.txt");
    }

    private void writeBellowThreshHold(){
        StringBuilder dic = new StringBuilder();
        List<Term> words = new ArrayList<>(bellowThreshHold.values());
        words.sort(Comparator.comparing(o -> o.getValue().toLowerCase()));
        for(Term term: words)
            dic.append(term.getValue() + "\n");
        List<StringBuilder> toWrite = new ArrayList<>();
        toWrite.add(dic);
        objectWriter.write(toWrite, outputDir + "\\bellowThreshHold.txt");
    }
}