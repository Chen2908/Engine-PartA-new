package Model.Retrieval;

import Model.Calculator;
import Model.Indexing.DocCorpusInfo;
import Model.Indexing.DocTermInfo;
import Model.Indexing.Term;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

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
    public Ranker(HashMap<String, DocCorpusInfo> docsDictionary) {
        this.docsDictionary = docsDictionary;  //docNum, docLength, sumOfTermsSquare (doc normal) and possibly date
        this.semantics = true;
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
    public ArrayList<Pair<String, Double>> rank(ArrayList<Term> queryTerms, ArrayList<Pair<Term, Integer>> semanticTerms) {
        if (semanticTerms == null)
            this.semantics = false;

        HashMap<String, Double> docNumBM25Query = new HashMap<>(); //docNum-> bm25
        HashMap<String, Double> docNumBM25QueryPlusSemantics= new HashMap<>(); //docNum-> weighted bm25
        HashMap<String, HashMap<Term, Integer>> docNumTermFirstIndex = new HashMap<>();

        for (Term term : queryTerms) {
            HashMap<String, DocTermInfo> termDocs = term.getDocs(); //string= docNum
            for (String docNum : termDocs.keySet()) {
                double bmVal = 0;
                DocTermInfo value = termDocs.get(docNum);
                int firstIndex = value.getTermFirstIndex();
                double bm25 = calculateBM25PerTerm(value.getTfi(), docsDictionary.get(docNum).getNumOfTerms(), getIdfForBM25(termDocs.size()));
                if (docNumBM25Query.containsKey(docNum)) {
                    bmVal = docNumBM25Query.get(docNum) + bm25;
                } else {
                    bmVal = bm25;
                }
                docNumBM25Query.put(docNum, bmVal);

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
        }
        if (semantics) {
            docNumBM25QueryPlusSemantics.putAll(docNumBM25Query);
            for (Pair<Term, Integer> semanticPair : semanticTerms) {
                Term term = semanticPair.getKey();
                HashMap<String, DocTermInfo> trmDocs = term.getDocs(); //string= docNum
                int termScore = semanticPair.getValue();
                double normalizedScore = termScore / 120000;
                //for each doc
                for (String docNum : trmDocs.keySet()) {
                    double bmSemantics;
                    DocTermInfo value = trmDocs.get(docNum);
                    double bm25 = calculateBM25PerTerm(value.getTfi(), docsDictionary.get(docNum).getNumOfTerms(), getIdfForBM25(trmDocs.size()));
                    //doc exists
                    if (!docNumBM25QueryPlusSemantics.containsKey(docNum)) {
                        bmSemantics = bm25 * normalizedScore;
                    } else {
                        bmSemantics = docNumBM25QueryPlusSemantics.get(docNum) + bm25 * normalizedScore;
                    }
                    docNumBM25QueryPlusSemantics.put(docNum, bmSemantics);
                    int firstIndex = value.getTermFirstIndex();
                    HashMap<Term,Integer> index;
                    if (!docNumTermFirstIndex.containsKey(docNum)){
                        index = new HashMap<>();
                    }
                    else {
                        index = docNumTermFirstIndex.get(docNum);
                    }
                    index.put(term, firstIndex);
                    docNumTermFirstIndex.put(docNum, index);
                }

            }
        }
        return rankDocs(docNumBM25Query, docNumBM25QueryPlusSemantics,docNumTermFirstIndex);
    }

    private ArrayList<Pair<String, Double>> rankDocs(HashMap<String, Double> docNumBM25Query, HashMap<String, Double> docNumBM25Semantics, HashMap<String, HashMap<Term, Integer>> docNumTermFirstIndex) {
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
        return (ArrayList)rankedDocs.subList(0, MAX_DOCS_TO_RETURN);
    }


    /**
     * This method calculates idf according to BM25
     *
     * @param numOfDocsForTerm number of docs the term appeared in
     * @return idf for term
     */
    private double getIdfForBM25(int numOfDocsForTerm) {
        double up = Calculator.getCorpusSize() - numOfDocsForTerm + 0.5;
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
        double up = numOfOccurrecnes * (K + 1);
        double down = numOfOccurrecnes + (K * (1 - B + (B * docLength / Calculator.averageDocLength())));
        return idf * up / down;
    }


}
