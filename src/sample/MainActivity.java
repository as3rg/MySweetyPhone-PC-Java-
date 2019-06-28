package sample;

import Utils.Receiving;
import Utils.Request;
import Utils.ServerMode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainActivity {

    public static MainActivity controller;

    @FXML
    public ResourceBundle resources;

    @FXML
    public URL location;

    @FXML
    public BorderPane MainPane;

    @FXML
    private Button Reload;

    @FXML
    public FlowPane Header;

    @FXML
    public javafx.scene.control.Label Label;

    @FXML
    public ImageView Logo;

    @FXML
    public AnchorPane Replace;

    @FXML
    private VBox MenuPane;

    @FXML
    private Button SavedButton, DevicesListButton;

    interface MethodToCall{
        void f() throws IOException;
    }

    MethodToCall mtc;

    @FXML
    void initialize() throws IOException {
        controller = this;

        File file = new File("properties.properties");
        FileInputStream propFile = new FileInputStream(file);
        Properties props = new Properties();
        props.load(propFile);
        propFile.close();
        if(props.containsKey("login")){
            DevicesList();
        }else {
            SClient();
            MenuPane.getChildren().removeAll(SavedButton,DevicesListButton);
        }

        if(Utils.ServerMode.getState())
            ServerMode.Start();

        Receiving receiving = new Receiving();
        receiving.Start();
        Thread Resize = new Thread(()->{
            try {
                while (MainPane.getScene() == null || MainPane.getScene().getWindow() == null) Thread.sleep(100);
                Replace.prefHeightProperty().bind(MainPane.getScene().getWindow().heightProperty().subtract(Header.heightProperty()));
                Replace.prefWidthProperty().bind(MainPane.getScene().getWindow().widthProperty().subtract(MenuPane.widthProperty()));
                Header.prefWidthProperty().bind(MainPane.getScene().getWindow().widthProperty());
                MenuPane.prefHeightProperty().bind(Replace.prefHeightProperty());
                for (Node b: MenuPane.getChildren()) {
                    ((Button) b).prefWidthProperty().bind(MenuPane.widthProperty());
                }
                MenuPane.hoverProperty().addListener((observableValue, aBoolean, t1) ->{
                    if(observableValue.getValue())
                        MenuPane.prefWidthProperty().set(150);
                    else
                        MenuPane.prefWidthProperty().set(50);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Resize.start();
    }

    @FXML
    void Reload() throws IOException {
        mtc.f();
    }

    @FXML
    void DevicesList() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DevicesList.fxml"));
        Pane pane = fxmlLoader.load();
        Replace.getChildren().setAll(pane);
        mtc = this::DevicesList;
    }

    @FXML
    void Saved() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Saved.fxml"));
        Pane pane = fxmlLoader.load();
        Replace.getChildren().setAll(pane);
        mtc = this::Saved;
    }

    @FXML
    void SClient() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SClient.fxml"));
        Pane pane = fxmlLoader.load();
        Replace.getChildren().setAll(pane);
        mtc = this::SClient;
    }

    @FXML
    void SServer() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SServer.fxml"));
        Pane pane = fxmlLoader.load();
        Replace.getChildren().setAll(pane);
        mtc = this::SServer;
    }

    @FXML
    void Exit() throws IOException {
        FileInputStream propFile = new FileInputStream("properties.properties");
        Properties props = new Properties();
        props.load(propFile);
        propFile.close();
        int id = Integer.parseInt((String)props.getOrDefault("id","-1"));
        String name = (String)props.getOrDefault("name","");
        String login = (String)props.getOrDefault("login","");
        new Thread(()-> {
            Request request = new Request() {
                @Override
                protected void On0() {

                }

                @Override
                protected void On1() {

                }

                @Override
                protected void On2() {

                }

                @Override
                protected void On3() {

                }

                @Override
                protected void On4() {

                }
            };
            try {
                request.Start("http://mysweetyphone.herokuapp.com/?Type=RemoveDevice&Login=" + URLEncoder.encode(login, "UTF-8") + "&Id=" + id + "&Name=" + URLEncoder.encode(name, "UTF-8"), new MultipartEntity());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }).start();
        File file = new File("properties.properties");
        file.delete();
        Platform.exit();
        System.exit(0);
    }
}
