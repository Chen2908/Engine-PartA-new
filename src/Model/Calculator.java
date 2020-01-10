package Model;

import java.math.BigDecimal;

/** This class does the calculations needed for the retrieval of terms
 * It includes the calculation of Tf-idf, cosine similarity
 */
public class Calculator {

    public static int corpusSize;
    public static long sumLength;


    /**
     * Calculate the Tf-idf for each term.
     * @param Df - document frequency, the number of different documents the term appeared in
     * @param fj - the number of times the term appear the document j
     * @param maxFj - the maximum appearances of a term in document j
     * @return TF-idf
     */
    public static double calculateTfIdf(int Df, int fj, int maxFj){
        if (maxFj == 0 || Df == 0)
            return -1;
        double tf = fj / maxFj;
        double idf = Math.log(corpusSize / Df);
        return tf * idf;
    }

    /**
     * Calculated the cosine similarity for the two given vectors, a value between 0 and 1.
     * The closer the calculated value to 1 the vectors and more similar to each other.
     * @param vector1
     * @param vector2
     * @return the value of cossim
     */
    public static double cosineSim(double [] vector1, double [] vector2, double vecLen1, double vecLen2){
        if (vector1.length != vector2.length){
            return -1;
        }
        double mult = 0;
        double cosSim;
        for (int i = 0; i < vector1.length; i++){
            mult += vector1[i] * vector2[i];
        }
        cosSim = mult / (Math.sqrt(vecLen1) * Math.sqrt(vecLen2));
        return cosSim;
    }


    public static void setSumLength(long sum) {
       sumLength = sum;
    }


    public static double averageDocLength(long sumLength, int corpusSize ){
        return sumLength/corpusSize;
    }

    public static void setCorpusSize(int corpusSize) {
        Calculator.corpusSize = corpusSize;
    }
}
