package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import sample.Anims.Shake;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class Login {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField Nick;

    @FXML
    private PasswordField Pass;

    @FXML
    private Label Error;

    @FXML
    private Button RegButton;

    @FXML
    private Button LoginButton;

    @FXML
    private AnchorPane MainPane;

    @FXML
    void initialize() throws IOException, URISyntaxException {
        File file = new File("properties.properties");
        if (file.exists()) {
            RegButton.setDisable(true);
            LoginButton.setDisable(true);
            Nick.setDisable(true);
            Pass.setDisable(true);
            FileInputStream propFile = new FileInputStream(file);
            Properties props = new Properties();
            props.load(propFile);
            propFile.close();
            int id = Integer.parseInt((String) props.getOrDefault("id", "-1"));
            int regdate = Integer.parseInt((String) props.getOrDefault("regdate", "-1"));
            String login = (String) props.getOrDefault("login", "");
            String name = (String) props.getOrDefault("name", "");
            if (Long.parseLong((String) props.getOrDefault("id", "-1")) != -1L) {

                Runnable r = () -> {
                    try {
                        URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=Check&DeviceType=PC&RegDate="+regdate+"&Login=" + login + "&Id=" + id + "&Name=" + name);
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
                        if (i.equals(0L)) {
                            i = (Long) result.getOrDefault("result", 0);
                            if (i.equals(2L)) {
                                Platform.runLater(() -> {
                                    try {
                                        AnchorPane pane = FXMLLoader.load(getClass().getResource("RegDevice.fxml"));
                                        MainPane.getChildren().setAll(pane);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else if (i.equals(1L)) {
                                Platform.runLater(() ->{
                                    try {
                                        AnchorPane pane = FXMLLoader.load(getClass().getResource("MainActivity.fxml"));
                                        MainPane.getChildren().setAll(pane);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });

                            }
                        }
                        RegButton.setDisable(false);
                        LoginButton.setDisable(false);
                        Nick.setDisable(false);
                        Pass.setDisable(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                Thread t = new Thread(r);
                t.start();
            }
        }
    }

    @FXML
    void onLogin() throws Exception{

        RegOrLogin("https://mysweetyphone.herokuapp.com/?Type=Login&Login="+Nick.getText()+"&Pass="+Pass.getText(),true);
    }

    @FXML
    void onReg() throws Exception{
        RegOrLogin("https://mysweetyphone.herokuapp.com/?Type=Reg&Login="+Nick.getText()+"&Pass="+Pass.getText(), false);
    }

    private void RegOrLogin(String url, boolean IsLogin){
        Runnable r = () -> {
            try {
                URL obj = new URL(url);

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
                Shake onErrorShake = new Shake(IsLogin ? LoginButton : RegButton);
                if (i.equals(3L)) {
                    Error.setVisible(true);
                    Error.setText("Имя и Пароль должны быть заполнены!");
                    onErrorShake.play();
                } else if (i.equals(1L)) {
                    Error.setVisible(true);
                    Error.setText(IsLogin ? "Ошибка! Неверное имя или пароль" : "Ошибка! Это имя уже используется");
                    onErrorShake.play();
                } else if (i.equals(0L)) {
                    File file = new File("properties.properties");
                    if(!file.exists())
                        file.createNewFile();
                    FileOutputStream propFile = new FileOutputStream(file);
                    Properties props = new Properties();
                    props.setProperty("login",Nick.getText());
                    props.setProperty("id",result.get("id").toString());
                    props.store(propFile, "login");
                    propFile.close();

                    AnchorPane pane = FXMLLoader.load(getClass().getResource("RegDevice.fxml"));
                    MainPane.getChildren().setAll(pane);
                }else {
                    throw new Exception("Ошибка приложения!");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
}