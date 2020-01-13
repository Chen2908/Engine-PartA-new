package Model.Retrieval;

import Model.Indexing.Term;

import java.io.*;
import java.util.*;

public class PostingReader {

    private int numOfPostingFiles;
    private File postingDirPath;
    private FileContentReader fileReader;
    private HashMap<String, Term> termsPosting;
    private HashMap<String, int[]> dictionary;

    private final int DIC_LINE_INDEX = 1;
    private final int DIC_TF_INDEX = 0;

    public PostingReader(HashMap<String, int[]> dictionary, String postingDirPath, int numOfPostingFiles){
        this.postingDirPath = new File(postingDirPath);
        this.fileReader = new FileContentReader();
        this.numOfPostingFiles = numOfPostingFiles;
        this.dictionary = new HashMap<>();
        this.termsPosting = new HashMap<>();
        this.dictionary = dictionary;
    }

    public HashMap<String, Term> getTermsPosting(Collection<String> terms){
        this.termsPosting = new HashMap<>();
        List<String> termsQueue = new ArrayList<>(terms);
        termsQueue.sort(Comparator.comparing(o -> fileNameHashFunction(o)));

        List<String> fileLines = null;
        String prevTermFilePath = null;
        String termValue;
        String termFilePath;

        while (termsQueue.size() > 0) {

            termValue = getTermValue(termsQueue.remove(0));
            if (termValue == null)
                continue;

            termFilePath = getFilePath(termValue);
            if (termFilePath != prevTermFilePath)
                fileLines = fileReader.getFileContent(termFilePath);
            prevTermFilePath = termFilePath;

            addTermToMap(fileLines, dictionary.get(termValue)[DIC_LINE_INDEX]);
        }

        return termsPosting;
    }

    /**
     * This method create new term from the term line in the posting file
     * and adds it to the terms map
     * @param lines - file lines
     * @param termLine - the line of the term information
     */
    private void addTermToMap(List<String> lines, Integer termLine){
        String termInfo = lines.get(termLine);
        Term term = new Term(termInfo);
        term.setTF(dictionary.get(term.getValue())[DIC_TF_INDEX]);
        this.termsPosting.put(term.getValue(), term);
    }

    /**
     * This method return the the term value like it appears in the dictionary
     * and returns null if the term doesn't exist.
     * @param termValue - the term to search
     * @return the term as it appears in the dictionary
     */
    private String getTermValue(String termValue){

        String termDicValue = null;

        if (dictionary.containsKey(termValue))
            termDicValue = termValue;
        else if (dictionary.containsKey(termValue.toLowerCase()))
            termDicValue = termValue.toLowerCase();
        else if (dictionary.containsKey(termValue.toUpperCase()))
            termDicValue = termValue.toUpperCase();

        return termDicValue;
    }

    private String getFilePath(String term){
        return postingDirPath.getAbsolutePath() + "\\" + fileNameHashFunction(term) + ".txt";
    }

    /**
     * This methods returns the hashcode of a given string
     * @param termValue - string
     * @return the hashcode ogf the given string
     */
    private int fileNameHashFunction(String termValue){
        return (Math.abs(termValue.toLowerCase().hashCode())) % numOfPostingFiles;
    }

}
