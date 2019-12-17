package ViewModel;

import Model.Model;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.*;
import java.util.*;

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
    public void selectPathForLoading() throws IOException, ClassNotFoundException {
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
    public void selectPathForSaving() throws IOException, ClassNotFoundException {
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
}

