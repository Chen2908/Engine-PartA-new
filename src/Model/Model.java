package Model;

import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

/**
 * This class represents the connections between the logical part of the search engine and the view
 * It keep a manager object and uses it to start the parsing and indexing process when start is clicked in the view
 */
public class Model extends Observable {

    private Manager manager;
    private boolean stem;
    private HashMap<String, int[]> terms;
    private HashMap<String, Integer> termstoShow;
    private boolean loaded;
    private ArrayList<String> termsSorted;
    private ArrayList<Integer> countOfTerms;
    private String dicPath;

    public Model() {

    }

    //initiates the parsing and indexing process
    public void parse(String loadingPath, String savingPath, boolean stem) {
        this.stem = stem;
        setManagerParsing(loadingPath, savingPath);
        manager.callReaderAndParser();
        double time = manager.getProcessTime();
        int corpusSize = manager.getCorpusSize();
        int vocabularySize = manager.getVocabularySize();
       // terms= manager.getDictionary();
        termstoShow = manager.getDictionaryToShow();
        this.dicPath = manager.getDictionaryPath();
        String[] notify = {"dictionary done", Double.toString(time), Integer.toString(corpusSize), Integer.toString(vocabularySize)};
        setChanged();
        notifyObservers(notify);
    }


    private void setManagerParsing(String loadingPath, String savingPath) {
        this.manager = new Manager(loadingPath, savingPath, stem);
    }

    private void setManagerSearching(String postingPath, boolean stemming, boolean semantics) {
        this.manager = new Manager(postingPath, stemming, semantics);
    }


    public void loadDictionary(String path) {
        HashMap<String, int[]> terms = new HashMap<>();
        HashMap<String, Integer> termsToShow = new HashMap<>();
        ArrayList<String> termsList = new ArrayList<>();
        ArrayList<Integer> count= new ArrayList<>();
        String pathFile=path+ "\\dictionary.txt";
        this.dicPath = pathFile;
        File dictFile = new File(pathFile);
        BufferedReader readDict;
        try {
            readDict= new BufferedReader(new FileReader(dictFile));
            try {
                String line = readDict.readLine();
                while (line != null) {
                    if (line.isEmpty()){
                        line = readDict.readLine();
                        continue;
                    }
                    String[] inLine = line.split(";");
                    int tf = Integer.parseInt(inLine[1].trim());
                    int[] val = new int[]{tf, Integer.parseInt(inLine[2].trim())};
                    String key = inLine[0].trim();
                    termsList.add(key);
                    count.add(tf);
                    terms.put(key, val);
                    termsToShow.put(key, tf);
                    line = readDict.readLine();
                }
                readDict.close();
                loaded=true;
            } catch (IOException e) {

            }

        } catch (FileNotFoundException e) {
            String [] error = {"error in loading"};
            setChanged();
            notifyObservers(error);
            return;
        }
        this.termsSorted = termsList;
        this.countOfTerms = count;
        this.termstoShow = termsToShow;
        this.terms = terms;
        String[] notify = {"dictionary loaded"};
        setChanged();
        notifyObservers(notify);
    }

    public ArrayList<String> getTermsSorted() {
        return termsSorted;
    }

    public ArrayList<Integer> getCountOfTerms() {
        return countOfTerms;
    }

    public HashMap<String, Integer> getDictionaryToShow() {
        return termstoShow;
    }

    public HashMap<String, int[]> getDictionary() {
        return terms;
    }

    //set all living objects to null
    public void resetObjects() {
        if (manager != null) {
            manager.resetObjects();
            this. manager = null;
            this.termstoShow= null;
            this.terms=null;
            this.termsSorted=null;
            this.countOfTerms=null;
        }
    }


    public List<Pair<String, Double>> search(String queryText, boolean stemming, boolean semantics) {
        setManagerSearching(this.dicPath, stemming, semantics);
        return manager.search(queryText);
    }
}
