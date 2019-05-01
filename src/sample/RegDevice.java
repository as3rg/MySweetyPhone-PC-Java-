package sample;

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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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
                while (MainPane.getScene() == null) Thread.sleep(100);
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
        Next.setDisable(true);
        DeviceName.setDisable(true);
        FileInputStream propFile = new FileInputStream("properties.properties");
        Properties props = new Properties();
        props.load(propFile);
        int id = Integer.parseInt((String)props.getOrDefault("id","-1"));
        String login = (String)props.getOrDefault("login","");
        Runnable r = () -> {
            try {
                URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=AddDevice&DeviceType=PC&Id="+id+"&Login="+login+"&Name="+DeviceName.getText());

                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();


                JSONObject result = (JSONObject) JSONValue.parse(response.toString());
                Long i = (Long) result.getOrDefault("code", 2);
                Platform.runLater(()-> {
                    try {
                        if (i.equals(3L)) {
                            Error.setVisible(true);
                            Error.setText("Вы должны указать имя!!");
                        } else if (i.equals(2L)) {
                            Error.setVisible(true);
                            Error.setText("Ошибка приложения!");
                        } else if (i.equals(1L)) {

                            Error.setVisible(true);
                            Error.setText("Вы уже используете это имя!");
                        } else if (i.equals(0L)) {
                            props.setProperty("name", DeviceName.getText());
                            props.setProperty("regdate", ((Long) result.get("regdate")).toString());
                            props.store(new FileOutputStream("properties.properties"), "");
                            AnchorPane pane = FXMLLoader.load(getClass().getResource("MainActivity.fxml"));
                            MainPane.getChildren().setAll(pane);
                        } else {
                            Error.setVisible(true);
                            Error.setText("Ошибка приложения!");
                        }
                        Next.setDisable(false);
                        DeviceName.setDisable(false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();
        propFile.close();
    }
}