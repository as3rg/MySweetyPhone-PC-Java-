package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainActivity {

    public static MainActivity controller;

    @FXML
    public ResourceBundle resources;

    @FXML
    public URL location;

    @FXML
    public FlowPane MainPane;

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


    static interface MethodToCall{
        void f() throws IOException;
    }

    MethodToCall mtc;

    @FXML
    void initialize() throws IOException {
        controller = this;
        DevicesList();
        Thread Resize = new Thread(()->{
            try {
                while (MainPane.getScene() == null) Thread.sleep(100);
                Replace.prefHeightProperty().bind(MainPane.getScene().heightProperty().subtract(Header.heightProperty()));
                Replace.prefWidthProperty().bind(MainPane.getScene().widthProperty().subtract(MenuPane.widthProperty()));
                Header.prefWidthProperty().bind(MainPane.getScene().widthProperty());
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
        Receiving receiving = new Receiving();
        receiving.Start();
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
}
