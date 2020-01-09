package Model.Indexing;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * This class represent a Term
 */
public class Term implements IWritable {

    //<editor-fold des="Class Fields">

    private String value;
    private HashMap<String, DocTermInfo> docs; // hashMap of all the docs that this term appears in
    private boolean startsWithCapital;
    private boolean isEntity;
    private int TF;//term frequency of the term in all the documents

    private String lastFileNumPrefix;
    private final String mainDel = "|";
    private final String docPrefixDel = "#";

    private final int VALUE_INDEX = 0;
    private final int DF_INDEX = 1;
    private final int DOCLIST_INDEX = 2;

    //</editor-fold>

    //<editor-fold des="Constructors">

    /**
     * Constructor
     * @param value - term Value
     * @param isEntity - if the term is an entity
     */
    public Term(String value, boolean isEntity) {
        setValue(value);
        docs = new HashMap<>();
        this.isEntity = isEntity;
        lastFileNumPrefix = "";
        TF = 0;
    }

    /**
     * Constructor
     * @param termInfo - term information
     */
    public Term(String termInfo) {

        docs = new HashMap<>();
        this.isEntity = false;
        lastFileNumPrefix = "";

        String[] splitByMainDel = StringUtils.split(termInfo, mainDel, 3);
        setValue(splitByMainDel[VALUE_INDEX]);

        addDocsInfo(splitByMainDel[DOCLIST_INDEX]);
    }

    private void addDocsInfo(String docInfoString){
        for (String docsPerPrefix: docInfoString.split(docPrefixDel)){
            String[] prefixInfoSplit = docsPerPrefix.split(":");
            lastFileNumPrefix = prefixInfoSplit[0];
            for (String docInfo: prefixInfoSplit[1].split(";")){
                DocTermInfo docTermInfo = new DocTermInfo(lastFileNumPrefix, docInfo);
                docs.put(docTermInfo.getDocNum(), docTermInfo);
            }

        }
    }

    //</editor-fold>

    //<editor-fold des="Setters">

    /**
     * This function sets the value of the term
     * @param value - term value
     */
    public void setValue(String value) {
        this.value = value;
        setStartsWithCapital(Character.isUpperCase(value.charAt(0)));
    }

    /**
     * This function sets if the term start with capital letter
     * @param startsWithCapital
     */
    private void setStartsWithCapital(boolean startsWithCapital) {
        this.startsWithCapital = startsWithCapital;
    }

    /**
     * This function gets a document number and index of the term in the given doc.
     * the method updates this document information
     * @param docNum - document number
     * @param index - the index of the term in the given document
     */
    public void updatesDocsInfo(String docNum, int index){
        DocTermInfo doc = new DocTermInfo(docNum);
        if (!docs.containsKey(doc.getDocNum()))
            docs.put(doc.getDocNum(), doc);
        doc = docs.get(doc.getDocNum());

        TF++;
        doc.increaseTfi();
        doc.addIndex(index);
    }

    //</editor-fold>

    //<editor-fold des="Getters">

    /** returns the Amount of docs this term appears in groups of files*/
    public int getDf() { return docs.size(); }

    /**
     * This method checks if the term is bellow the given threshold
     * @param numOfDocs - the number of documents of the threshold
     * @param totalNum - number of appearance threshold
     * @return if the term is bellow the threshold
     */
    public boolean isBellowThreshHold(int numOfDocs, int totalNum){

        if(isEntity && getDf() == 1)
            return true;

        if(docs.size() > numOfDocs || (isEntity && getDf() > 1))
            return false;

        return TF < totalNum;
    }

    /**
     * @return the term frequency in all documents
     */
    public int getTF(){
        return TF;
    }

    /**
     * This method gets another term and merge between the given term and this term
     * @param term - term to merge
     */
    public void merge(Term term){
        value = getUpdatedValue(term.value);
        this.TF += term.getTF();
        docs.putAll(term.getDocs());
    }

    /**
     * This function returns the docs of this term
     * @return the docs that this term appears in
     */
    public HashMap<String, DocTermInfo> getDocs() {
        return docs;
    }

    /**
     * This method returns the the term value
     * @return term value
     */
    public String getValue() { return value; }

    /**
     * This method returns if the term starts with capital letter
     * @return if the term starts with capital letter
     */
    public boolean isStartsWithCapital() { return startsWithCapital; }

