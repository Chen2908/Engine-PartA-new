package Model;

import java.util.ArrayList;
import java.util.List;

public class DocCorpusInfo implements IWritable {

    private static final int POW = 2;

    //<editor-fold des="Fields">

    private int maxTf; // max term frequency
    private int numOfUniqTerms; // number of terms that appears one time in the doc
    private double sumOfTermsSquare; // the doc vector size square
    private final String del = ",";

    //</editor-fold>

    //<editor-fold des="Constructors">

    public DocCorpusInfo(){
        this.maxTf = 0;
        this. numOfUniqTerms = 0;
        this.sumOfTermsSquare = 0;
    }

    //</editor-fold>

    //<editor-fold des="Setters">

    public void setMaxTf(int maxTf) {
        if (maxTf > this.maxTf)
            this.maxTf = maxTf;
    }

    public void increaseUniqTerms(){ this.numOfUniqTerms++; }

    public void addToVector(int numOfTerms){
        this.sumOfTermsSquare += Math.pow(numOfTerms, POW);
    }

    //</editor-fold>

    //<editor-fold des="Getters">

    public int getMaxTf() { return maxTf; }

    public int getNumOfUniqTerms() { return numOfUniqTerms; }

    public double getSumOfTermsSquare() { return sumOfTermsSquare; }

    //</editor-fold>

    //<editor-fold des="Interface Functions">

    @Override
    // add
    public String toString(){
        return maxTf + del + numOfUniqTerms + del + sumOfTermsSquare;
    }

    @Override
    public List<String> toFile() {
        List<String> toWrite = new ArrayList<String>();
        toWrite.add(toString());
        return toWrite;
    }

    public List<String> update(List<String> toUpdate) {
        String[] update = toUpdate.get(0).split(del);
        int updatedMaxTf = maxTf + Integer.parseInt(update[0]);
        int updatedNumOfUniqTerms = numOfUniqTerms + Integer.parseInt(update[1]);
        double updatedSumOfTermsSquare = sumOfTermsSquare + Double.parseDouble(update[2]);
        List<String> toWrite = new ArrayList<>();
        toWrite.add(updatedMaxTf + del + updatedNumOfUniqTerms + del + updatedSumOfTermsSquare);
        return toWrite;
    }

    //</editor-fold>

}
