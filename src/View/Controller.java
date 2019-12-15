package View;

import ViewModel.ViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Controller implements Observer {

    private ViewModel viewModel;
    private String loadingPath;
    private String savingPath;
    private static Stage primaryStage;
    private boolean stem;
    private double runningTime;
    private int corpusSize;



    @FXML
    public javafx.scene.control.Button btnStart;
    public javafx.scene.control.Button btnBrowseLoad;
    public javafx.scene.control.Button btnBrowseSave;
    public javafx.scene.control.Button btnReset;
    public javafx.scene.control.Button btnShowDictionary;
    public javafx.scene.control.Button btnLoadDictionary;
    public javafx.scene.control.CheckBox btnStem;
    public javafx.scene.control.TextField fieldLoadingPath;
    public javafx.scene.control.TextField fieldSavingPath;


    public void initialize(ViewModel viewModel, Stage primaryStage) {
        this.viewModel = viewModel;
        this.primaryStage = primaryStage;
        loadingPath = "";
        savingPath = "";
        stem= false;
       disableAllButtonsButBrowseAndStart();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            String[] args= (String[])arg;
            switch (args[0]) {
                case "load":
                    loadingPath= args[1];
                    fieldLoadingPath.appendText(loadingPath);
                    break;
                case "save":
                    savingPath = args[1];
                    fieldSavingPath.appendText(savingPath);
                    btnStem.setDisable(false);
                    btnStart.setDisable(false);
                    break;
                case "dictionary done":
                    enableButtons();


            }
        }
    }

    public void loadPath() throws IOException, ClassNotFoundException {
        viewModel.selectPathForLoading();
    }

    public void savePath() throws IOException, ClassNotFoundException {
        viewModel.selectPathForSaving();
    }

    public void btnStemPressed(){
        stem = btnStem.isSelected();
    }

    public void disableAllButtonsButBrowseAndStart(){
        btnReset.setDisable(true);
        fieldSavingPath.setDisable(true);
        fieldLoadingPath.setDisable(true);
        btnLoadDictionary.setDisable(true);
        btnShowDictionary.setDisable(true);
    }

    public void disableStartButton(){
        btnStart.setDisable(true);
        btnStem.setDisable(true);
    }

    public void disableBrowseButtons(){
        btnBrowseLoad.setDisable(true);
        btnBrowseSave.setDisable(true);
    }

    public void enableButtons(){
        btnStart.setDisable(false);
        btnStem.setDisable(false);
        btnLoadDictionary.setDisable(false);
        btnShowDictionary.setDisable(false);
    }


    //being called when clicked start
    public void parse (){
        if (fieldLoadingPath.getText().isEmpty() || fieldSavingPath.getText().isEmpty()) {
           Alert alert = new Alert(Alert.AlertType.ERROR, "Both paths should be selected before clicking the Start button");
            alert.show();
            return;
        }
       Alert alert = new Alert(Alert.AlertType.NONE, "Working on it...");
       alert.setTitle("Parsing and indexing takes time!");
       alert.setHeight(600);
       alert.setWidth(600);
       alert.show();
       disableAllButtonsButBrowseAndStart();
       disableStartButton();
       disableBrowseButtons();
       viewModel.parse(loadingPath, savingPath, stem);
    }

//    public void showDictionary() {
//        try {
//            List<String> dictionary = viewModel.showDictionary();
//            Stage stage = new Stage();
//            stage.setTitle("Dictionary");
//            FXMLLoader fxmlLoader = new FXMLLoader();
//            Parent root = fxmlLoader.load(getClass().getResource("dictionary.fxml").openStream());
//            Scene scene = new Scene(root, 450, 200);
//            stage.setScene(scene);
//            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
//            stage.show();
//        } catch (Exception e) {
//
//        }
    //}

}
