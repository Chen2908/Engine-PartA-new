package Model;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ReadFile {

    //<editor-fold des="Class Fields">

    private List<File> files;

    private BufferedReader fileToRead;
    private Document nextDoc;
    private String line;

    //<editor-fold des="Documents Tags">

    private static final String[] docTagStr = {"<DOC>","</DOC>"};
    private static final String[] docNumStr = {"<DOCNO>"};
    private static final String[] docDateStartStr = {"<DATE1>","<DATE>"};
    private static final String[] docDateEndStr = {"</DATE"};
    private static final String[] docTitleStartStr = {"<TI>","<HEADLINE>"};
    private static final String[] docTitleEndStr = {"</TI>","</HEADLINE>"};
    private static final String[] docTextStr = {"<TEXT>","</TEXT>"};
    private static final String[] docPerTagStr = {"<P>","</P>"};

    //</editor-fold>

    //</editor-fold>

    //<editor-fold des="Constructor">

    /**
     * Constructor
     * @param dirPath - the path to the Directory of all the files
     */
    public ReadFile(String dirPath) {
        this.files = new LinkedList<>();
        this.line = null;
        getAllFiles(new File(dirPath));
        this.fileToRead = null;
        this.nextDoc = new Document();
    }

    //</editor-fold>

    //<editor-fold des="Getters">

    /**
     * This gets all the file in the given directory and all the sub directory
     * @param file - directory
     */
    private void getAllFiles(File file){
        if (file.isDirectory()){
            File[] dirFiles = file.listFiles();
            for (File curFileToRead : dirFiles)
                getAllFiles(curFileToRead);
        }
        else if (file.isFile()){
            files.add(file);
        }
    }

    /**
     * This method makes a list of the given number of documents
     * @param numOfDocs - size of the return list
     * @return list of documents objects
     */
    public List<Document> getNextDocs(int numOfDocs){
        if (nextDoc == null)
            return null;
        List<Document> docs = new ArrayList<>();
        for (int i = 0; i < numOfDocs && getNextDoc() != null ; i++)
            docs.add(nextDoc);

        return docs;
    }

    /**
     * This method gets the next document
     * @return the next document object, null if there is no more documents
     */
    public Document getNextDoc() {
        saveNextDoc();
        return this.nextDoc;
    }

    //</editor-fold>

    //<editor-fold des="Aid Class Methods">

    /**
     * This method creates the next document object and saves it in nextDoc variable
     */
    private void saveNextDoc() {

        StringBuilder text = new StringBuilder();

        boolean isText = false;
        boolean foundDate = false;
        boolean foundTitle = false;
        try {

            if (!findFirstLine()){
                this.nextDoc = null;
                return;
            }

            nextDoc = new Document();
            findLineWith(docNumStr);
            removeTags();
            nextDoc.setDocNum(insertContentToLine());

            while(!line.contains(docTagStr[1])) {
                if (!foundDate && isLineContains(line, docDateStartStr)){
                    foundDate = true;
                    getAllDataTo(docDateEndStr);
                    removeTags();
                    nextDoc.setDate(insertContentToLine());
                } else if (!foundTitle && isLineContains(line, docTitleStartStr)){
                    foundTitle = true;
                    getAllDataTo(docTitleEndStr);
                    removeTags();
                    insertContentToLine();
                    nextDoc.setTitle(line);
                }else if (line.contains(docTextStr[1]))
                    isText = false;
                else if (isText && !isLineContains(line, docPerTagStr))
                    text.append(line + "\n");
                else if (line.contains(docTextStr[0]))
                    isText = true;
                line = this.fileToRead.readLine();
            }
            this.nextDoc.setText(text.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method removes spaces from the line field
     * @return new line with no spaces at the start and end
     */
    private String insertContentToLine(){
        boolean[] gotIn = {false,false};
        int length = line.length();
        for (int i=0; i < length && (!gotIn[0] || !gotIn[1]) ; i++){
            if (!gotIn[0] && line.charAt(i) != ' ') {
                line = line.substring(i);
                gotIn[0] = true;
            }
            if (!gotIn[1] && line.charAt(line.length()-i-1) != ' ') {
                line = line.substring(0, line.length() - i);
                gotIn[1] = true;
            }
        }
        return line;
    }

    /**
     * This method finds the first line of the document
     * @return if a first line was found
     * @throws IOException
     */
    private boolean findFirstLine() throws IOException {
        while (true) {
            if (this.fileToRead == null || (line = this.fileToRead.readLine()) == null) {
                if (this.files.size() == 0)
                    return false;

                this.fileToRead = new BufferedReader(new FileReader(this.files.remove(0)));
                line = this.fileToRead.readLine();
            }
            if (line.contains(docTagStr[0]))
                return true;
        }
    }

    /**
     * This method ind a line with a given strings
     * @param strings - substring to find
     * @throws IOException
     */
    private void findLineWith(String[] strings) throws IOException {
        while(!isLineContains(line, strings))
            line = this.fileToRead.readLine();
    }

    /**
     * This method removes tags form the line field
     */
    private void removeTags(){
        int start = 0;
        int end = line.length();
        int j = end;
        for (int i = 0 ; i < j ; i++){
            j--;
            if (line.charAt(i) == '>')
                start = i+1;
            if (line.charAt(j) == '<')
                end = j;
        }
        line = line.substring(start, end);
    }

    /**
     * This method adds to the data StringBuilder all the lines until one of the line contains one of the given strings
     * @param end - sub strings options of the given end line
     * @throws IOException
     */
    private void getAllDataTo(String[] end) throws IOException {
        StringBuilder data = new StringBuilder(line);
        String cur_line = line;
        while (!isLineContains(cur_line, end))
            data.append(cur_line = (this.fileToRead.readLine() + "\n"));
        line = data.toString();
    }

    /**
     * This method checks if the line contains one of the given strings
     * @param line - String
     * @param strings - subString to search in the line
     * @return true - if the line contains on or more of the given strings
     */
    private boolean isLineContains(String line, String[] strings){
        for (String s: strings)
            if (line.contains(s))
                return true;
        return false;
    }

    //</editor-fold>
}
