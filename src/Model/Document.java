package Model;

/**
 * This object represent a document
 * and holds all the relevant values
 */
public class Document {

    //<editor-fold "Fields">

    private String docNum;
    private String date;
    private String text;
    private String title;

    //</editor-fold>

    /**
     * Constructor
     */
    public Document(){}

    //<editor-fold "Getters">

    /**
     * This methods returns the document number
     * @return document number
     */
    public String getDocNum() {
        return docNum;
    }

    /**
     * This methods returns the document date
     * @return document date
     */
    public String getDate() {
        return date;
    }

    /**
     * This methods returns the document text
     * @return document text
     */
    public String getText() {
        return text;
    }

    /**
     * This methods returns the document title
     * @return document title
     */
    public String getTitle(){
        return this.title;
    }

    //</editor-fold>

    //<editor-fold "Setters">

    /**
     * This method updates the document number
     * @param docNum - new document number
     */
    public void setDocNum(String docNum) {
        this.docNum = docNum;
    }

    /**
     * This method updates the document date
     * @param date - new document date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * This method updates the document title
     * @param title - new document title
     */
    public void setTitle(String title){
        this.title = title;
    }

    /**
     * This method updates the document text
     * @param text - new document text
     */
    public void setText(String text) {
        this.text = text;
    }

    //</editor-fold>
}
