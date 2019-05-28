package sample;

import Utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
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
    private VBox ConnectToSessionMainPane;

    private ArrayList<Session> sessions;

    @FXML
    public void initialize(){
        Thread Resize = new Thread(()->{
            try {
                while (MainPane.getScene() == null) Thread.sleep(100);
                MainPane.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                MainPane.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty());
                ConnectToSessionMainPane.prefHeightProperty().bind(MainPane.heightProperty());
                ConnectToSessionMainPane.prefWidthProperty().bind(MainPane.widthProperty());
                ConnectToSessionScrollPane.prefHeightProperty().bind(MainPane.prefHeightProperty().subtract(SearchSessions.heightProperty().subtract(10)));
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
    }

    @FXML
    public void Search(MouseEvent mouseEvent) {
        try {
            SearchSessions.setOnMouseClicked(this::StopSearching);
            SearchSessions.setText("Остановить поиск");
            Utils.SessionClient.Search(ConnectToSession, new Thread(() -> {
                SearchSessions.setOnMouseClicked(this::Search);
                SearchSessions.setText("Поиск...");
            }));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void StopSearching(MouseEvent mouseEvent) {
        SearchSessions.setText("Поиск...");
        SearchSessions.setOnMouseClicked(this::Search);
        Utils.SessionClient.StopSearching();
    }
}
