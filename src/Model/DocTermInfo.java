package Model;

public class DocTermInfo{

    //<editor-fold des="Class Fields">

    private String docNumPrefix;
    private String docNumSuffix;
    private int tfi; // number of times the term, that contains the Object, appears in this document.
    private StringBuilder termIndexes; // contains all the indexes where that term appears in the doc, separated by commas.
    private final String del = ",";
    private final String endDel = ";";

    //</editor-fold>

    //<editor-fold des="Constructor">

    public DocTermInfo(String docNum){
        String[] splitDocNum = docNum.split("-");
        this.docNumPrefix = splitDocNum[0];
        this.docNumSuffix = splitDocNum[1];
        this.tfi = 0;
        this.termIndexes = new StringBuilder();
    }

    //</editor-fold>

    //<editor-fold des="Setters">

    public void setTfi(int tfi) { this.tfi = tfi; }

    public void increaseTfi(){ this.tfi++; }

    public void addIndex(int index){
        if(this.termIndexes.length() != 0)
            this.termIndexes.append("," + index);
        else
            this.termIndexes.append(index);

    }

    //</editor-fold>

    //<editor-fold des="Getters">

    public String getDocNum() { return docNumPrefix + "-" + docNumSuffix; }

    public int getTfi() { return tfi; }

    public String getTermIndexes() { return termIndexes.toString(); }

    public String getDocNumSuffix(){ return docNumSuffix; }

    public String getDocNumPrefix(){ return docNumPrefix; }

    //</editor-fold>



    @Override
    // add Wij
    public String toString(){
        return getDocNumSuffix() + del + tfi + endDel;
    }

    //<editor-fold des="Interface Functions">

//    @Override
//    public List<String> toFile() {
//        List<String> toWrite = new ArrayList<String>();
//        toWrite.add(toString());
//        return toWrite;
//    }
//
//    @Override
//    public List<String> update(List<String> toUpdate) {
//        return toFile();
//    }


    //</editor-fold>

}
