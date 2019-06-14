package sample;

import Utils.Request;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.apache.http.entity.mime.MultipartEntity;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import sample.Anims.Shake;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.ResourceBundle;

public class RegDevice {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField DeviceName;

    @FXML
    private Button Next;

    @FXML
    private Label Error;

    @FXML
    private AnchorPane MainPane;

    @FXML
    private FlowPane Header;

    @FXML
    private BorderPane BodyPane;

    @FXML
    void initialize() {
        Thread Resize = new Thread(()->{
            try {
                while (MainPane.getScene() == null || MainPane.getScene().getWindow() == null) Thread.sleep(100);
                MainPane.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                Header.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                BodyPane.prefHeightProperty().bind(MainPane.getScene().heightProperty().subtract(Header.heightProperty()));
                BodyPane.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                DeviceName.prefHeightProperty().bind(MainPane.getScene().heightProperty().divide(10));
                DeviceName.maxWidthProperty().bind(MainPane.getScene().widthProperty().subtract(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Resize.start();
    }

    @FXML
    private void onKeyPressed(KeyEvent value) throws IOException {
        if(value.getCode() == KeyCode.ENTER){
            onNextClick();
        }
    }

    @FXML
    private void onNextClick() throws IOException {
        if(!DeviceName.getText().matches("\\w+")) {
            Error.setVisible(true);
            Error.setText("Имя содержит недопустимые символы!");
            return;
        }
        File file = new File("properties.properties");
        if(!file.exists()) file.createNewFile();
        FileInputStream propFile = new FileInputStream(file);
        Properties props = new Properties();
        props.load(propFile);
        if(!props.containsKey("login")){
            props.setProperty("name", DeviceName.getText());
            props.store(new FileOutputStream("properties.properties"), "");
            AnchorPane pane = FXMLLoader.load(getClass().getResource("MainActivity.fxml"));
            MainPane.getChildren().setAll(pane);
        } else{
            int id = Integer.parseInt((String)props.getOrDefault("id","-1"));
            String login = (String)props.getOrDefault("login","");

            Runnable r = () -> {
                Request request = new Request() {
                    @Override
                    protected void On0() {
                        Platform.runLater(()-> {
                            try {
                                props.setProperty("name", DeviceName.getText());
                                props.setProperty("regdate", ((Long) result.get("regdate")).toString());
                                props.store(new FileOutputStream("properties.properties"), "");
                                AnchorPane pane = FXMLLoader.load(getClass().getResource("MainActivity.fxml"));
                                MainPane.getChildren().setAll(pane);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    protected void On1() {
                        Platform.runLater(()-> {
                            Shake onErrorShake = new Shake(Next);
                            Error.setVisible(true);
                            Error.setText("Вы уже используете это имя!");
                            onErrorShake.play();
                        });
                    }

                    @Override
                    protected void On2() {
                        Platform.runLater(()-> {
                            Shake onErrorShake = new Shake(Next);
                            Error.setVisible(true);
                            Error.setText("Ошибка приложения!");
                            onErrorShake.play();
                        });
                    }

                    @Override
                    protected void On3() {
                        Platform.runLater(()-> {
                            Shake onErrorShake = new Shake(Next);
                            Error.setVisible(true);
                            Error.setText("Вы должны указать имя!");
                            onErrorShake.play();
                        });
                    }

                    @Override
                    protected void On4() {

                    }
                };
                try {
                    request.Start("http://mysweetyphone.herokuapp.com/?Type=AddDevice&DeviceType=PC&Id="+id+"&Login="+ URLEncoder.encode(login, "UTF-8")+"&Name="+URLEncoder.encode(DeviceName.getText(), "UTF-8"), new MultipartEntity());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            };
            Thread t = new Thread(r);
            t.start();
            propFile.close();
        }
    }
}