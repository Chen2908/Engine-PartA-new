package Model.Retrieval;

import Model.Indexing.DocCorpusInfo;
import Model.Indexing.Parse;
import Model.Indexing.Term;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Searcher {

    private File indexDir;
    private FileContentReader fileReader;
    private PostingReader postingReader;
    private Semantics semanticModel;
    private Parse parser;
    private Ranker ranker;
    private HashMap<String, Integer> dictionary;
    private HashMap<String, List<Pair<String, Double>>> semanticDictionary;
    private HashMap<String, DocCorpusInfo> docsInfo;

    private long sumOfDocsLength;
    private boolean isSemanticSearch;

    private final String del = ";";
    private final int DIC_TERM_INDEX = 0;
    private final int DIC_TF_INDEX = 1;
    private final int DIC_LINE_NUM_INDEX = 2;

    private final int DOC_NUM_INDEX = 0;

    private final String DOCSINFO_SUB_PATH = "\\Docs\\DocsInfo.txt";
    private final String POSTING_FILES_SUB_PATH = "\\Terms";
    private final String DIC_SUB_PATH = "\\dictionary.txt";
    private final String SEM_DIC_SUB_PATH = "\\semanticDic.txt";


    public Searcher(String indexDirPath, String stopWordPath, int numOfPosting, boolean stemming, boolean semantics){
        this.sumOfDocsLength = 0;
        this.isSemanticSearch = semantics;
        this.docsInfo = new HashMap<>();
        this.dictionary = new HashMap<>();
        this.semanticDictionary = new HashMap<>();

        this.indexDir = new File(indexDirPath);
        this.fileReader = new FileContentReader();

        this.parser = new Parse(stopWordPath, stemming);

        readDictionary();
        readDocsInfoDic();

        this.ranker = new Ranker(docsInfo, sumOfDocsLength);
        this.postingReader = new PostingReader(dictionary,
                indexDir.getAbsolutePath() + POSTING_FILES_SUB_PATH, numOfPosting);

        if (semantics) {
            this.semanticModel = new Semantics();
            readSemanticDictionary();
        }

    }

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

    private void readDictionary(){
        List<String> dictionaryFile = fileReader.getFileContent(indexDir.getAbsolutePath() + DIC_SUB_PATH);
        String[] splitLine;

        for(int i = 0; i < dictionaryFile.size() && dictionaryFile.get(i).compareTo("") != 0; i++){
            splitLine = dictionaryFile.get(i).split(del);
            this.dictionary.put(splitLine[DIC_TERM_INDEX], Integer.parseInt(splitLine[DIC_LINE_NUM_INDEX]));
        }
    }

    private void readSemanticDictionary(){
        List<String> dictionaryFile = fileReader.getFileContent(indexDir.getAbsolutePath() + SEM_DIC_SUB_PATH);
        if (dictionaryFile == null)
            return;
        String[] splitLine;
        for(String line : dictionaryFile){
            splitLine = StringUtils.split(line, ",");
            List<Pair<String,Double>> semList = new ArrayList<>();
            for (int i = 1; i < splitLine.length; i += 2)
                semList.add(new Pair<>(splitLine[i], Double.parseDouble(splitLine[i+1])));
            this.semanticDictionary.put(splitLine[0], semList);
        }
    }
    
    public List<Pair<String, Double>> search(String query){
        HashMap<String, Term> queryTermsMap = this.parser.parse(query, "A-1", "");
        ArrayList<String> queryTerms = new ArrayList<>(queryTermsMap.keySet());
        ArrayList<String> allTerms = new ArrayList<>(queryTerms);
        HashMap<String, Double> semTerms = null;
        if (this.isSemanticSearch)
            semTerms = getSemTerm(allTerms);
        HashMap<String, Term> termsPosting = postingReader.getTermsPosting(allTerms);
        ArrayList<Term> queryTermPosting = new ArrayList<>();
        ArrayList<Pair<Term, Double>> semTermPosting = new ArrayList<>();
        for (String term: allTerms){
            if (queryTermsMap.containsKey(term))
                queryTermPosting.add(termsPosting.get(term));
            else
                semTermPosting.add(new Pair(termsPosting.get(term), semTerms.get(term)));
        }
        return this.ranker.rank(queryTermPosting, semTermPosting);
    }

    private HashMap<String, Double> getSemTerm(List<String> queryTerms){
        HashMap<String, Double> semTerms = new HashMap<>();
        List<Pair<String, Double>> semTermList;
        for (String term: queryTerms){
            if (this.semanticDictionary.containsKey(term))
                semTermList = this.semanticDictionary.get(term);
            else
                semTermList = semanticModel.termWithSimilarMeaning(term);
            for (Pair<String, Double> pair: semTermList)
                semTerms.put(pair.getKey(), pair.getValue());
        }
        for (String term: semTerms.keySet())
            queryTerms.add(term);

        return semTerms;
    }

    public HashMap<String, Integer> getDictionary() {
        return dictionary;
    }
}
