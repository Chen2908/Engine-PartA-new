package Model.Indexing;

import java.util.ArrayList;
import java.util.List;

public class DocCorpusInfo implements IWritable {

    //<editor-fold des="Fields">

    private static final int POW = 2;
    private int maxTf; // max term frequency
    private int numOfTerms;//the amount of term in the document
    private int numOfUniqTerms; // number of terms that appears one time in the doc
    private double sumOfTermsSquare; // the doc vector size square
    private final String del = ",";

    //</editor-fold>

    //<editor-fold des="Constructors">

    /**
     * Constructor
     */
    public DocCorpusInfo(){
        this.maxTf = 0;
        this.numOfUniqTerms = 0;
        this.sumOfTermsSquare = 0;
        this.numOfTerms = 0;
    }

    //</editor-fold>

    //<editor-fold des="Setters">

    /**
     * This function get term frequency and update the objects fields:
     * max term frequency, num of uniq terms, file vector size (sigma tf square)
     * @param tf - term frequency
     */
    public void updateDoc(int tf){
        setMaxTf(tf);
        increaseUniqTerms();
        addToVector(tf);
        this.numOfTerms += tf;
    }

    /**
     * This function sets new Max Term frequency, only if it is max
     * @param maxTf - the term that appears the most in the Document
     */
    private void setMaxTf(int maxTf) {
        if (maxTf > this.maxTf)
            this.maxTf = maxTf;
    }

    /**
     * This function increase the num of uniq words
     */
    private void increaseUniqTerms(){ this.numOfUniqTerms++; }

    /**
     * This function ands to the Document vector the given number of term square
     * @param tf - number of terms
     */
    private void addToVector(int tf){
        this.sumOfTermsSquare += Math.pow(tf, POW);
    }

    //</editor-fold>

    //<editor-fold des="Getters">

    /**
     * This function returns the Max term frequency of the Max term frequency
     * @return Max term frequency
     */
    public int getMaxTf() { return maxTf; }

    /**
     * This function returns the number of uniq word in the doc
     * @return number of uniq word in the doc
     */
    public int getNumOfUniqTerms() { return numOfUniqTerms; }

    /**
     * This function returns the vector that contains all the terms frequency square
     * @return vector size
     */
    public double getSumOfTermsSquare() { return sumOfTermsSquare; }

    //</editor-fold>

    //<editor-fold des="Interface Functions">

    /**
     * This function returns a StringBuilder of the object (the information to write to the file)
     * @return StringBuilder of the object information
     */
    public StringBuilder toFileString(){
        return new StringBuilder(maxTf + del + numOfTerms + del + numOfUniqTerms + del + sumOfTermsSquare);
    }

    /**
     * This function creates a list of the file information that needs to be written
     * @return List of the the file information
     */
    @Override
    public List<StringBuilder> toFile() {
        List<StringBuilder> toWrite = new ArrayList<>();
        toWrite.add(toFileString());
        return toWrite;
    }

    /**
     * This function gets a line of information about that object and updates the information
     * @param toUpdate - a list with one line of the doc information
     * @return List with with one line with the updated information
     */
    public List<StringBuilder> update(List<String> toUpdate) {
        String[] update = toUpdate.get(0).split(del);
        int updatedMaxTf = maxTf + Integer.parseInt(update[0]);
        int updatedNumOfUniqTerms = numOfUniqTerms + Integer.parseInt(update[1]);
        double updatedSumOfTermsSquare = sumOfTermsSquare + Double.parseDouble(update[2]);
        List<StringBuilder> toWrite = new ArrayList<>();
        StringBuilder line = new StringBuilder(updatedMaxTf + del);
        line.append(updatedNumOfUniqTerms + del);
        line.append(updatedSumOfTermsSquare);
        toWrite.add(line);
        return toWrite;
    }

    //</editor-fold>

}
