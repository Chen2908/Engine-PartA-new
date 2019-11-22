import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class ReadFile {

    private List<File> files;

    private BufferedReader curFileToRead;
    private Document nextDoc;
    private String line;

    private static final String[] docTagStr = {"<DOC>","</DOC>"};
    private static final String[] docNumStr = {"<DOCNO>"};
    private static final String[] docDateStartStr = {"<DATE1>","<DATE>"};
    private static final String[] docDateEndStr = {"</DATE"};
    private static final String[] docTitleStartStr = {"<TI>","<HEADLINE>"};
    private static final String[] docTitleEndStr = {"</TI>","</HEADLINE>"};
    private static final String[] docTextStr = {"<TEXT>","</TEXT>"};
    private static final String[] docPerTagStr = {"<P>","</P>"};

    public ReadFile(String dirPath) throws FileNotFoundException {

        this.files = new LinkedList<>();
        getAllFiles(new File(dirPath));
        this.curFileToRead = new BufferedReader(new FileReader(this.files.remove(0)));
    }

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

    public Document getNextFile() {
        saveNextFile();
        return this.nextDoc;
    }

    private void saveNextFile() {

        this.nextDoc = new Document();
        StringBuilder text = new StringBuilder();

        boolean isText = false;
        boolean foundDate = false;
        boolean foundTitle = false;
        try {

            if (!findFirstLine()){
                this.nextDoc = null;
                return;
            }

            findLineWith(docNumStr);
            removeTags();
            nextDoc.setDocNo(insetContentToline());

            while(!line.contains(docTagStr[1])) {
                if (!foundDate && isLineContains(line, docDateStartStr)){
                    foundDate = true;
                    getAllDataTo(docDateEndStr);
                    removeTags();
                    nextDoc.setDate(insetContentToline());
                } else if (!foundTitle && isLineContains(line, docTitleStartStr)){
                    foundTitle = true;
                    getAllDataTo(docTitleEndStr);
                    removeTags();
                    insetContentToline();
                    nextDoc.setTitle(line);
                }else if (line.contains(docTextStr[1]))
                    isText = false;
                else if (isText && !isLineContains(line, docPerTagStr)) {
                    text.append(line);
                }
                else if (line.contains(docTextStr[0]))
                    isText = true;
                line = this.curFileToRead.readLine();
            }
            this.nextDoc.setText(text.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String insetContentToline(){
        String newLine = "";
        boolean[] gotIn = {false,false};
        for (int i=0; i < line.length() && (!gotIn[0] || !gotIn[1]) ; i++){
            if (line.charAt(i) != ' ' && !gotIn[0]) {
                newLine = line.substring(i);
                gotIn[0] = true;
            }
            if (line.charAt(line.length()-i-1) != ' ' && !gotIn[1]) {
                newLine = line.substring(0, line.length() - i);
                gotIn[1] = true;
            }
        }
        line = newLine;
        return newLine;
    }

    private boolean findFirstLine() throws IOException {
        while (true) {
            if ((line = this.curFileToRead.readLine()) == null) {
                if (this.files.size() == 0)
                    return false;

                this.curFileToRead = new BufferedReader(new FileReader(this.files.remove(0)));
                line = this.curFileToRead.readLine();
            }
            if (line.contains(docTagStr[0]))
                return true;
        }
    }

    private void findLineWith(String[] strings) throws IOException {
        while(!isLineContains(line, strings))
            line = this.curFileToRead.readLine();
    }

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

    private void getAllDataTo(String[] end) throws IOException {
        StringBuilder data = new StringBuilder(line);
        String cur_line = line;
        while (!isLineContains(cur_line, end))
            data.append(cur_line = this.curFileToRead.readLine());
        line = data.toString();
    }


    private boolean isLineContains(String line, String[] strings){
        for (String s: strings)
            if (line.contains(s))
                return true;
        return false;
    }

}
