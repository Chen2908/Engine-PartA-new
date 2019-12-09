import java.util.*;

public class Term implements IWritable{

    //<editor-fold des="Class Fields">

    private String value;
    private HashMap<String, DocTermInfo> docs; // hashMap of all the docs that this term appears in
    private boolean startsWithCapital;
    private boolean isEntity;
    private final String del = ",";

    //</editor-fold>

    //<editor-fold des="Constructors">

    public Term(String value, boolean isEntity) {
        setValue(value);
        docs = new HashMap<>();
        this.isEntity = isEntity;
    }

    public Term(String value) {
        setValue(value);
        docs = new HashMap<>();
        this.isEntity = false;
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

    @Override
    //
    public String toString(){
        return getDf() + "";
    }

    @Override
    public List<String> toFile() {
        List<String> toWrite = new ArrayList<>();
        toWrite.add(toString());
        return toWrite;
    }

    @Override
    public List<String> update(List<String> toUpdate) {
        String df = toUpdate.remove(0);
        int updatedDf = Integer.parseInt(df) + getDf();
        List<String> toWrite = new ArrayList<>();
        toWrite.add(updatedDf + "");
        return toWrite;
    }

    //</editor-fold>

}
