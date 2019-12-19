package Model;

import java.io.File;
import java.util.*;

public class Indexer {

    //<editor-fold des="Class Fields">

    private final int TF_INDEX = 0;
    private final int FILE_LINE_INDEX = 1;

    private int indexIfCopy;
    private int threshHold;
    private int numOfPostingFiles = 800;

    //<editor-fold des="Directory Files">

    private File outputDir;
    private File postingDir;
    private File docsInfoDir;

    //</editor-fold>

    //<editor-fold des="Data Structure">

    private HashMap<String, int[]> dictionary;//dictionary of all the words
    private HashMap<String, Integer> fileLastLine;//saves the last line occupied in all the files
    private HashMap<String, Term> terms;//HashMap of the term of the current batch
    private List<String> termsList;//all the given terms list
    private HashMap<String, DocCorpusInfo> docsCorpusInfo;//saves all the information about the documents in the current batch
    private HashMap<String, Term> bellowThreshHold;//saves all the terms that there appearance so far was bellow the threshold
    private StringBuilder docsInfoString;//StringBuilder that contains all the information about the documents (docs file)
    private FilesCache filesCache;//cache memory of files

    //</editor-fold>

    //<editor-fold des="Read Write">

    private ObjectWriter objectWriter;

    //</editor-fold>

    //</editor-fold>

    //<editor-fold des="Constructor & Setups">

    /**
     * Constructor for an Indexer Object
     * @param outputDirPath - the directory of the output files
     * @param poolSize - amount threads that will write to the disk
     * @param threshHold - a threshold for words that will not be written
     * @param cacheSize - the amount of files that the cache will store in the memory
     */
    public Indexer(String outputDirPath, int poolSize, int threshHold, int cacheSize, int numOfPostingFiles){

        this.threshHold = threshHold;

        this.docsCorpusInfo = new HashMap<>();
        this.fileLastLine = new HashMap<>();
        this.dictionary = new HashMap<>();
        this.bellowThreshHold = new HashMap<>();

        this.docsInfoString = new StringBuilder();
        this.filesCache = new FilesCache(cacheSize);
        this.objectWriter = new ObjectWriter(poolSize);
        this.indexIfCopy = 0;
        this.numOfPostingFiles = numOfPostingFiles;

        this.outputDir = new File(outputDirPath + "\\Index");
        creatFolders();

    }

    /**
     * This methods create the output directories in the given output directory
     */
    private void creatFolders(){

        String outputDirPath = this.outputDir.getAbsolutePath();
        while(this.outputDir.exists() && this.outputDir.isDirectory())
            this.outputDir = new File(outputDirPath + "-" + ++indexIfCopy);

        this.docsInfoDir = new File(this.outputDir.getAbsolutePath() + "\\Docs");
        this.postingDir = new File(this.outputDir.getAbsolutePath() + "\\Terms");

        this.outputDir.mkdir();
        this.docsInfoDir.mkdir();
        this.postingDir.mkdir();
    }

    /**
     * This methods get HashMap of term and starts the indexing process on them
     * @param terms - terms HashMap
     */
    public void setTerms(HashMap<String, Term> terms){
        this.terms = terms;
        addDocsCorpusInfo();
        docsCorpusInfo = new HashMap<>();
        createIndex();
    }

    //</editor-fold>

    /**
     * This method iterates on all the terms and create the dictionary file, and posting files
     */
    private void createIndex(){

        Term term;
        termsList = new ArrayList<>(this.terms.keySet());
        termsList.sort(Comparator.comparing(o -> fileNameHashFunction(o)));
        addToCacheFutureUse(termsList);

        while (termsList.size() > 0){
            term = terms.get(termsList.remove(0));

            if(term.isEntity() && term.getDf() == 1 || term.isBellowThreshHold(threshHold, threshHold)){
                this.bellowThreshHold.put(term.getValue(), term);
                continue;
            }

            updateFiles(term);
        }
    }

    //<editor-fold des="Update Data Structure">