    /**
     * This method returns if the term is entity
     * @return if the term is entity
     */
    public boolean isEntity(){ return this.isEntity; }

    //<editor-fold des="Documents Information Getters">

    /**
     * This method returns the given document term frequency
     * @param docNum - doc number
     * @return term frequency of the given doc
     */
    public int getDocTfi(String docNum){ return docs.get(docNum).getTfi(); }

    /**
     * This method returns the indexes of the term in the given document
     * @param docNum - document number
     * @return the indexes of the term in the given document
     */
    public String getTermDocIndexes(String docNum){ return docs.get(docNum).getTermIndexes(); }

    //</editor-fold>

    //<editor-fold des="Documents Lists Getters">

    /**
     * This method sort all the docs by the documents number (String sort)
     * @return list of DocTermInfo objects sorted by number
     */
    public List<DocTermInfo> getDocsSortedByName(){
        ArrayList<DocTermInfo> docList = new ArrayList<>(docs.values());
        docList.sort(Comparator.comparing(DocTermInfo::getDocNum));
        return docList;
    }

    //</editor-fold>

    /**
     * @param docNum - document number
     * @return DocTermInfo object of the given doc, null if the object doesn't exists
     */
    public DocTermInfo getDoc(String docNum){ return docs.get(docNum); }

    //</editor-fold>

    //<editor-fold des="Interface Functions">

    /**
     * This methods writes all the Term information of the Term object to a StringBuilder
     * @return the StringBuilder of the term information
     */
    public StringBuilder toFileString(){
        StringBuilder termData = new StringBuilder();
        termData.append(getValue() + mainDel);
        termData.append(getDf() + mainDel);
        termData.append(getDocListString());
        return termData;
    }

    /**
     * This method create a new list with the object information
     * @return a list with one StringBuilder of the object
     */
    @Override
    public List<StringBuilder> toFile() {
        List<StringBuilder> toWrite = new ArrayList<>();
        toWrite.add(toFileString());
        return toWrite;
    }

    /**
     * This method gets a list of file lines and the index of the relevant line of the
     * current term, updates the line and return if to the file lines list
     * @param toUpdate - file lines
     * @param lineNum - the index o the relevant line
     */
    public void update(List<StringBuilder> toUpdate, int lineNum) {

        StringBuilder termPosting = toUpdate.remove(lineNum);
        String[] lineSplitToMainSections = StringUtils.split(termPosting.toString(), mainDel,3);
        StringBuilder updatedTermData = new StringBuilder();

        updatedTermData.append(getUpdatedValue(lineSplitToMainSections[VALUE_INDEX]) + mainDel);
        updatedTermData.append(getUpdatedDF(lineSplitToMainSections[DF_INDEX]) + mainDel);
        updatedTermData.append(getUpdatedDocListString(lineSplitToMainSections[DOCLIST_INDEX]));

        toUpdate.add(lineNum, updatedTermData);
    }

    /**
     * This method gets the previous term value and updates
     * @param prevValue
     * @return
     */
    private String getUpdatedValue(String prevValue){
        if(isEntity || value.compareTo(prevValue.toLowerCase()) == 0)
            return value;
        return prevValue;
    }

    /**
     * This methods gets the previous document frequency and updates the document frequency
     * @param prevDF - the previous document frequency
     * @return the new document frequency
     */
    private String getUpdatedDF(String prevDF){
        return (Integer.parseInt(prevDF) + getDf()) + "";
    }

    /**
     * This methods return string of the documents list
     * @returns tring of the documents list
     */
    private StringBuilder getDocListString(){
        return getUpdatedDocListString("").replace(0,1, "");
    }

    /**
     * This method creates a StringBuilder of the combined documents list, the given(previous) and the current
     * @param prevDocList - previous document list
     * @return combined documents list
     */
    private StringBuilder getUpdatedDocListString(String prevDocList){

        StringBuilder newDocList = new StringBuilder(prevDocList);

        for(DocTermInfo docInfo: getDocsSortedByName())

            if(docInfo.getDocNumPrefix().compareTo(lastFileNumPrefix) == 0)
                newDocList.append(docInfo.toFileString());
            else {
                lastFileNumPrefix = docInfo.getDocNumPrefix();
                newDocList.append(docPrefixDel + lastFileNumPrefix + ":" + docInfo.toFileString());
            }

        return newDocList;
    }


    //</editor-fold>

}
