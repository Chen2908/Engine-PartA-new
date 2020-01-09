package ViewModel;

import Model.Model;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

/**
 * ViewModel class is the layer between the view and the model. It connects them.
 */
public class ViewModel extends Observable implements Observer {

    private Model model;

    public ViewModel(Model model) {
        this.model = model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o==model){
            setChanged();
            notifyObservers(arg);
        }
    }


    //browse path from where to load stopwords and data set
    public void selectPathForLoading()  {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Data set and stopwords path");
        Window primaryStage = null;
        File file = dc.showDialog(primaryStage);
        if (file !=null){
           String path = file.getPath();
           setChanged();
           String[] pathToUpdate = {"load", path};
           notifyObservers(pathToUpdate);
        }
    }


    //browse path where to save the posting files and dictionary
    public void selectPathForSaving() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose where to save dictionary and posting files");
        Window primaryStage = null;
        File file = dc.showDialog(primaryStage);
        if (file!=null){
            String path = file.getPath();
            setChanged();
            String[] pathToUpdate = {"save", path};
            notifyObservers(pathToUpdate);
        }
    }

    public void parse(String loadingPath, String savingPath, boolean stem){
        model.parse(loadingPath, savingPath, stem);
    }

    public void loadDictionary(String path){
        model.loadDictionary(path);
    }


    public HashMap<String, int[]> showDictionary(){
        return model.getDictionary();
    }

    public HashMap<String, Integer> showDictionaryToShow(){
        return model.getDictionaryToShow();
    }


    public void resetObjects() {
        model.resetObjects();
    }

    public ArrayList<String> getTermsSorted() {
        return model.getTermsSorted();
    }

    public ArrayList<Integer> getCountOfTerms() {
        return model.getCountOfTerms();
    }


    public void selectPathForQuery() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose your query path");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text files", "*.txt");
        fc.getExtensionFilters().add(extFilter);
        Window primaryStage = null;
        File file = fc.showOpenDialog(primaryStage);
        if (file!=null){
            String path = file.getPath();
            setChanged();
            String[] pathToUpdate = {"query", path};
            notifyObservers(pathToUpdate);
        }
    }

    public ArrayList<Pair<String, Double>> search(String queryText, boolean stemming, boolean semantics) {
        return model.search(queryText, stemming, semantics);
    }

    public void saveResults() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose where to save your query results");
        Window primaryStage = null;
        File queryFile = dc.showDialog(primaryStage);
        if (queryFile != null){
            String queryPath = queryFile.getPath();
            setChanged();
            String[] pathToUpdate = {"saveQuery", queryPath};
            notifyObservers(pathToUpdate);
        }
    }
}

