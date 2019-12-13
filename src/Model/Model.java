package Model;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class Model extends Observable {

    private Manager manager;
    private boolean stem;


    public Model() {

    }

    public void parse(String loadingPath, String savingPath, boolean stem) {
        this.stem=stem;
        setManager(loadingPath, savingPath);
        manager.callReaderAndParser();
    }


    private void setManager(String loadingPath, String savingPath) {
        this.manager = new Manager(loadingPath, savingPath, stem);
    }

    public void reset(){

    }

    public List<String> showDictionary(){
        List<String> terms= new LinkedList<>();
        File dictFile = new File(manager.getDictionaryPath());
        String term;
        try{
            BufferedReader readDict = new BufferedReader(new FileReader(dictFile));
            try {
                String line = readDict.readLine();
                while (line != null){
                    term=(line.split(";"))[0];
                    terms.add(term);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        return terms;
    }

}
