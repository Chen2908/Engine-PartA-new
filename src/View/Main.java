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

/**
 * main class, uploads the application
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();
        ViewModel viewModel = new ViewModel(model);
        model.addObserver(viewModel);
        //--------------
        primaryStage.setTitle("Zoogle, your one and only search engine");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("scene.fxml"));
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





