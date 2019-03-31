package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    private Pane CallMenu;

    @FXML
    void initialize() throws IOException {
        controller = this;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DevicesList.fxml"));
        Pane pane = fxmlLoader.load();
        Replace.getChildren().setAll(pane);
        Thread Resize = new Thread(()->{
            try {
                while (MainPane.getScene() == null) Thread.sleep(100);
                Replace.prefHeightProperty().bind(MainPane.getScene().heightProperty().subtract(Header.heightProperty()));
                MenuPane.prefHeightProperty().bind(MainPane.getScene().heightProperty().subtract(Header.heightProperty()));
                Replace.prefWidthProperty().bind(MainPane.getScene().widthProperty().subtract(CallMenu.widthProperty()));
                MainPane.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                Header.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                MenuPane.visibleProperty().bind(CallMenu.hoverProperty().or(MenuPane.hoverProperty()));
                Replace.disableProperty().bind(CallMenu.hoverProperty().or(MenuPane.hoverProperty()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Resize.start();
    }

    @FXML
    void DevicesList() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DevicesList.fxml"));
        Pane pane = fxmlLoader.load();
        Replace.getChildren().setAll(pane);
    }

    @FXML
    void Saved() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Saved.fxml"));
        Pane pane = fxmlLoader.load();
        Replace.getChildren().setAll(pane);
    }

    @FXML
    void Sessions() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Sessions.fxml"));
        Pane pane = fxmlLoader.load();
        Replace.getChildren().setAll(pane);
    }
}
