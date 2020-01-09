package View;

import ViewModel.ViewModel;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * This class represents the view layer in MVVM structure. This is the controller of the program.
 * It controls the UI
 */
public class Controller implements Observer {

    private ViewModel viewModel;
    private String loadingPath;
    private String savingPath;
    private String queryPath;
    private String queryText;
    private String saveQueryResultsPath;
    private static Stage primaryStage;
    private boolean stem;
    private boolean semantics;
    private boolean clickstream;
    private boolean loaded;
    private boolean parsed;
    private List<Pair<String, String>> queriesFromFileText;
    private HashMap<String, List<Pair<String, Double>>> resultsPerQuery;
    private List<String> queryResultsIncludingIdDocs;
    private List<Double> queryResultsIncludingIdScore;

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
    public javafx.scene.control.TextField fieldLoadingQuery;
    public javafx.scene.control.TextField fieldTypingQuery;
    public javafx.scene.control.CheckBox btnSemantics;
    public javafx.scene.control.CheckBox btnClickstream;
    public javafx.scene.control.Button btnBrowseQuery;
    public javafx.scene.control.Button btnSaveResults;
    public javafx.scene.control.Button btnShowResults;
    public javafx.scene.control.Button btnStartS;

    public Pane pane;
    public ImageView boximage;


