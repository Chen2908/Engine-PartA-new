package ViewModel;

import Model.Model;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class ViewModel extends Observable implements Observer {

    private Model model;

    public ViewModel(Model model) {
        this.model = model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o==model){
            //functions
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


}

