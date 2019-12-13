package Model;

/** This class does the calculations needed for the retrieval of terms
 * It includes the calculation of Tf-idf, cosine similarity
 */
public class Calculator {

    private static int corpusSize;

    public Calculator(int N) {
        this.corpusSize = N;
    }


    /**
     * Calculate the Tf-idf for each term.
     * @param Df - document frequency, the number of different documents the term appeared in
     * @param fj - the number of timed the term appear the document j
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
    public static double cosineSim(double [] vector1, double [] vector2){
        if (vector1.length != vector2.length){
            return -1;
        }
        double mult = 0;
        double vector1Len = 0;
        double vector2Len = 0;
        double cosSim;
        for (int i = 0; i < vector1.length; i++){
            mult += vector1[i] * vector2[i];
            vector1Len +=  Math.pow(vector1[i],2);
            vector2Len +=  Math.pow(vector2[i],2);
        }
        vector1Len = Math.sqrt(vector1Len);
        vector2Len = Math.sqrt(vector2Len);
        cosSim = mult / (vector1Len * vector2Len);
        return cosSim;
    }

}
