package sample;

import Utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class SServer {

    @FXML
    private AnchorPane MainPane;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button NewSession;

    @FXML
    private ChoiceBox<String> SessionType;

    @FXML
    private BorderPane NewSessionMainPane;

    private ArrayList<Session> sessions;

    @FXML
    public void initialize(){
        Thread Resize = new Thread(()->{
            try {
                while (NewSession.getScene() == null) Thread.sleep(100);
                MainPane.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                MainPane.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty());
                NewSessionMainPane.prefHeightProperty().bind(MainPane.heightProperty());
                NewSessionMainPane.prefWidthProperty().bind(MainPane.widthProperty());
                MainPane.prefWidthProperty().bind(NewSession.getScene().widthProperty());
                MainPane.prefHeightProperty().bind(NewSession.getScene().heightProperty());

                NewSession.setDisable(false);
                SessionType.setDisable(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Resize.start();
        SessionType.getItems().add("Эмуляция мыши");
        NewSession.setOnMouseClicked(this::OpenSession);
    }

    public void OpenSession(MouseEvent e){
        try{
            if (SessionType.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Выберите тип сеанса");
                alert.show();
            } else {
                NewSession.setOnMouseClicked(this::CloseSession);
                NewSession.setText("Закрыть сессию");
                Utils.SessionServer s = new Utils.SessionServer(Session.Type.values()[SessionType.getItems().indexOf(SessionType.getValue())],0,()->{
                    NewSession.setOnMouseClicked(this::OpenSession);
                    NewSession.setText("Открыть сессию");
                    SessionType.setDisable(false);
                });
                s.Start();
                SessionType.setDisable(true);
            }
        } catch (IOException err){
            err.printStackTrace();
        }
    }

    public void CloseSession(MouseEvent e) {
        try {
            NewSession.setOnMouseClicked(this::OpenSession);
            NewSession.setText("Открыть сессию");
            Session.sessions.get(Session.sessions.size() - 1).Stop();
            Session.sessions.remove(Session.sessions.size() - 1);
            SessionType.setDisable(false);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
