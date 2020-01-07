package Model.Retrieval;

import Model.Calculator;
import Model.Indexing.DocCorpusInfo;
import Model.Indexing.DocTermInfo;
import Model.Indexing.Term;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Ranker {

    public final double K = 1.6;
    public final double B = 0.6;
//    public final long MAX_DAYS_BETWEEN_DAYS = 373928;   //the oldest document

    private HashMap<String, DocCorpusInfo> docsDictionary;  //all relevant information about the documents

    public Ranker(HashMap<String, DocCorpusInfo> docsDictionary) {
        this.docsDictionary = docsDictionary;  //docNum, docLength, sumOfTermsSquare (doc normal) and possibly date
    }



    /**
     * This method receives queryTerms, a hashmap of term objects, according to their relevancy to the query.
     * The hashmap key is a integer from 0 to 5 and the value of each is an arrayList containing terms,
     * where the values of 0 are terms that appeared in the query.
     * If semantics is required: the values of i, i=1..5, are the ith terms similar in meaning to each term in the query.
     * For example, the value of 1 is an arrayList of the 1st similar word to each term in the query.
     *
     * @param queryTerms
     * @return 50 document numbers in which the terms given appeared in, ranked.
     */
    public ArrayList<String> rank(HashMap<Integer, ArrayList<Term>> queryTerms) {
        ArrayList<String> rankedDocs = new ArrayList<>();
        HashMap<String, Double>  docNumBM25 = new HashMap<>();
        for (int i = 0; i < 6; i++){
            ArrayList<Term> termsArray = queryTerms.get(i);
            for (Term trm: termsArray){
                HashMap<String, DocTermInfo> trmDocs = trm.getDocs(); //string= docNum
                for(Map.Entry<String, DocTermInfo> entry: trmDocs.entrySet()){
                    String docNum = entry.getKey();
                    DocTermInfo value = entry.getValue();
                    int tfi = value.getTfi();
                    int docLength = 0; //get doc length!
                    double idfBM25 = getIdfForBM25(trmDocs.size());
                    double bm25 = calculateBM25PerTerm(tfi,docLength,idfBM25);
                    docNumBM25.put(docNum, bm25);
                }


            }

        }

        return rankedDocs;
    }


    /**
     * This method calculates idf according to BM25
     * @param numOfDocsForTerm  number of docs the term appeared in
     * @return idf for term
     */
    private double getIdfForBM25(int numOfDocsForTerm) {
        double up = Calculator.getCorpusSize() - numOfDocsForTerm + 0.5;
        double down = numOfDocsForTerm + 0.5;
        return Math.log(up / down) / Math.log(2);
    }

    /**
     * This method calculated BM25 for each doc according to the BM25 formula
     * @param numOfOccurrecnes
     * @param docLength
     * @param idf
     * @return idf * up / down;
     */
    private double calculateBM25PerTerm(int numOfOccurrecnes, int docLength, double idf) {
        double up = numOfOccurrecnes * (K + 1);
        double down = numOfOccurrecnes + (K * (1 - B + (B * docLength / Calculator.averageDocLength())));
        return idf * up / down;
    }


    private double calulateCossinePerDoc() {
       // double result = Calculator.cosineSim(); //need to be pre calculated with query vec, doc vec, query len, doc len

        return 0;
    }


}
