package Model.Retrieval;

import Model.Indexing.DocCorpusInfo;
import Model.Indexing.Parse;
import Model.Indexing.Term;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Searcher {

    private File indexDir;//holds the directory of the indexed file directory
    private FileContentReader fileReader;
    private PostingReader postingReader;
    private Semantic semanticModel;
    private Parse parser;
    private Ranker ranker;
    private HashMap<String, int[]> dictionary;
    private HashMap<String, DocCorpusInfo> docsInfo;

    private long sumOfDocsLength;
    private int isSemanticSearch;

    private final String del = ";";
    private final int DIC_TERM_INDEX = 0;
    private final int DIC_TF_INDEX = 1;
    private final int DIC_LINE_NUM_INDEX = 2;

    private final int DOC_NUM_INDEX = 0;


    private final String DOCSINFO_SUB_PATH = "\\Docs\\DocsInfo.txt";
    private final String POSTING_FILES_SUB_PATH = "\\Terms";
    private final String DIC_SUB_PATH = "\\dictionary.txt";
    private final String SEM_DIC_SUB_PATH = "\\semanticDic.txt";

    /**
     * Constructor
     * @param indexDirPath - the path to the index files directory
     * @param stopWordPath - the path to the stop word file
     * @param numOfPosting - the number of posting file
     * @param stemming - the type of semantic model to use
     */
    public Searcher(String indexDirPath, String stopWordPath, int numOfPosting, boolean stemming){
        this.sumOfDocsLength = 0;
        this.docsInfo = new HashMap<>();
        this.dictionary = new HashMap<>();
        this.indexDir = new File(indexDirPath);
        this.fileReader = new FileContentReader();
        this.parser = new Parse(stopWordPath, stemming);

        readDictionary();
        readDocsInfoDic();

        this.ranker = new Ranker(docsInfo, sumOfDocsLength);
        this.postingReader = new PostingReader(dictionary,
                indexDir.getAbsolutePath() + POSTING_FILES_SUB_PATH, numOfPosting);

    }

    /**
     * This method sets a semantic model
     * @param semanticsNum - type of semantic model
     */
    public void setSemanticModel(int semanticsNum) {
        this.isSemanticSearch = semanticsNum;
        if (semanticsNum == 1)
            this.semanticModel = new SemanticsModel();
        else if (semanticsNum == 2)
            this.semanticModel = new SemanticsAPI();
    }

    /**
     * This method reads the docs information
     */
    private void readDocsInfoDic(){
        List<String> dictionaryFile = fileReader.getFileContent(indexDir.getAbsolutePath() + DOCSINFO_SUB_PATH);
        String[] splitLine;
        for(int i = 0; i < dictionaryFile.size() && dictionaryFile.get(i).compareTo("") != 0; i++){
            splitLine = dictionaryFile.get(i).split(del);
            DocCorpusInfo doc = new DocCorpusInfo(splitLine);
            this.docsInfo.put(splitLine[DOC_NUM_INDEX], doc);
            this.sumOfDocsLength += doc.getNumOfTerms();
        }
    }

    /**
     * This methods read the dictionary
     */
    private void readDictionary(){
        List<String> dictionaryFile = fileReader.getFileContent(indexDir.getAbsolutePath() + DIC_SUB_PATH);
        String[] splitLine;

        for(int i = 0; i < dictionaryFile.size() && dictionaryFile.get(i).compareTo("") != 0; i++){
            splitLine = dictionaryFile.get(i).split(del);
            int [] termDicInfo = { Integer.parseInt(splitLine[DIC_TF_INDEX]), Integer.parseInt(splitLine[DIC_LINE_NUM_INDEX]) };
            this.dictionary.put(splitLine[DIC_TERM_INDEX], termDicInfo);
        }
    }

    /**
     * This methods get a query, parse it, send it ot the ranker
     * and returns a sorted List of the the documents numbers
     * @param query - query to answer
     * @return documents sorted by the ranking
     */
    public List<Pair<String, Double>> search(String query){
        long sTime = System.currentTimeMillis();
        HashMap<String, Term> queryTermsMap = this.parser.parseQuery(query, "A-1"); //title
        ArrayList<String> queryTerms = new ArrayList<>(queryTermsMap.keySet());
        ArrayList<String> allTerms = new ArrayList<>(queryTerms);
        HashMap<String, Double> semTerms = null;

        if (this.isSemanticSearch > 0) {
            semTerms = getSemTerm(allTerms);
        }

        HashMap<String, Term> termsPosting = postingReader.getTermsPosting(allTerms);
        ArrayList<Term> queryTermPosting = new ArrayList<>();
        ArrayList<Pair<Term, Double>> semTermPosting = new ArrayList<>();

        for (String termTitle: termsPosting.keySet()){
            if (isTermInMap(queryTermsMap, termTitle))
                queryTermPosting.add(termsPosting.get(termTitle));
            else
                semTermPosting.add(new Pair(termsPosting.get(termTitle), semTerms.get(termTitle.toLowerCase())));
        }
        List<Pair<String, Double>> docs = this.ranker.rank(queryTermPosting, semTermPosting);
        System.out.println((System.currentTimeMillis() - sTime));
        return docs;
    }

    /**
     * This methods checks if the given term is found it the given
     * HashMap keys
     * @param map - HashMap to search in
     * @param term - value to search for
     * @return if the term appears in the mpa in any way (regular, lower, upper)
     */
    private boolean isTermInMap(HashMap<String, Term> map, String term){
        return map.containsKey(term) || map.containsKey(term.toUpperCase()) || map.containsKey(term.toLowerCase());
    }

    /**
     * THis method gets a list of term and returns a list of all the terms that
     * the semantic model found closest to them
     * @param queryTerms
     * @return all the terms that the semantic model found similar to the given terms
     */
    private HashMap<String, Double> getSemTerm(List<String> queryTerms){
        HashMap<String, Double> semTerms = new HashMap<>();
        List<Pair<String, Double>> semTermList;
        for (String term: queryTerms){
            semTermList = semanticModel.termWithSimilarMeaning(term.toLowerCase());

            for (Pair<String, Double> pair: semTermList)
                semTerms.put(pair.getKey(), pair.getValue());
        }
        for (String term: semTerms.keySet())
            queryTerms.add(term);

        return semTerms;
    }

    /**
     * Getter
     * @return returns the HashMap of the dictionary
     */
    public HashMap<String, int[]> getDictionary() {
        return dictionary;
    }

    /**
     * Getter
     * @return the an HashMap of all the entities for all the documents
     */
    public HashMap<String, List<String>> getEntities() {
        HashMap<String, List<String>> entities = new HashMap<>();
        for (String s: docsInfo.keySet()){
            entities.put(DocCorpusInfo.getDocDecimalNum(s), docsInfo.get(s).getMostFreqEntities());
        }
        return entities;
    }

}