    /**
     * This method updates the files of the given term
     * in case there are more term that should be written to that same file it updates them to
     * @param term - the term that needs to be update
     */
    private void updateFiles(Term term){

        String filePath = getPath(postingDir, term.getValue());//the path of the terms file
        List<StringBuilder> fileLines = getFileLines(filePath);//gets the file of the term
        List<String> fileTerm = getSameFileTermValues(term);// gets all the terms values of the term that needs to be written to the same file

        //fill the data that saves the future files that will be written soon (in case it is empty)
        if (filesCache.isFutureEmpty())
            addToCacheFutureUse(termsList);

        String termLower;
        String termUpper;
        String dicKey = null;

        for(String termValue: fileTerm){

            boolean isNew = false;
            termLower = termValue.toLowerCase();
            termUpper = termValue.toUpperCase();

            //checks if a term that started only with capital letter found with lower case letter
            if(!termLower.equals(termUpper) && terms.containsKey(termLower) && dictionary.containsKey(termUpper))
                updateDictionary(termValue);

            //get the way that the term appears in the dictionary
            if(dictionary.containsKey(termLower))
                dicKey = termLower;
            else if(dictionary.containsKey(termUpper))
                dicKey = termUpper;
            else if(dictionary.containsKey(termValue))
                dicKey = termValue;
            else
                isNew = true;

            //checks if the term is found in the HashMap that contains the terms that are bellow the threshold
            if(isNew && bellowThreshHold.containsKey(termValue.toLowerCase())) {
                //marge the term info with the the term that were saved in the HashMap
                terms.get(termValue).marge(bellowThreshHold.get(termValue.toLowerCase()));
                bellowThreshHold.remove(termValue.toLowerCase());
            }

            //checks if the term is bellow the threshold
            if(terms.get(termValue).isBellowThreshHold(threshHold, threshHold)){
                bellowThreshHold.put(termValue.toLowerCase(), terms.get(termValue));
                continue;
            }

            //adds the file information to the file lines, and adds it to the dictionary
            if(isNew) {
                fileLines.add(terms.get(termValue).toFileString());
                addToDictionary(termValue);
            }
            //updates the term line in the file lines
            else {
                terms.get(termValue).update(fileLines, dictionary.get(dicKey)[FILE_LINE_INDEX]);
                dictionary.get(dicKey)[TF_INDEX] += terms.get(termValue).getTF();
            }
            //writes the terms document information to the docs HashMap
            updateDocsTermInfo(terms.get(termValue));
        }
    }

    /**
     * This method updates the key (termValue value) of the dictionary
     * @param termValue - the termValue to update
     */
    private void updateDictionary(String termValue){
        dictionary.put(termValue.toLowerCase(), dictionary.get(termValue.toUpperCase()));
        dictionary.remove(termValue.toUpperCase());
    }

    //</editor-fold>

    //<editor-fold des="Add to Data Structures">

    /**
     * This method adds all the information on the document to the StringBuilder of all the documents
     */
    private void addDocsCorpusInfo(){
        List<String> docs = new ArrayList<>(docsCorpusInfo.keySet());
        docs.sort(String::compareTo);
        for(String docNum: docs)
            docsInfoString.append(docNum + ";" + docsCorpusInfo.get(docNum).toFileString() + "\n");
    }

    /**
     * This method adds to the cache the information about witch file is going to be used in the future
     * @param terms - list of term sorted by the hashFunction
     */
    private void addToCacheFutureUse(List<String> terms){
        for (int i = 0 ; i < terms.size() && !filesCache.isFutureDataFull() ; i++)
            filesCache.addToFutureUse(getPath(postingDir, terms.get(i)));
    }

    /**
     * This method adds a list of term to the cache memory
     * and writes the returned file to the directory (file is returned in case the cache is full)
     * @param filePath - the path of the file
     * @param fileLines - the file content
     */
    private void addToCache(String filePath, List<StringBuilder> fileLines){
        List<StringBuilder> toWrite = filesCache.add(filePath, fileLines);
        if(toWrite != null)
            objectWriter.write(toWrite, filesCache.getLastRemovedFileName());
    }

    /**
     * This method gets a termValue and adds it to the dictionary, and update the fileLastLine HashMap
     * @param termValue - termValue
     */
    private void addToDictionary(String termValue) {
        String fileName = getFileName(termValue);

        if (fileLastLine.containsKey(fileName))
            fileLastLine.put(fileName, fileLastLine.get(fileName) + 1);
        else
            fileLastLine.put(fileName, 0);

        dictionary.put(termValue, new int[2]);
        dictionary.get(termValue)[TF_INDEX] = terms.get(termValue).getTF();
        dictionary.get(termValue)[FILE_LINE_INDEX] = fileLastLine.get(fileName);
    }
    //</editor-fold>

    //<editor-fold des="Getters">

