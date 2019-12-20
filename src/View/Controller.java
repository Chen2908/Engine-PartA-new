package View;

import ViewModel.ViewModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * This class represents the view layer in MVVM structure. This is the controller of the program.
 * It controls the UI
 */
public class Controller implements Observer {

    private ViewModel viewModel;
    private String loadingPath;
    private String savingPath;
    private static Stage primaryStage;
    private boolean stem;
    private boolean loaded;


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
    public Pane pane;
    public ImageView boximage;


    public void initialize(ViewModel viewModel, Stage primaryStage) {
        this.viewModel = viewModel;
        this.primaryStage = primaryStage;
        loadingPath = "";
        savingPath = "";
        stem = false;
        disableAllButtonsButBrowseAndStart();

        try {
            BufferedImage bufferedImage;
            bufferedImage = ImageIO.read(new File("./Capture1.JPG"));
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            this.boximage.setImage(image);
        }catch (Exception e) {
        }

    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            String[] args = (String[]) arg;
            switch (args[0]) {
                case "load":
                    loadingPath = args[1];
                    fieldLoadingPath.setText(loadingPath);
                    break;
                case "save":
                    savingPath = args[1];
                    fieldSavingPath.setText(savingPath);
                    btnStem.setDisable(false);
                    btnStart.setDisable(false);
                    btnLoadDictionary.setDisable(false);
                    break;
                case "dictionary done":
                    enableButtons();
                    if (args.length > 3) {
                        double time = Double.parseDouble(args[1]);
                        int corpusSize = Integer.parseInt(args[2]);
                        int vocabularySize = Integer.parseInt(args[3]);
                        showInfoOnIndex(time, corpusSize, vocabularySize);
                    }
                    break;
                case "dictionary loaded":
                    btnShowDictionary.setDisable(false);
                    btnReset.setDisable(false);
                    this.loaded = true;
//                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Your dictionary was loaded");
//                    alert.setHeaderText("Great!");
//                    alert.showAndWait();
//                    alert.close();
                    break;
                case "error in loading":
                    Alert alert2 = new Alert(Alert.AlertType.ERROR);
                    alert2.setContentText("No dictionary to load in the given path. Check your path and try again.");
                    alert2.showAndWait();
                    break;
            }
        }
    }

    private void showInfoOnIndex(double time, int corpusSize, int vocabularySize) {
        String info = "Corpus size: " + corpusSize + " documents"+
                '\n' + "Running time: " + time + " seconds"+
                '\n' + "Vocabulary size: " + vocabularySize + " terms";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(info);
        alert.setHeaderText("Corpus global information");
        alert.showAndWait();
    }

    public void loadPath() throws IOException, ClassNotFoundException {
        viewModel.selectPathForLoading();
    }

    public void savePath() throws IOException, ClassNotFoundException {
        viewModel.selectPathForSaving();
    }

    public void btnStemPressed()
    {
        stem = btnStem.isSelected();
    }


    public void disableAllButtonsButBrowseAndStart() {
        btnReset.setDisable(true);
        fieldSavingPath.setDisable(true);
        btnLoadDictionary.setDisable(true);
        btnShowDictionary.setDisable(true);
        fieldLoadingPath.setDisable(true);
    }

    public void disableStartButton() {
        btnStart.setDisable(true);
        btnStem.setDisable(true);
        btnReset.setDisable(true);
    }

    public void disableBrowseButtons() {
        btnBrowseLoad.setDisable(true);
        btnBrowseSave.setDisable(true);
    }

    public void enableButtons() {
        btnStart.setDisable(false);
        btnStem.setDisable(false);
        btnLoadDictionary.setDisable(false);
        btnShowDictionary.setDisable(false);
        btnReset.setDisable(false);
    }


    //being called when clicked start
    public void parse() {
        loaded=false;
        viewModel.resetObjects();
        if (fieldLoadingPath.getText().isEmpty() || fieldSavingPath.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Both paths should be selected before clicking the Start button");
            alert.show();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please Press OK to Start the Process");
        alert.setHeaderText("Parsing and indexing takes time!");
        alert.setTitle("Wait");
        alert.setHeight(200);
        alert.setWidth(250);
        disableAllButtonsButBrowseAndStart();
        disableStartButton();
        disableBrowseButtons();
        alert.showAndWait();
        viewModel.parse(loadingPath, savingPath, stem);
    }

    public void loadClicked (){
        btnShowDictionary.setDisable(false);
    }

    public void loadDictionary() {
        if(stem)
             viewModel.loadDictionary(savingPath+ "\\With Stemming\\Index");
        else
            viewModel.loadDictionary(savingPath+ "\\Without Stemming\\Index");
    }

    public void showDictionary() {
        if (!loaded)
            loadDictionary();
        if (loaded) {
            ArrayList<String> termToShow = viewModel.getTermsSorted();
            ArrayList<Integer> counts = viewModel.getCountOfTerms();

            TableView tb = new TableView<>();

            TableColumn<String, MapView> firstCol = new TableColumn<>("Term");
            firstCol.setCellValueFactory(new PropertyValueFactory<>("term"));
            firstCol.setPrefWidth(300);
            TableColumn<Integer, MapView> secondCol = new TableColumn<>("Count");
            secondCol.setCellValueFactory(new PropertyValueFactory<>("count"));
            secondCol.setEditable(true);
            secondCol.setPrefWidth(300);

            tb.getColumns().add(firstCol);
            tb.getColumns().add(secondCol);
            tb.setEditable(true);
            tb.getSelectionModel().setCellSelectionEnabled(true);

            for (int i = 0; i < termToShow.size(); i++) {
                Integer count = counts.get(i);
                String term = termToShow.get(i);
                MapView mv = new MapView(term, count);
                tb.getItems().add(mv);
            }

            StackPane sPane = new StackPane(tb);
            Scene scene = new Scene(sPane, 600, 800);
            Stage stage = new Stage();
            stage.setTitle("Dictionary");
            stage.setScene(scene);
            stage.show();
        }

    }

    public void resetAll() {
       deleteDirs(savingPath+ "\\With Stemming");
       deleteDirs(savingPath+ "\\Without Stemming");
        //reset memory
        viewModel.resetObjects();
        btnShowDictionary.setDisable(true);
        btnLoadDictionary.setDisable(true);
    }

     private void deleteDirs(String path){
         File dir= new File(path);
             if (dir.exists()) {
                 try {
                     FileUtils.cleanDirectory(dir);
                     FileUtils.deleteDirectory(dir);
                 }catch (Exception e){
                     Alert alert = new Alert(Alert.AlertType.ERROR, "Cannot delete directory");
                     alert.showAndWait();
                 }
             }
    }

    public static class MapView{
        private SimpleStringProperty term;
        private SimpleIntegerProperty count;

        public MapView(String term, Integer count){
            this.term = new SimpleStringProperty(term);
            this.count = new SimpleIntegerProperty(count);
        }

        public void setCount(int count) {
            this.count.set(count);
        }

        public void setTerm(String term) {
            this.term.set(term);
        }

        public String getTerm() {
            return term.get();
        }

        public int getCount() {
            return count.get();
        }

        public SimpleStringProperty termProperty() {
            return term;
        }

        public SimpleIntegerProperty countProperty() {
            return count;
        }

    }

}
