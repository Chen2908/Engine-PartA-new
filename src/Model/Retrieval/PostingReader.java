package Model.Retrieval;

import Model.Indexing.Term;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public class PostingReader {

    private int numOfPostingFiles;
    private File postingDirPath;
    private HashMap<String, Term> terms;

    /**
     * Constructor
     * @param postingDirPath - path to the posting files directory
     */
    public PostingReader(String postingDirPath, int numOfPostingFiles){
        this.postingDirPath = new File(postingDirPath);
        this.numOfPostingFiles = numOfPostingFiles;
    }

    public HashMap<String, Term> getTermsPosting(Collection<String> terms){
        return null;
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