    /**
     * This method gets a file path and returns the file lines
     * @param filePath - path to the wanted file
     * @return list of the file lines
     */
    private List<StringBuilder> getFileLines(String filePath){

        List<StringBuilder> fileLines;

        if(!filesCache.isInCache(filePath))
            addToCache(filePath, objectWriter.readFile(filePath));
        fileLines = filesCache.getFile(filePath);

        return fileLines;
    }

    /**
     * This method gets a term and return a list of the terms that have the same hashcode
     * @param term
     * @return list of all the current term values withe the same hashcode value
     */
    private List<String> getSameFileTermValues(Term term){
        int hashCode = fileNameHashFunction(term.getValue());
        List<String> terms = new LinkedList<>();
        terms.add(term.getValue());
        while(termsList.size() > 0 && fileNameHashFunction(termsList.get(0)) == hashCode)
            terms.add(termsList.remove(0));

        return terms;
    }

    /**
     * This method gets the Directory of the file and the termValue and returns the whole path to the term file
     * @param dir - directory of the file
     * @param termValue - term value
     * @return the whole path to the term file
     */
    private String getPath(File dir, String termValue){
        return dir.getAbsolutePath() + "\\" + getFileName(termValue) + ".txt";
    }

    /**
     * This function returns the the name of the file of the given term
     * @param termValue
     * @return the file name of the given term value
     */
    private String getFileName(String termValue){
        return fileNameHashFunction(termValue) + "";
    }

    /**
     * This method updates all the Document information that the given term contains
     * @param term
     */
    private void updateDocsTermInfo(Term term){
        for(DocTermInfo doc: term.getDocs().values()){
            if(!docsCorpusInfo.containsKey(doc.getDocNum()))
                docsCorpusInfo.put(doc.getDocNum(), new DocCorpusInfo());
            docsCorpusInfo.get(doc.getDocNum()).updateDoc(doc.getTfi());
        }
    }

    /**
     * This methods returns the hashcode of a given string
     * @param termValue - string
     * @return the hashcode ogf the given string
     */
    private int fileNameHashFunction(String termValue){
        return (Math.abs(termValue.toLowerCase().hashCode())) % numOfPostingFiles;
    }

    /**
     * This methods returns a dictionary hashMap that contains the terms values(keys)
     * and the term frequency(values)
     * @return dictionary as HashMap
     */
    public HashMap<String, Integer> getDictionary(){
        HashMap<String, Integer> newDic = new HashMap<>();
        Iterator it = dictionary.keySet().iterator();
        while (it.hasNext()){
            String term = (String) it.next();
            newDic.put(term, dictionary.get(term)[TF_INDEX]);
        }
        return newDic;
    }

    /**
     * This methods returns a hashMap that contains the terms values(keys)
     * and the term(values) that were bellow the threshold
     * @return dictionary as HashMap
     */
    public HashMap<String, Term> getBellowThreshHold() {
        return bellowThreshHold;
    }

    //</editor-fold>

    //<editor-fold des="Write Files Methods">

    /**
     * This methods writes the Document information StringBuilder to the document file
     */
    private void writeDocsInfo(){
        List<StringBuilder> docInfo = new ArrayList<>(1);
        docInfo.add(docsInfoString);
        objectWriter.write(docInfo, docsInfoDir.getAbsolutePath() + "\\docsInfo.txt");
    }

    /**
     * This method writes all the files in the cache memory
     */
    private void writeCache(){
        Queue<String> filesNames = filesCache.getFilesQueue();
        if(filesNames == null)
            return;
        for(String file: filesNames){
            objectWriter.write(filesCache.getFile(file), file);
        }
    }

    /**
     * This method writes the dictionary to the dictionary file
     */
    private void writeDictionary(){
        StringBuilder dic = new StringBuilder();
        List<String> words = new ArrayList<>(dictionary.keySet());
        words.sort(String::compareTo);
        for(String word: words)
            dic.append(word + ";" + dictionary.get(word)[TF_INDEX] + ";" + dictionary.get(word)[FILE_LINE_INDEX] + "\n");
        List<StringBuilder> toWrite = new ArrayList<>();
        toWrite.add(dic);
        objectWriter.write(toWrite, outputDir + "\\dictionary.txt");
    }

    /**
     * This function writes the bellow threshold terms to a file
     */
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

    /**
     * This method close all the indexer writes objects
     */
    public void closeWriter(){
        writeCache();
        writeDictionary();
        writeBellowThreshHold();
        writeDocsInfo();
        objectWriter.close();
    }

    //</editor-fold>

}