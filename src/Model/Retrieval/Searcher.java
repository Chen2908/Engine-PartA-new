package Model.Retrieval;

import Model.Indexing.DocCorpusInfo;
import Model.Indexing.Parse;
import Model.Indexing.Term;

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
    private HashMap<String, List<String>> semanticDictionary;
    private HashMap<String, DocCorpusInfo> docsInfo;

    private long sumOfDocsLength;
    private int numOfDocs;

    private final String del = ";";
    private final int DIC_TERM_INDEX = 0;
    private final int DIC_TF_INDEX = 1;
    private final int DIC_LINE_NUM_INDEX = 2;

    private final int DOC_NUM_INDEX = 0;

    private final String DOCSINFO_SUB_PATH = "\\Docs\\DocsInfo.txt";
    private final String POSTING_FILES_SUB_PATH = "\\Docs\\DocsInfo.txt";
    private final String DIC_SUB_PATH = "\\dictionary.txt";
    private final String SEM_DIC_SUB_PATH = "\\semanticDic.txt";


    public Searcher(String indexDirPath, String stopWordPath, int numOfPosting, boolean stemming){
        this.numOfDocs = 0;
        this.sumOfDocsLength = 0;
        this.docsInfo = new HashMap<>();
        this.indexDir = new File(indexDirPath);
        this.fileReader = new FileContentReader();
        this.semanticModel = new Semantics();
        this.parser = new Parse(stopWordPath, stemming);
        this.dictionary = new HashMap<>();
        this.semanticDictionary = new HashMap<>();
        readDictionary();
        readDocsInfoDic();
        readSemanticDictionary();
        this.postingReader = new PostingReader(dictionary,
                indexDir.getAbsolutePath() + POSTING_FILES_SUB_PATH, numOfPosting);
        this.ranker = new Ranker(docsInfo);
    }

    private void readDocsInfoDic(){
        List<String> dictionaryFile = fileReader.getFileContent(indexDir.getAbsolutePath() + DOCSINFO_SUB_PATH);
        String[] splitLine;
        for(String line : dictionaryFile){
            splitLine = line.split(del);
            DocCorpusInfo doc = new DocCorpusInfo(splitLine[1]);
            this.docsInfo.put(splitLine[DOC_NUM_INDEX], doc);
            this.sumOfDocsLength += doc.getNumOfTerms();
            this.numOfDocs++;
        }
    }

    private void readDictionary(){
        List<String> dictionaryFile = fileReader.getFileContent(indexDir.getAbsolutePath() + DIC_SUB_PATH);
        String[] splitLine;
        for(String line : dictionaryFile){
            splitLine = line.split(del);
            this.dictionary.put(splitLine[DIC_TERM_INDEX], Integer.parseInt(splitLine[DIC_LINE_NUM_INDEX]));
        }
    }

    private void readSemanticDictionary(){
        List<String> dictionaryFile = fileReader.getFileContent(indexDir.getAbsolutePath() + SEM_DIC_SUB_PATH);
        if (dictionaryFile == null)
            return;
        String[] splitLine;
        for(String line : dictionaryFile){
            splitLine = line.split(del);
            List<String> semList = new ArrayList<>();
            for (int i = 1; i < splitLine.length; i++)
                semList.add(splitLine[i]);
            this.semanticDictionary.put(splitLine[0], semList);
        }
    }
    
    public void handelQuery(String query){
        HashMap<String, Term> queryTerms = this.parser.parse(query, "","");
    }

    public HashMap<String, Integer> getDictionary() {
        return dictionary;
    }
}
