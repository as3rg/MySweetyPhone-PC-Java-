package sample;

import Utils.Request;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import org.apache.http.entity.mime.MultipartEntity;
import sample.Anims.Shake;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
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
    private Button LoginButton;

    @FXML
    private AnchorPane MainPane;

    @FXML
    private FlowPane Header;

    @FXML
    private HBox Type;

    @FXML
    private BorderPane BodyPane;

    @FXML
    void initialize() throws IOException {
        Thread Resize = new Thread(()->{
            try {
                while (MainPane.getScene() == null || MainPane.getScene().getWindow() == null) Thread.sleep(100);
                MainPane.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                Header.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                BodyPane.prefHeightProperty().bind(MainPane.getScene().heightProperty().subtract(Header.heightProperty()));
                BodyPane.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                Nick.prefHeightProperty().bind(MainPane.getScene().heightProperty().divide(10));
                Pass.prefHeightProperty().bind(MainPane.getScene().heightProperty().divide(10));
                LoginButton.prefHeightProperty().bind(MainPane.getScene().heightProperty().divide(10));
                Nick.maxWidthProperty().bind(MainPane.getScene().widthProperty().subtract(10));
                Pass.maxWidthProperty().bind(MainPane.getScene().widthProperty().subtract(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Resize.start();
        File file = new File("properties.properties");
        if (file.exists()) {
            Nick.setDisable(true);
            Pass.setDisable(true);
            Type.setDisable(true);
            LoginButton.setDisable(true);
            FileInputStream propFile = new FileInputStream(file);
            Properties props = new Properties();
            props.load(propFile);
            propFile.close();
            int id = Integer.parseInt((String) props.getOrDefault("id", "-1"));
            int regdate = Integer.parseInt((String) props.getOrDefault("regdate", "-1"));
            String login = (String) props.getOrDefault("login", "");
            String name = (String) props.getOrDefault("name", "");
            if(login.isEmpty()&& !name.isEmpty()){
                AnchorPane pane = FXMLLoader.load(getClass().getResource("MainActivity.fxml"));
                MainPane.getChildren().setAll(pane);
            } else if (props.containsKey("login")) {
                Runnable r = () -> {
                    Request request = new Request() {
                        @Override
                        protected void On0() {
                            int i = ((Long) result.getOrDefault("result", 0)).intValue();
                            if (i == 2) {
                                Platform.runLater(() -> {
                                    try {
                                        AnchorPane pane = FXMLLoader.load(getClass().getResource("RegDevice.fxml"));
                                        MainPane.getChildren().setAll(pane);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else if (i == 1) {
                                Platform.runLater(() -> {
                                    try {
                                        AnchorPane pane = FXMLLoader.load(getClass().getResource("MainActivity.fxml"));
                                        MainPane.getChildren().setAll(pane);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });

                            }
                        }

                        @Override
                        protected void On1() {
                            File file = new File("properties.properties");
                            file.delete();
                            Platform.exit();
                        }

                        @Override
                        protected void On2() {
                            throw new RuntimeException("Ошибка приложения!");
                        }

                        @Override
                        protected void On3() {
                            throw new RuntimeException("Файл не отправлен!");
                        }

                        @Override
                        protected void OnError(Throwable e) {
                            if(e instanceof UnknownHostException)
                                Platform.runLater(()-> {
                                    Shake onErrorShake = new Shake(LoginButton);
                                    Error.setVisible(true);
                                    Error.setText("Сервер недоуступен! Используйте Offline режим");
                                    onErrorShake.play();
                                });
                        }
                    };
                    try {
                        request.Start("http://mysweetyphone.herokuapp.com/?Type=Check&DeviceType=PC&RegDate=" + regdate + "&Login=" + URLEncoder.encode(login, "UTF-8") + "&Id=" + id + "&Name=" + URLEncoder.encode(name, "UTF-8"), new MultipartEntity());
                        Nick.setDisable(false);
                        Pass.setDisable(false);
                        Type.setDisable(false);
                        LoginButton.setDisable(false);
                        ChangeToLogin();
                    } catch (UnsupportedEncodingException | RuntimeException e) {
                        e.printStackTrace();
                    }
                };
                Thread t = new Thread(r);
                t.start();
            }
        }

        LoginButton.setOnMouseClicked(event ->
        {
            try {
                RegOrLogin("http://mysweetyphone.herokuapp.com/?Type=Login&Login="+URLEncoder.encode(Nick.getText(), "UTF-8")+"&Pass="+URLEncoder.encode(Pass.getText(), "UTF-8"),true);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void ChangeToReg(){
        LoginButton.setText("Зарегистрироваться");
        Nick.setDisable(false);
        Pass.setDisable(false);
        LoginButton.setOnMouseClicked(event -> {
            try {
                RegOrLogin("http://mysweetyphone.herokuapp.com/?Type=Reg&Login="+URLEncoder.encode(Nick.getText(), "UTF-8")+"&Pass="+URLEncoder.encode(Pass.getText(), "UTF-8"), false);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void ChangeToLogin(){
        LoginButton.setText("Войти");
        Nick.setDisable(false);
        Pass.setDisable(false);
        LoginButton.setOnMouseClicked(event -> {
            try {
                RegOrLogin("http://mysweetyphone.herokuapp.com/?Type=Login&Login="+URLEncoder.encode(Nick.getText(), "UTF-8")+"&Pass="+URLEncoder.encode(Pass.getText(), "UTF-8"),true);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void ChangeToOffline(){
        LoginButton.setText("Включить Offline Режим");
        Nick.setDisable(true);
        Pass.setDisable(true);
        LoginButton.setOnMouseClicked(this::Offline);
    }

    @FXML
    void Offline(MouseEvent event){
        try {
            File file = new File("properties.properties");
            file.delete();
            AnchorPane pane = FXMLLoader.load(getClass().getResource("RegDevice.fxml"));
            MainPane.getChildren().setAll(pane);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void RegOrLogin(String url, boolean IsLogin){
        if (!Nick.getText().matches("\\w+")) {
            Error.setVisible(true);
            Error.setText("Имя содержит недопустимые символы!");
            return;
        }
        LoginButton.setDisable(true);
        Nick.setDisable(true);
        Pass.setDisable(true);
        Type.setDisable(true);
        Runnable r = () -> {
            Request request = new Request() {
                @Override
                protected void On0() {
                    Platform.runLater(() -> {
                        try {
                            File file = new File("properties.properties");
                            if (!file.exists())
                                file.createNewFile();
                            FileOutputStream propFile = new FileOutputStream(file);
                            Properties props = new Properties();
                            props.setProperty("login", Nick.getText());
                            props.setProperty("id", result.get("id").toString());
                            props.store(propFile, null);
                            propFile.close();
                            AnchorPane pane = FXMLLoader.load(getClass().getResource("RegDevice.fxml"));
                            MainPane.getChildren().setAll(pane);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                protected void On1() {
                    Platform.runLater(() -> {
                        Shake onErrorShake = new Shake(LoginButton);
                        Error.setVisible(true);
                        Error.setText(IsLogin ? "Ошибка! Неверное имя или пароль" : "Ошибка! Это имя уже используется");
                        onErrorShake.play();
                    });
                }

                @Override
                protected void On3() {
                    Platform.runLater(() -> {
                        Shake onErrorShake = new Shake(LoginButton);
                        Error.setVisible(true);
                        Error.setText("Имя и Пароль должны быть заполнены!");
                        onErrorShake.play();
                    });
                }

                @Override
                protected void OnError(Throwable e) {
                    if(e instanceof UnknownHostException)
                        Platform.runLater(()-> {
                            Shake onErrorShake = new Shake(LoginButton);
                            Nick.setDisable(false);
                            Pass.setDisable(false);
                            Type.setDisable(false);
                            LoginButton.setDisable(false);
                            Error.setVisible(true);
                            Error.setText("Сервер недоуступен! Используйте Offline режим");
                            onErrorShake.play();
                        });
                }
            };
            request.Start(url, new MultipartEntity());
        };
        Thread t = new Thread(r);
        t.start();
    }
}