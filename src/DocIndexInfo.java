public class DocIndexInfo {

    private static final int POW = 2;

    //<editor-fold des="Fields">

    private int maxTf; // max term frequency
    private int numOfUniqTerms; // number of terms that appears one time in the doc
    private double sumOfTermsSquare; // the doc vector size square

    //</editor-fold>

    //<editor-fold des="Constructors">

    public DocIndexInfo(){
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
}
