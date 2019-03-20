package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

public class Main extends Application {

    static ArrayList<File> tempfiles;
    @Override
    public void start(Stage primaryStage) throws Exception{
        tempfiles = new ArrayList<>();
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        primaryStage.setTitle("MySweetyPhone");
        Scene scene = new Scene(root,1270,720 );
        scene.getStylesheets().add(Main.class.getResource("Style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("Images/Icon.png")));
        primaryStage.setOnCloseRequest(event -> Platform.exit());
        primaryStage.show();

    }

    @Override
    public void stop() throws Exception{
        for(File f : tempfiles){
            f.deleteOnExit();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