    public void initialize(ViewModel viewModel, Stage primaryStage) {
        this.viewModel = viewModel;
        this.primaryStage = primaryStage;
        loadingPath = "";
        savingPath = "";
        queryPath = "";
        queryText = "";
        saveQueryResultsPath = "";
        queriesFromFileText = new ArrayList<>();
        stem = false;
        semantics = false;
        clickstream = false;
        loaded = false;
        parsed = false;
        disableAllButtonsButBrowseResetAndStart();
        resultsPerQuery = new HashMap<>();

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
                case "query":
                    queryPath = args[1];
                    fieldLoadingQuery.setText(queryPath);
                    readQueryFromFile();
                    btnStartS.setDisable(false);
                    break;
                case "saveQuery":
                    saveQueryResultsPath = args[1];
                    btnShowResults.setDisable(false);
                    writeResultsToFile();
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

    private void readQueryFromFile() {
        String queryNum = "";
        String filePath = queryPath;
        StringBuilder text = new StringBuilder();
        File queryFile = new File(filePath);
        try {
            Scanner sc = new Scanner(queryFile);
            while (sc.hasNext()) {
                String line = sc.nextLine();
                if (line.contains("<num>")){
                    queryNum = line.substring(14);
                }
                if (line.contains("<title>")){
                    queriesFromFileText.add(new Pair(queryNum, line.substring(8)));
                }
                text.append(line + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        queryText = text.toString();
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

    public void loadPath()  {
        viewModel.selectPathForLoading();
    }

    public void savePath(){
        viewModel.selectPathForSaving();
    }


    public void btnStemPressed()
    {
        stem = btnStem.isSelected();
    }


    public void disableAllButtonsButBrowseResetAndStart() {
        fieldSavingPath.setDisable(true);
        btnLoadDictionary.setDisable(true);
        btnShowDictionary.setDisable(true);
        fieldLoadingPath.setDisable(true);
        fieldLoadingQuery.setDisable(true);
        btnSaveResults.setDisable(true);
        btnShowResults.setDisable(true);
    }

    public void disableStartButton() {
        btnStart.setDisable(true);
        btnStem.setDisable(true);
        btnReset.setDisable(true);
        btnStartS.setDisable(true);
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
        disableAllButtonsButBrowseResetAndStart();
        disableStartButton();
        disableBrowseButtons();
        alert.showAndWait();
        this.parsed = true;
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
        if (! parsed){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Nothing to delete");
            alert.showAndWait();
            return;
        }
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

    //partB

    public void browseQuery() {
        viewModel.selectPathForQuery();
    }


    public void saveResults(){
        viewModel.saveResults();
    }

    private void writeResultsToFile(){
        try {
            File file = new File(this.saveQueryResultsPath + "/queryResults" + stem + ".txt");
            BufferedWriter bf = new BufferedWriter(new FileWriter(file));
            for (String queryNum : resultsPerQuery.keySet()){
                for (Pair<String, Double> pair: resultsPerQuery.get(queryNum)){
                    bf.write(queryNum + " 0 " +pair.getKey() + " 1" +  " 0.0" + " mt" + "\n");
                }
            }
            bf.close();
        }catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Cannot write results to file");
            alert.showAndWait();
        }
    }

    public void showResults(){
        //show results
        TableView tableView = new TableView<>();

        TableColumn<String, MapViewDouble> firstColumn = new TableColumn<>("Query : DocNO");
        firstColumn.setCellValueFactory(new PropertyValueFactory<>("query : DocNo"));
        firstColumn.setPrefWidth(300);
        TableColumn<Double, MapViewDouble> secondColumn = new TableColumn<>("Score");
        secondColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        secondColumn.setPrefWidth(300);
        tableView.getColumns().add(firstColumn);
        tableView.getColumns().add(secondColumn);
        tableView.getSelectionModel().setCellSelectionEnabled(true);

        for (int i = 0; i < this.queryResultsIncludingIdDocs.size(); i++) {
            double score = this.queryResultsIncludingIdScore.get(i);
            String docNo = this.queryResultsIncludingIdDocs.get(i);
            MapViewDouble mv = new MapViewDouble(docNo, score);
            tableView.getItems().add(mv);
        }
        StackPane sPane = new StackPane(tableView);
        Scene sceneQuery = new Scene(sPane, 600, 800);
        Stage stageQuery = new Stage();
        stageQuery.setTitle("Query Results");
        stageQuery.setScene(sceneQuery);
        stageQuery.show();
    }



    public void search() {
        if (!this.loaded && !this.parsed) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No dictionary loaded, cannot run query");
            alert.show();
            return;
        }

        List<Pair<String, Double>> queryResults;
        List<String> queryResultsIncludingIdDocs = new ArrayList<>();
        List<Double> queryResultsIncludingIdScore = new ArrayList<>();
        HashMap<String, List<Pair<String, Double>>> resultsPerQuery = new HashMap<>();

        if (!fieldTypingQuery.getText().isEmpty() && fieldLoadingQuery.getText() != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You can choose either a query file or a text query, not both");
            alert.show();
            return;
        }

        else if (!fieldTypingQuery.getText().isEmpty()) {
            queryText = fieldTypingQuery.getText();
            queryResults = viewModel.search(queryText, stem, semantics);
            for (Pair<String, Double> pair : queryResults) {
                queryResultsIncludingIdDocs.add("query 000: " + pair.getKey());
                queryResultsIncludingIdScore.add(pair.getValue());
            }
            resultsPerQuery.put("000", queryResults);

        } else if (fieldLoadingQuery.getText() != null) {
            //call search with each query from file
            for (Pair<String, String> queryPair : queriesFromFileText) {
                queryResults = viewModel.search(queryPair.getValue(), stem, semantics);
                for (Pair<String, Double> pair : queryResults) {
                    queryResultsIncludingIdDocs.add("query " + queryPair.getKey() + ": " + pair.getKey());
                    queryResultsIncludingIdScore.add(pair.getValue());
                }
                resultsPerQuery.put(queryPair.getKey(), queryResults);
            }
        }
        this.resultsPerQuery = resultsPerQuery;
        this.queryResultsIncludingIdDocs = queryResultsIncludingIdDocs;
        this.queryResultsIncludingIdScore = queryResultsIncludingIdScore;

        //the user should now be able to view and save the results
        btnShowResults.setDisable(false);
        btnSaveResults.setDisable(false);

        //let the user know he can view the results
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Query run is done");
        alert.setContentText("Your query results are ready. To view them click on 'Show Results'");
        alert.showAndWait();
    }


    public void btnSemanticsPressed(){
        this.semantics = btnSemantics.isSelected();
    }

    public void btnClickstreamPressed(){
        this.clickstream = btnClickstream.isSelected();
    }



    //for table view
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

    //for table view
    public static class MapViewDouble{
        private SimpleStringProperty docNo;
        private SimpleDoubleProperty score;

        public MapViewDouble(String docNo, double score){
            this.docNo = new SimpleStringProperty(docNo);
            this.score = new SimpleDoubleProperty(score);
        }

        public void setScore(int count) {
            this.score.set(count);
        }

        public void setTerm(String term) {
            this.docNo.set(term);
        }

        public String getDocNo() {
            return docNo.get();
        }

        public double getScore() {
            return score.get();
        }

        public SimpleStringProperty docNoProperty() {
            return docNo;
        }

        public SimpleDoubleProperty scoreProperty() {
            return score;
        }
    }

}
