public class DocTermInfo {

    //<editor-fold des="Class Fields">

    private String docNum;
    private int tfi; // number of times the term, that contains the Object, appears in this document.
    private StringBuilder termIndexes; // contains all the indexes where that term appears in the doc, separated by commas.

    //</editor-fold>

    //<editor-fold des="Constructor">

    public DocTermInfo(String docNum){
        this.docNum = docNum;
        this.tfi = 0;
        this.termIndexes = new StringBuilder();
    }

    //</editor-fold>

    //<editor-fold des="Setters">

    public void setTfi(int tfi) { this.tfi = tfi; }

    public void increaseTfi(){ this.tfi++; }

    public void addIndex(int index){ this.termIndexes.append("," + index); }

    //</editor-fold>

    //<editor-fold des="Getters">

    public String getDocNum() { return docNum; }

    public int getTfi() { return tfi; }

    public String getTermIndexes() { return termIndexes.toString(); }

    //</editor-fold>

    @Override
    public String toString(){
        return this.docNum + ":" + this.tfi + ":" + getTermIndexes() + ";";
    }

}
