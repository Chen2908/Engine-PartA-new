public class Document {

    private String docName;
    private String docNo;
    private String date;
    private String header;
    private String text;

    public Document(String docName, String docNo, String date, String header, String text) {
        this.docName = docName;
        this.docNo = docNo;
        this.date = date;
        this.header = header;
        this.text = text;
    }

    public String getDocName() {
        return docName;
    }

    public String getDocNo() {
        return docNo;
    }

    public String getDate() {
        return date;
    }

    public String getHeader() {
        return header;
    }

    public String getText() {
        return text;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setText(String text) {
        this.text = text;
    }
}
