public class Document {

    private String docNo;
    private String date;
    private String text;
    private String title;
    private int max_tf;
    private int uniqueAmount;

    public Document(){}

    public String getDocNo() {
        return docNo;
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public String getTitle(){
        return this.title;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setMax_tf(int max_tf) {
        this.max_tf = max_tf;
    }

    public void setUniqueAmount(int uniqueAmount) {
        this.uniqueAmount = uniqueAmount;
    }

    public int getMax_tf() {
        return max_tf;
    }

    public int getUniqueAmount() {
        return uniqueAmount;
    }
}
