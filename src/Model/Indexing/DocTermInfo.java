package Model.Indexing;

public class DocTermInfo{

    //<editor-fold des="Class Fields">

    private String docNumPrefix;
    private String docNumSuffix;
    private int tfi; // number of times the term, that contains the Object, appears in this document.
    private boolean isInHeadLine;
    private StringBuilder termIndexes; // contains all the indexes where that term appears in the doc, separated by commas.
    private final String del = ",";
    private final String endDel = ";";
    private int firstIndex;

    //</editor-fold>

    //<editor-fold des="Constructor">

    /**
     * Constructor
     * @param docNum - term number in documents format
     */
    public DocTermInfo(String docNum, boolean isInHeadLine){
        String[] splitDocNum = docNum.split("-");
        this.docNumPrefix = splitDocNum[0];
        this.docNumSuffix = String.format("%X", Integer.parseInt(splitDocNum[1]));
        this.tfi = 0;
        this.termIndexes = new StringBuilder();
        this.isInHeadLine = isInHeadLine;
    }

    public DocTermInfo(String docNum){
        String[] splitDocNum = docNum.split("-");
        this.docNumPrefix = splitDocNum[0];
        this.docNumSuffix = String.format("%X", Integer.parseInt(splitDocNum[1]));
        this.tfi = 0;
        this.termIndexes = new StringBuilder();
        this.isInHeadLine = false;
    }

    public DocTermInfo(String prefix, String docInfo){
        this.termIndexes = new StringBuilder();
        String[] docInfoSplit = docInfo.split(del);
        docNumPrefix = prefix;
        docNumSuffix = docInfoSplit[0];
        tfi = Integer.parseInt(docInfoSplit[1]);
        termIndexes.append(docInfoSplit[2]);
        setInHeadLine(docInfoSplit[3]);
    }

    //</editor-fold>

    //<editor-fold des="Setters">

    /**
     * This method sets new term frequency
     * @param tfi - the term frequency to set
     */
    public void setTfi(int tfi) { this.tfi = tfi; }

    /**
     * This method increase the term frequency by one
     */
    public void increaseTfi(){ this.tfi++; }

    /**
     * This method adds new index to the indexes String
     * @param index - the index to be added
     */
    public void addIndex(int index){
        if(this.termIndexes.length() != 0)
            this.termIndexes.append(del + index);
        else {
            this.termIndexes.append(index);
            this.firstIndex = index;
        }

    }

    public void merge (DocTermInfo doc){
        this.tfi += doc.getTfi();
        if (firstIndex > doc.getTermFirstIndex())
           this.firstIndex = doc.getTermFirstIndex();
        if (doc.getIsInHeadLine())
            isInHeadLine = true;
    }

    public void setInHeadLine(boolean inHeadLine) {
        isInHeadLine = inHeadLine;
    }

    public void setTermFirstIndex(int index) {
        this.firstIndex = index;
    }

    private void setInHeadLine(String isInHeadLine){
        if (isInHeadLine.compareTo("1") == 0)
            this.isInHeadLine = true;
        else
            this.isInHeadLine = false;
    }

    //</editor-fold>

    //<editor-fold des="Getters">

    /**
     * This method returns the whole document number
     * @return the whole document number
     */
    public String getDocNum() { return docNumPrefix + "-" + docNumSuffix; }

    /**
     * This function returns the term frequency
     * @return the term frequency
     */
    public int getTfi() { return tfi; }

    /**
     * This method returns the indexes string
     * @return
     */
    public String getTermIndexes() { return termIndexes.toString(); }

    /**
     * This method returns the document number suffix(after the hyphen)
     * @return the document number suffix
     */
    public String getDocNumSuffix(){ return docNumSuffix; }

    /**
     * This method returns the document number prefix(before the hyphen)
     * @return the document number prefix
     */
    public String getDocNumPrefix(){ return docNumPrefix; }

    public boolean getIsInHeadLine(){ return this.isInHeadLine; }

    private String getIsInHeadLineStr(){
        if (this.isInHeadLine)
            return "1";
        return "0";
    }
    //</editor-fold>

    /**
     * This method returns a StingBuilder of the document information
     * Document number , term frequency ;
     * @return StingBuilder of the document information
     */
    public StringBuilder toFileString(){
        return new StringBuilder(getDocNumSuffix() + del + tfi + del + getTermFirstIndex() + del + getIsInHeadLineStr() + endDel);
    }

    public int getTermFirstIndex() {
//        if (termIndexes != null && termIndexes.length() > 0)
//            return Integer.parseInt(StringUtils.split(termIndexes.toString(), del)[0]);
//        return -1;
        return firstIndex;
    }
}
