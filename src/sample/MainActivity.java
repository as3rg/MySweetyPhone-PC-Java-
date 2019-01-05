package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

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
    private TabPane TabPane;

    @FXML
    void initialize() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("DevicesList.fxml"));
        ReplaceToDevicesList.getChildren().setAll(pane);
        pane = FXMLLoader.load(getClass().getResource("Saved.fxml"));
        ReplaceToSaved.getChildren().setAll(pane);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Saved.fxml"));
        loader.getController();
    }
}
