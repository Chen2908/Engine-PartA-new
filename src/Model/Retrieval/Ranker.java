package Model.Retrieval;

import Model.Calculator;
import Model.Indexing.DocCorpusInfo;
import Model.Indexing.DocTermInfo;
import Model.Indexing.Term;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ranker {

    private final double K = 0.05;
    private final double B = 0.1;
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
    }


    /**
     * This method receives queryTerms, an arrayList of term objects, that appeared the query, and semanticTerms,
     * an arrayList of terms that are closed to the terms in queryTerms, and their score.
     * The method ranks the documents these terms appeared in and returns 50 first documents.
     *
     *
     * @return 50 document numbers in which the terms given appeared in, ranked.
     */
    public List<Pair<String, Double>> rank(ArrayList<Term> queryTerms, ArrayList<Pair<Term, Double>> semanticTerms) {
        if (semanticTerms.size() == 0)
            this.semantics = false;

        HashMap<String, List<Pair<Integer, Integer>>> docTFDF = new HashMap<>();
        HashMap<String, Double> docNumBM25Query1 = new HashMap<>(); //docNum-> bm25
        HashMap<String, Integer> docNoInHeadLine = new HashMap<>();
        HashMap<String, Integer> docNoInTheBeggining = new HashMap<>();
        HashMap<String, Integer> docNoPartOfEntiry = new HashMap<>();

        HashMap<String, Double> finalRanks = new HashMap<>(); //docNum-> rank

        for (Term term : queryTerms) {
            HashMap<String, DocTermInfo> termDocs = term.getDocs(); //string= docNum
            for (String docNum : termDocs.keySet()) {
                List<Pair<Integer, Integer>> docList;
                if (docTFDF.containsKey(docNum)) {
                    docList = docTFDF.get(docNum);
                } else {
                    docList = new ArrayList<>();
                }
                docList.add(new Pair(termDocs.get(docNum).getTfi(), termDocs.size()));
                docTFDF.put(docNum, docList);

                checkAtTheBegining(docNum, termDocs.get(docNum), docNoInTheBeggining);
                checkInHeadline(docNum, termDocs.get(docNum), docNoInHeadLine);
                checkPartOFEntity(docNum, term, docNoPartOfEntiry);

            }
        }

        double max = -Double.MAX_VALUE;
        for (String docNum : docTFDF.keySet()) {
            double bm25 = calculateBM25PerDoc(docTFDF.get(docNum), docNum);
            docNumBM25Query1.put(docNum, bm25);
            if (bm25 > max)
                max = bm25;
        }

        for (String docNum : docNumBM25Query1.keySet()) {
            double normalBm = docNumBM25Query1.get(docNum) / max;
            double extraWeight1 = 0;
//            if (docNoInTheBeggining.containsKey(docNum))
//                extraWeight1 += docNoInTheBeggining.get(docNum);
            if (docNoInHeadLine.containsKey(docNum))
                extraWeight1 += 3*docNoInHeadLine.get(docNum);
            if (docNoPartOfEntiry.containsKey(docNum))
                extraWeight1 += docNoPartOfEntiry.get(docNum);
            extraWeight1 /= docsDictionary.get(docNum).getNumOfUniqTerms();
            finalRanks.put(docNum, 0.8 * normalBm + extraWeight1 * 0.2);

        }

        List<Pair<String, Double>> docRanks = new ArrayList<>();
        for (String doc : finalRanks.keySet()) {
            docRanks.add(new Pair<>(DocCorpusInfo.getDocDecimalNum(doc), finalRanks.get(doc)));
        }

        //sort docs according to weight
        docRanks.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        int minLength = Math.min(docRanks.size(), MAX_DOCS_TO_RETURN);
        return docRanks.subList(0, minLength);

    }

    private void checkPartOFEntity(String docNum, Term term, HashMap<String, Integer> docNoPartOfEntiry) {
        if (docPartOfEntities(docNum, term)) {
            int partOf;
            if (docNoPartOfEntiry.containsKey(docNum))
                partOf = docNoPartOfEntiry.get(docNum) + 1;
            else
                partOf = 1;
            docNoPartOfEntiry.put(docNum, partOf);
        }
    }

    private boolean docPartOfEntities(String docNo, Term term) {
        for (String entity : docsDictionary.get(docNo).getMostFreqEntities()) {
            if (StringUtils.containsIgnoreCase(entity, term.getValue()))
                return true;
        }
        return false;
    }

    private void checkAtTheBegining(String docNum, DocTermInfo dti, HashMap<String, Integer> docNoInTheBeggining) {
        if ((double) dti.getTermFirstIndex() / docsDictionary.get(docNum).getNumOfTerms() < 0.1) {
            int atBegining;
            if (docNoInTheBeggining.containsKey(docNum))
                atBegining = docNoInTheBeggining.get(docNum) + 1;
            else
                atBegining = 1;
            docNoInTheBeggining.put(docNum, atBegining);
        }
    }

    private void checkInHeadline(String docNum, DocTermInfo dti, HashMap<String, Integer> docNoInHeadLine) {
        if (dti.getIsInHeadLine()) {
            int inHeadline;
            if (docNoInHeadLine.containsKey(docNum))
                inHeadline = docNoInHeadLine.get(docNum) + 1;
            else
                inHeadline = 1;
            docNoInHeadLine.put(docNum, inHeadline);
        }
    }


    private double calculateBM25PerDoc(List<Pair<Integer, Integer>> pairs, String docNum) {
        double result = 0;
        for (Pair<Integer, Integer> pair : pairs) {
            double tf = (double) pair.getKey() / docsDictionary.get(docNum).getMaxTf();
            double idf = Math.log((Calculator.corpusSize - pair.getValue() + 0.5) / (pair.getValue() + 0.5));
            double up = tf * (K + 1) * idf;
            double average = Calculator.sumLength / Calculator.corpusSize;
            int uniqueTerms = docsDictionary.get(docNum).getNumOfUniqTerms();
            double down = tf + (K * (1 - B + (B * ((double) uniqueTerms / average))));
            result += up / down;
        }
        return result;
    }
}



