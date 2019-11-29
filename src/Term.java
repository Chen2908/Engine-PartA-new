import java.util.*;

public class Term {

    //<editor-fold des="Class Fields">

    private String value;
    private HashMap<String, DocInfo> docs; // hashMap of all the docs that this term appears in
    private boolean startsWithCapital;


    //</editor-fold>

    //<editor-fold des="Constructors">

    public Term(String value) {
        setValue(value);
        docs = new HashMap<>();
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
        DocInfo curDoc = docs.get(docNum);
        if (curDoc == null) {
            // creates a new document info object
            curDoc = new DocInfo(docNum);
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

    //<editor-fold des="Documents Information Getters">

    public int getDocTfi(String docNum){ return docs.get(docNum).getTfi(); }

    public String getTermDocIndexes(String docNum){ return docs.get(docNum).getTermIndexes(); }

    //</editor-fold>

    //<editor-fold des="Documents Lists Getters">

    public List<DocInfo> getDocsSortedByName(){
        ArrayList<DocInfo> docList = new ArrayList<>(docs.values());
        docList.sort(Comparator.comparing(DocInfo::getDocNum));
        return docList;
    }

    public List<String> getDocsWhereTfiIs(int tfi){
        List<String> docsNumber = new LinkedList<>();
        for (DocInfo doc: docs.values())
            if (doc.getTfi() == tfi)
                docsNumber.add(doc.getDocNum());

        return docsNumber;
    }

    //</editor-fold>

    public DocInfo getDoc(String docNum){ return docs.get(docNum); }

    //</editor-fold>

}
