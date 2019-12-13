package View;
import ViewModel.ViewModel;
import Model.Model;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.*;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();
        ViewModel viewModel = new ViewModel(model);
        model.addObserver(viewModel);
        //--------------
        primaryStage.setTitle("Zoogle, your one and only search engine");
        FXMLLoader fxmlLoader= new FXMLLoader(getClass().getResource("View/scene.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene1 = new Scene(root, 680, 600);
        //scene1.getStylesheets().add(getClass().getResource("ViewStyle.css").toExternalForm());
        primaryStage.setScene(scene1);
        primaryStage.show();
        primaryStage.setMinHeight(550.0);
        primaryStage.setMinWidth(500.0);

        //--------------
        Controller view = fxmlLoader.getController();
        view.initialize(viewModel, primaryStage);
        viewModel.addObserver(view);
        //--------------
        SetStageCloseEvent(primaryStage, model);
    }

    private void SetStageCloseEvent(Stage primaryStage, Model model) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    // user chose OK, close program
                    primaryStage.close();
                } else {
                    // user chose CANCEL or closed the dialog
                    windowEvent.consume();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}



//public class View.Main {
//    @SuppressWarnings("Duplicates")
//   public static void main(String[] args) {
//        int counter=0;
//        File file = new File ("C:\\Users\\cheng\\Documents\\University\\3rd\\Aihzur\\parser.txt");
//       //try {
//          // FileWriter fr = new FileWriter(file);
//            double start = System.nanoTime() / Math.pow(10, 6);
//            Model.ReadFile reader = new Model.ReadFile("C:\\Users\\cheng\\Documents\\University\\3rd\\Aihzur\\corpus\\corpus");
//            Model.Document doc0 = new Model.Document();
//            doc0.setText("tel aviv Israel israel");
//            doc0.setDocNo("0");
//            Model.Document doc = new Model.Document();
//           doc.setText("Yakov israel lives in tel aviv Israel");
//           doc.setDocNo("1");
//           Model.Parse parser = new Model.Parse("C:\\Users\\cheng\\Documents\\University\\3rd\\Aihzur", true);
//            List<Model.Document> docs= new ArrayList<>();
//            docs.add(doc0);
//            docs.add(doc);
//            HashMap<String,Model.Term> result= parser.parse(docs);
//            System.out.println(result);
//
//        // HashMap<String, Model.Term> result = parser.parse("1992/January-12", "", "");
////            boolean work= true;
////            List<Model.Document> docs= reader.getNextDocs(50000);
////            HashMap<String,Model.Term> result;
////            ArrayList<String> sizes= new ArrayList<>();
////            int batchNum=1;
////            while (docs!=null) {
////                result = parser.parse(docs);
////                sizes.add("Batch #" + batchNum++ + " size: " + result.size());
////                if(batchNum==2)
////                    fr.write(result.keySet().toString());
////                docs= reader.getNextDocs(50000);
////                //work=false;
////                counter++;
////            }
////            fr.close();
////            double end = System.nanoTime() / Math.pow(10, 6);
////            System.out.println(end - start);
////            System.out.println(counter);
////            System.out.println(sizes);
////        }catch(IOException e){
////
////        }
//
//
//        }
//    }



