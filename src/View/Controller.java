package View;

import ViewModel.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class Controller implements Observer {

    private ViewModel viewModel;
    private String loadingPath;
    private String savingPath;
    private static Stage primaryStage;
    boolean stem;

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
       disableAllButtonsButBrowseAndStart();
       disableStartButton();
       disableBrowseButtons();
       viewModel.parse(loadingPath, savingPath, stem);
       enableButtons();
    }

}
