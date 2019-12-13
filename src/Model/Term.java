package Model;

import Model.DocTermInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Term implements IWritable {

    //<editor-fold des="Class Fields">

    private String value;
    private HashMap<String, DocTermInfo> docs; // hashMap of all the docs that this term appears in
    private boolean startsWithCapital;
    private boolean isEntity;
    private final String del = ",";
    private final String docPrefixDel = "#";
    private String lastFileNumPrefix;

    private final int VALUE_INDEX = 0;
    private final int DF_INDEX = 1;
    private final int DOCLIST_INDEX = 2;

    //</editor-fold>

    //<editor-fold des="Constructors">

    public Term(String value, boolean isEntity) {
        setValue(value);
        docs = new HashMap<>();
        this.isEntity = isEntity;
        lastFileNumPrefix = "";
    }

    public Term(String value) {
        setValue(value);
        docs = new HashMap<>();
        this.isEntity = false;
        lastFileNumPrefix = "";
    }

    //</editor-fold>

    //<editor-fold des="Setters">

    public void setValue(String value) {
        this.value = value;
        setStartsWithCapital(Character.isUpperCase(value.charAt(0)));
    }

    private void setStartsWithCapital(boolean startsWithCapital) {
        this.startsWithCapital = startsWithCapital;
    }

    public void updatesDocsInfo(String docNum, int index){
        DocTermInfo curDoc = docs.get(docNum);
        if (curDoc == null) {
            // creates a new document info object
            curDoc = new DocTermInfo(docNum);
            docs.put(docNum, curDoc);
        }

        curDoc.increaseTfi();
        curDoc.addIndex(index);
    }

    //</editor-fold>

    //<editor-fold des="Getters">

    /**Amount of docs this term appears in groups of files*/
    public int getDf() { return docs.size(); }

    public String getValue() { return value; }

    public boolean isStartsWithCapital() { return startsWithCapital; }

    public boolean isEntity(){ return this.isEntity; }

    //<editor-fold des="Documents Information Getters">

    public int getDocTfi(String docNum){ return docs.get(docNum).getTfi(); }

    public String getTermDocIndexes(String docNum){ return docs.get(docNum).getTermIndexes(); }

    //</editor-fold>

    //<editor-fold des="Documents Lists Getters">

    public List<DocTermInfo> getDocsSortedByName(){
//        ArrayList<Model.IWritable> docList = new ArrayList<>(docs.values());
//        docList.sort(Comparator.comparing(o -> ((Model.DocTermInfo) o).getDocNum()));
        ArrayList<DocTermInfo> docList = new ArrayList<>(docs.values());
        docList.sort(Comparator.comparing(DocTermInfo::getDocNum));
        return docList;
    }

    public int getDocsWhereTfiIsNum(int tfi){
        List<String> docsNumber = new LinkedList<>();
        for (DocTermInfo doc: docs.values())
            if (doc.getTfi() == tfi)
                docsNumber.add(doc.getDocNum());

        return docsNumber.size();
    }

    //</editor-fold>

    public DocTermInfo getDoc(String docNum){ return docs.get(docNum); }

    //</editor-fold>

    //<editor-fold des="Interface Functions">

    public String toFileString(){
        StringBuilder termData = new StringBuilder();
        termData.append(getValue() + "|");
        termData.append(getDf() + "|");
        termData.append(getDocListString());
        return termData.toString();
    }

    @Override
    public List<String> toFile() {
        List<String> toWrite = new ArrayList<>();
        toWrite.add(toFileString());
        return toWrite;
    }

    public void update(List<String> toUpdate, int lineNum) {

        String termPosting = toUpdate.remove(lineNum);
        String[] lineSplitToMainSections = StringUtils.split(termPosting, "|");
        StringBuilder updatedTermData = new StringBuilder();

        updatedTermData.append(getUpdatedValue(lineSplitToMainSections[VALUE_INDEX]) + "|");
        updatedTermData.append(getUpdatedDF(lineSplitToMainSections[DF_INDEX]) + "|");
        updatedTermData.append(getUpdatedDocListString(lineSplitToMainSections[DOCLIST_INDEX]));

        toUpdate.add(lineNum, updatedTermData.toString());
    }

    private String getUpdatedValue(String prevValue){
        if(isEntity || value.compareTo(prevValue.toLowerCase()) == 0)
            return  value;
        return prevValue;
    }

    private String getUpdatedDF(String prevDF){
        return (Integer.parseInt(prevDF) + getDf()) + "";
    }

    private StringBuilder getDocListString(){
        return getUpdatedDocListString("").replace(0,1, "");
    }

    private StringBuilder getUpdatedDocListString(String prevDocList){

        StringBuilder newDocList = new StringBuilder(prevDocList);

        for(DocTermInfo docInfo: getDocsSortedByName())

            if(docInfo.getDocNumPrefix().compareTo(lastFileNumPrefix) == 0)
                newDocList.append(docInfo.toString());
            else {
                lastFileNumPrefix = docInfo.getDocNumPrefix();
                newDocList.append(docPrefixDel + lastFileNumPrefix + ":" + docInfo.toString());
            }

        return newDocList;
    }


    //</editor-fold>

}
