package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainActivity {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane MainPane;

    @FXML
    private Tab Devices;

    @FXML
    private AnchorPane ReplaceToDevicesList;

    @FXML
    private AnchorPane ReplaceToSaved;

    @FXML
    private AnchorPane ReplaceToSessions;

    @FXML
    private AnchorPane ReplaceToBlockSite;

    @FXML
    private TabPane TabPane;

    @FXML
    void initialize() throws IOException {
        FXMLLoader DevicesList = new FXMLLoader(getClass().getResource("DevicesList.fxml"));
        FXMLLoader Saved = new FXMLLoader(getClass().getResource("Saved.fxml"));
        FXMLLoader BlockSite = new FXMLLoader(getClass().getResource("BlockSite.fxml"));
        FXMLLoader Sessions = new FXMLLoader(getClass().getResource("Sessions.fxml"));
        Pane pane = DevicesList.load();
        ReplaceToDevicesList.getChildren().setAll(pane);
        pane = Saved.load();
        ReplaceToSaved.getChildren().setAll(pane);
        /*pane = BlockSite.load();
        ReplaceToBlockSite.getChildren().setAll(pane);*/
        pane = Sessions.load();
        ReplaceToSessions.getChildren().setAll(pane);
        Timeline timeline = new Timeline(new KeyFrame(new Duration(60000), ev -> {
            switch (TabPane.getSelectionModel().getSelectedItem().getText()){
                case "Устройства":
                    try {
                        ((DevicesList)DevicesList.getController()).initialize();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "Сохраненное":
                    ((Saved)Saved.getController()).initialize();
                    break;
                default:
                    break;
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
}
