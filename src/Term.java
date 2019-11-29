/**
 * This class represents a term in the corpus
 */

public class Term {

    private String value;
    private boolean startsWithCapital;
    private int df;  //amount of docs it appeared in
    private double tf; //amount of times it appeated

    public Term(String value) {
        this.value=value;
        setStartsWithCapital(value);
        df=1;
        tf=1;
    }

    public Term(String value, int df, int tf) {
        this.value=value;
        setStartsWithCapital(value);
        this.df=df;
        this.tf=tf;
    }

    private void setStartsWithCapital(String value) {
        startsWithCapital = Character.isUpperCase(value.charAt(0));
    }

    public void setDf(int df) {
        this.df = df;
    }

   public void setTf(int tf) {
        this.tf = tf;
    }

    public String getValue() {
        return value;
    }

    public boolean isStartsWithCapital() {
        return startsWithCapital;
    }

    public double getDf() {
        return df;
    }

    public double getTf() {
        return tf;
    }
}
