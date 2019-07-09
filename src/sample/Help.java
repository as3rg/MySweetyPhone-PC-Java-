package sample;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Help {
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private WebView Text;

    class MyText extends Text{
        MyText(String text, FontWeight fw, double d){
            setFill(Color.WHITE);
            setFont(Font.font("Helvetica", fw,d));
            setText(text);
        }
    }

    @FXML
    void initialize() {
        Thread Resize = new Thread(()->{
            try {
                while (Text.getScene() == null || Text.getScene().getWindow() == null) Thread.sleep(100);
                Text.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                Text.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty().subtract(40));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Resize.start();
        Text.getEngine().load(getClass().getResource("Help.html").toExternalForm());
    }
}
