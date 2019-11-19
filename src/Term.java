/**
 * This class represents a term in the corpus
 */

public class Term {

    private String value;
    private boolean startsWithCapital;
    //private double Idf;
    //private double Tf;

    public Term(String value) {
        this.value=value;
        setStartsWithCapital(value);
       // Idf=0;
       // Tf=0;
    }

    public Term(String value, int Idf, int Tf) {
        this.value=value;
        setStartsWithCapital(value);
       // setIid(Idf);
       // setTf(Tf);
    }

    private void setStartsWithCapital(String value) {
        startsWithCapital = Character.isUpperCase(value.charAt(0));
    }

//   // public void setIid(double Idf) {
//        this.Idf = Idf;
//    }
//
//   // public void setTf(double Tf) {
//        this.Tf = Tf;
//    }

    public String getValue() {
        return value;
    }

    public boolean isStartsWithCapital() {
        return startsWithCapital;
    }

//    public double getIdf() {
//        return Idf;
//    }
//
//    public double getTf() {
//        return Tf;
//    }
}
