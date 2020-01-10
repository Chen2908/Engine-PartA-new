package Model.Retrieval;

import Model.Calculator;
import Model.Indexing.DocCorpusInfo;
import Model.Indexing.DocTermInfo;
import Model.Indexing.Term;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ranker {

    private final double K = 1.25;
    private final double B = 0.75;
    private final int MAX_DOCS_TO_RETURN = 50;
    private HashMap<String, DocCorpusInfo> docsDictionary;  //all relevant information about the documents
    private boolean semantics;

    /**
     * constructor
     *
     * @param docsDictionary
     */
    public Ranker(HashMap<String, DocCorpusInfo> docsDictionary, long sumLengths) {
        this.docsDictionary = docsDictionary;  //docNum, docLength, sumOfTermsSquare (doc normal) and possibly date
        Calculator.setCorpusSize(docsDictionary.size());
        Calculator.setSumLength(sumLengths);
        this.semantics = true;
    }


    /**
     * This method receives queryTerms, an arrayList of term objects, that appeared the query, and semanticTerms,
     * an arrayList of terms that are closed to the terms in queryTerms, and their score.
     * The method ranks the documents these terms appeared in and returns 50 first documents.
     * @param queryTerms
     * @param semanticTerms
     * @return 50 document numbers in which the terms given appeared in, ranked.
     */
    public List<Pair<String, Double>> rank(ArrayList<Term> queryTerms, ArrayList<Pair<Term, Double>> semanticTerms) {
        if (semanticTerms.size() == 0)
            this.semantics = false;

        HashMap<String, Double> docNumBM25Query = new HashMap<>(); //docNum-> bm25
        HashMap<String, Double> docNumBM25QueryPlusSemantics= new HashMap<>(); //docNum-> weighted bm25
        HashMap<String, HashMap<Term, Integer>> docNumTermFirstIndex = new HashMap<>();

        for (Term term : queryTerms) {
            HashMap<String, DocTermInfo> termDocs = term.getDocs(); //string= docNum
            for (String docNum : termDocs.keySet()) {
                putDocNumBM(termDocs.get(docNum), docNum, docNumBM25Query, termDocs.size(),1);
                int firstIndex = termDocs.get(docNum).getTermFirstIndex();
                putDocNumIndex(term, docNum, firstIndex,docNumTermFirstIndex);
            }
        }
        if (semantics) {
            docNumBM25QueryPlusSemantics.putAll(docNumBM25Query);
            for (Pair<Term, Double> semanticPair : semanticTerms) {
                Term term = semanticPair.getKey();
                HashMap<String, DocTermInfo> trmDocs = term.getDocs(); //string= docNum
                double termScore = semanticPair.getValue();
                //for each doc
                for (String docNum : trmDocs.keySet()) {
                    putDocNumBM(trmDocs.get(docNum), docNum, docNumBM25QueryPlusSemantics, trmDocs.size(), termScore);
                    int firstIndex = trmDocs.get(docNum).getTermFirstIndex();
                    putDocNumIndex(term, docNum, firstIndex,docNumTermFirstIndex);
                }
            }
        }
        return rankDocs(docNumBM25Query, docNumBM25QueryPlusSemantics,docNumTermFirstIndex);
    }


    /**
     * Puts pairs of docNum, BM25 score in the hashmap
     * @param value - DocTermInfo object
     * @param docNum
     * @param docNumBM25Query  - insert hashmap
     * @param size - number of documents that term appeared in
     * @param weight  - 1 if it is a term that appeared in the query, normalized score if it is a semantic term
     */
    private void putDocNumBM(DocTermInfo value, String docNum, HashMap<String, Double> docNumBM25Query, int size, double weight){
        double bmVal = 0;
        int firstIndex = value.getTermFirstIndex();
        double bm25 = calculateBM25PerTerm(value.getTfi(), docsDictionary.get(docNum).getNumOfTerms(), getIdfForBM25(size));
        if (docNumBM25Query.containsKey(docNum)) {
            bmVal = docNumBM25Query.get(docNum) + bm25 * weight;
        } else {
            bmVal = bm25 * weight;
        }
        docNumBM25Query.put(docNum, bmVal);
    }

    /**
     * Puts docNum, <Term, first index> in hashmap
     * @param term
     * @param docNum
     * @param firstIndex
     * @param docNumTermFirstIndex - insert hashmap
     */
    private void putDocNumIndex(Term term, String docNum, int firstIndex, HashMap<String, HashMap<Term, Integer>> docNumTermFirstIndex){
        HashMap<Term,Integer> index;
        if (docNumTermFirstIndex.containsKey(docNum)){
            index = docNumTermFirstIndex.get(docNum);
        }
        else {
            index = new HashMap<>();
        }
        index.put(term, firstIndex);
        docNumTermFirstIndex.put(docNum, index);
    }

    /**
     * Rank documents according to ranking formula
     * @param docNumBM25Query
     * @param docNumBM25Semantics
     * @param docNumTermFirstIndex
     * @return 50 pair of (docNum,score), sorted by score
     */
    private List<Pair<String, Double>> rankDocs(HashMap<String, Double> docNumBM25Query, HashMap<String, Double> docNumBM25Semantics, HashMap<String, HashMap<Term, Integer>> docNumTermFirstIndex) {
        ArrayList<Pair<String, Double>> rankedDocs = new ArrayList<>();
        HashMap<String, Double> hashToWordOn;
        if (semantics)
            hashToWordOn = docNumBM25Semantics;  //combined score
        else
            hashToWordOn = docNumBM25Query;

        for (String docNum : hashToWordOn.keySet()) {
            double sumWeightForDoc = 0;
            double score = hashToWordOn.get(docNum);
            for (Term term : docNumTermFirstIndex.get(docNum).keySet()){
                DocCorpusInfo dci = docsDictionary.get(docNum);
                double tfidf = Calculator.calculateTfIdf(term.getDf(), term.getDocTfi(docNum), dci.getMaxTf());
                double normalizedIndex = (double)docNumTermFirstIndex.get(docNum).get(term) / dci.getNumOfTerms();

                sumWeightForDoc += tfidf * (1 - normalizedIndex);
            }
//            double rank = Math.sqrt(score) + sumWeightForDoc;
            double rank = score + sumWeightForDoc;
            rankedDocs.add(new Pair(docNum, rank));
        }

        //sort docs according to weight
        rankedDocs.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        int minLength = Math.min(rankedDocs.size(), MAX_DOCS_TO_RETURN);
        return rankedDocs.subList(0, minLength);
    }


    /**
     * This method calculates idf according to BM25
     *
     * @param numOfDocsForTerm number of docs the term appeared in
     * @return idf for term
     */
    private double getIdfForBM25(int numOfDocsForTerm) {
        double up = Calculator.corpusSize - numOfDocsForTerm + 0.5;
        double down = numOfDocsForTerm + 0.5;
        return Math.log(up / down) / Math.log(2);
    }

    /**
     * This method calculated BM25 for each doc according to the BM25 formula
     *
     * @param numOfOccurrecnes
     * @param docLength
     * @param idf
     * @return idf * up / down;
     */
    private double calculateBM25PerTerm(int numOfOccurrecnes, int docLength, double idf) {
        long sumLengths = Calculator.sumLength;
        int cospusSize = Calculator.corpusSize;
        double up = numOfOccurrecnes * (K + 1);
        double down = numOfOccurrecnes + (K * (1 - B + (B * docLength / Calculator.averageDocLength(sumLengths,cospusSize))));
        return idf * up / down;
    }

    //cossim?


}
