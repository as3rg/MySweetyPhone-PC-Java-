package sample;

import Utils.Session;
import Utils.SessionServer;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
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
    private CheckBox ServerMode;

    @FXML
    private ChoiceBox<String> SessionType;

    @FXML
    private BorderPane NewSessionMainPane;

    @FXML
    public void initialize(){
        try{
            Thread Resize = new Thread(()->{
                try {
                    while (MainPane.getScene() == null || MainPane.getScene().getWindow() == null) Thread.sleep(100);
                    MainPane.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                    MainPane.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty());
                    NewSessionMainPane.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty());
                    NewSessionMainPane.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                    MainPane.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                    MainPane.prefHeightProperty().bind(MainPane.getScene().heightProperty());


                    ServerMode.setSelected(Utils.ServerMode.getState());
                    NewSession.setDisable(ServerMode.isSelected());
                    SessionType.setDisable(ServerMode.isSelected());

                    if(SessionServer.openedServer != null){
                        NewSession.setOnMouseClicked(this::CloseSession);
                        NewSession.setText("Закрыть сессию");
                        SessionType.setDisable(true);
                    }else{
                        NewSession.setOnMouseClicked(this::OpenSession);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            Resize.start();
            for (int t : SessionServer.allowedTypes) {
                SessionType.getItems().add(Session.decodeType(t));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void OpenSession(MouseEvent e){
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
                Utils.SessionServer s = new Utils.SessionServer(Session.encodeType(SessionType.getSelectionModel().getSelectedItem()),0,()->{
                    NewSession.setOnMouseClicked(this::OpenSession);
                    NewSession.setText("Открыть сессию");
                    SessionType.setDisable(false);
                });
                s.SingleStart();
                SessionType.setDisable(true);
            }
        } catch (IOException err){
            err.printStackTrace();
        }
    }

    private void CloseSession(MouseEvent e) {
        try {
            NewSession.setOnMouseClicked(this::OpenSession);
            NewSession.setText("Открыть сессию");
            SessionServer.openedServer.Stop();
            SessionType.setDisable(false);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @FXML
    void SwitchServerMode() throws IOException {
        if(ServerMode.isSelected()){
            if(SessionServer.openedServer != null) CloseSession(null);
            Utils.ServerMode.Start();
        }else{
            Utils.ServerMode.Stop();
        }
        NewSession.setDisable(ServerMode.isSelected());
        SessionType.setDisable(ServerMode.isSelected());
    }
}
