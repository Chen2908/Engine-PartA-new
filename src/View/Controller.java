package View;

import ViewModel.ViewModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Controller implements Observer {

    private ViewModel viewModel;
    private String loadingPath;
    private String savingPath;
    private static Stage primaryStage;
    private boolean stem;
    private boolean loaded;
    private String show;



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
    public javafx.scene.control.TextArea txtArea_dictionary;
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
                    fieldLoadingPath.appendText(loadingPath);
                    break;
                case "save":
                    savingPath = args[1];
                    fieldSavingPath.appendText(savingPath);
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
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Your dictionary was loaded");
                    alert.setHeaderText("Great!");
                    alert.showAndWait();
                    alert.close();
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
        if (fieldLoadingPath.getText().isEmpty() || fieldSavingPath.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Both paths should be selected before clicking the Start button");
            alert.show();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Working on it...");
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
//            StringBuilder dic = new StringBuilder();
//            HashMap<String, Integer> dictionary = viewModel.showDictionaryToShow();
//            ArrayList<String> sortedKeys = new ArrayList<>(dictionary.keySet());
//            Collections.sort(sortedKeys);
//            Iterator it = sortedKeys.iterator();
//            while (it.hasNext()){
//                String term= (String)it.next();
//                dic.append(term+ " - " + dictionary.get(term) + '\n');
//            }
           if (!loaded)
               loadDictionary();
            ArrayList<String> termToShow = viewModel.getTermsSorted();
            ArrayList<Integer> counts = viewModel.getCountOfTerms();

            TableView tb = new TableView<>();
            TableColumn<String, MapView> firstCol = new TableColumn<>("Term");
            firstCol.setCellValueFactory(new PropertyValueFactory<>("term"));

            TableColumn<String, MapView> secondCol = new TableColumn<>("Count");
            firstCol.setCellValueFactory(new PropertyValueFactory<>("count"));

            tb.getColumns().add(firstCol);
            tb.getColumns().add(secondCol);

           for (int i= 0; i<termToShow.size(); i++){
               int count = counts.get(i);
               String term = termToShow.get(i);
               MapView mv = new MapView(term,count);
               tb.getItems().add(mv);
           }
            VBox box =  new VBox();
            box.getChildren().add(tb);
            Scene scene = new Scene(box);
            Stage stage = new Stage();
            stage.setTitle("Dictionary");
            stage.setScene(scene);
            stage.show();

//            FXMLLoader fxmlLoader = new FXMLLoader();
//            Parent root = fxmlLoader.load(getClass().getResource("dictionary.fxml").openStream());
//            Scene scene = new Scene(root, 610, 650);
//
//            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
//            txtArea_dictionary = (javafx.scene.control.TextArea) scene.lookup("#txtArea_dictionary");
//            txtArea_dictionary.setText(show);
//
    }

    public void resetAll() {
        File dir;
        if (stem)
             dir= new File(savingPath+ "\\With Stemming");
        else
            dir = new File(savingPath+ "\\Without Stemming");

        if (dir.exists()) {
            try {
                boolean delete= deleteDirectory(dir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Alert alert= new Alert(Alert.AlertType.ERROR, "path for deletion does not exist");
            alert.showAndWait();
        }

        //reset memory
        viewModel.resetObjects();
        btnShowDictionary.setDisable(true);
        btnLoadDictionary.setDisable(true);
    }

    private boolean deleteDirectory(File dir){
        if (dir.exists()) {
                if(dir.isDirectory()){
                    String[] childFiles = dir.list();
                    if(childFiles == null) {
                        //Directory is empty. Proceed for deletion
                        dir.delete();
                    }
                    else {
                        //Directory has other files.
                        //Need to delete them first
                        for (String childFilePath :  childFiles) {
                            File childFile = new File(childFilePath);
                            //recursive delete the files
                            deleteDirectory(childFile);
                        }
                    }
                    return true;
                }
                else if (dir.isFile()){
                    dir.delete();
                }
            }
        return false;
    }



    public static class MapView{
        private SimpleStringProperty term;
        private SimpleIntegerProperty count;

        public MapView(String term, int count){
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

    }

}
