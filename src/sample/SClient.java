package sample;

import Utils.ServerMode;
import Utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;


public class SClient {

    @FXML
    private ScrollPane ConnectToSessionScrollPane;

    @FXML
    private AnchorPane MainPane;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox ConnectToSession;

    @FXML
    private Button SearchSessions;

    @FXML
    private BorderPane ConnectToSessionMainPane;

    @FXML
    public void initialize(){
        try {
            FileInputStream propFile = new FileInputStream("properties.properties");
            Properties props = new Properties();
            props.load(propFile);
            propFile.close();
            Thread Resize = new Thread(()->{
                try {
                    while (MainPane.getScene() == null || MainPane.getScene().getWindow() == null) Thread.sleep(100);
                    MainPane.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                    MainPane.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty());
                    ConnectToSessionMainPane.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty());
                    ConnectToSessionMainPane.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                    ConnectToSessionScrollPane.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty().subtract(SearchSessions.heightProperty().subtract(10)));
                    ConnectToSession.minHeightProperty().bind(ConnectToSessionScrollPane.heightProperty());
                    MainPane.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                    MainPane.prefHeightProperty().bind(MainPane.getScene().heightProperty());
                    SearchSessions.setDisable(false);
                    ConnectToSession.setDisable(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            Resize.start();
            SearchSessions.setOnMouseClicked(this::Search);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void Search(MouseEvent mouseEvent) {
        try {
            ServerMode.Stop();
            SearchSessions.setOnMouseClicked(this::StopSearching);
            SearchSessions.setText("Остановить поиск");
            Utils.SessionClient.Search(ConnectToSession, new Thread(() -> {
                SearchSessions.setOnMouseClicked(this::Search);
                SearchSessions.setText("Поиск...");
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void StopSearching(MouseEvent mouseEvent) {
        SearchSessions.setText("Поиск...");
        SearchSessions.setOnMouseClicked(this::Search);
        Utils.SessionClient.StopSearching();
    }
}
