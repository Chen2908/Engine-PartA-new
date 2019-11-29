import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents a term in the corpus
 */

public class Term {

    private String value;
    private int df;  //amount of docs it appeared in groups of files
    private HashMap<String, DocInfo> docs; // hashMap of all the docs that this term appears in
    private boolean startsWithCapital;

    public Term(String value) {
        setValue(value);
        setDf(0);
        docs = new HashMap<>();
    }

    public Term(String value, int df) {
        setValue(value);
        setDf(df);
        docs = new HashMap<>();
    }

    public boolean isStartsWithCapital() { return startsWithCapital; }

    private void setStartsWithCapital(boolean startsWithCapital) {
        this.startsWithCapital = startsWithCapital;
    }

    public int getDf() { return df; }

    public void setDf(int df) { this.df = df; }

    public String getValue() { return value; }

    public void setValue(String value) {
        this.value = value;
        setStartsWithCapital(Character.isUpperCase(value.charAt(0)));
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

    public int getDocTfi(String docNum){ return docs.get(docNum).getTfi(); }

    public String getTermDocIndexes(String docNum){ return docs.get(docNum).getTermIndexes(); }

    public List<DocInfo> getDocsSortedByName(){
        ArrayList<DocInfo> docList = new ArrayList<>(docs.values());
        docList.sort(Comparator.comparing(DocInfo::getDocNum));
        return docList;
    }

    public DocInfo getDoc(String docNum){ return docs.get(docNum); }
}
