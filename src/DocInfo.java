public class DocInfo {

    private String docNum;
    private int tfi; // number of times the term, that contains the Object, appears in this document.
    private StringBuilder termIndexes; // contains all the indexes where that term appears in the doc, separated by commas.

    public DocInfo(String docNum){
        this.docNum = docNum;
        this.tfi = 0;
        this.termIndexes = new StringBuilder();
    }

    public String getDocNum() { return docNum; }

    public int getTfi() { return tfi; }

    public void setTfi(int tfi) { this.tfi = tfi; }

    public void increaseTfi(){ this.tfi++; }

    public String getTermIndexes() { return termIndexes.toString(); }

    public void addIndex(int index){ this.termIndexes.append("," + index); }

    @Override
    public String toString(){
        return this.docNum + ":" + this.tfi + ":" + getTermIndexes() + ";";
    }

}
