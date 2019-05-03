package sample;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import sample.Anims.Create;
import sample.Anims.Destroy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;


public class Saved {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button SendButton, FileButton, LoadButton;

    @FXML
    private FlowPane SendBar;

    @FXML
    private VBox Messages;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private BorderPane MainPane;

    @FXML
    private TextArea MessageText;

    @FXML
    private AnchorPane RootPane;

    private String name;
    private String login;
    private int id;
    private int regdate;

    @FXML
    void initialize() {
        Thread Resize = new Thread(()->{
            try {
                while (Messages.getScene() == null) Thread.sleep(100);
                scrollPane.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                MessageText.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty().divide(10).multiply(8).subtract(50));
                SendButton.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty().divide(10));
                FileButton.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty().divide(10));
                LoadButton.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty().divide(5).multiply(2));
                SendBar.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                MainPane.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                MainPane.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Resize.start();
        LoadMore();
        Messages.setFillWidth(false);
    }

    @FXML
    void LoadMore(){
        LoadMore(10);
    }

    private void LoadMore(int Count){
        Runnable r = () -> {
            try {
                FileInputStream propFile = new FileInputStream("properties.properties");
                Properties props = new Properties();
                props.load(propFile);
                propFile.close();
                id = Integer.parseInt((String) props.getOrDefault("id", "-1"));
                regdate = Integer.parseInt((String)props.getOrDefault("regdate","-1"));
                login = (String) props.getOrDefault("login", "");
                name = (String) props.getOrDefault("name", "");

                URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=GetMessages&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id+"&Count="+(Messages.getChildren().size()+Count));
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
                int i = ((Long) result.getOrDefault("code", 2)).intValue();
                if(i == 2){
                    throw new RuntimeException("Ошибка приложения!");
                }else if(i == 1){
                    throw new RuntimeException("Неверные данные");
                }else if(i == 0){
                    Platform.runLater(()-> {
                        Object[] messages = ((JSONArray) result.get("messages")).toArray();
                        Messages.getChildren().remove(0,Messages.getChildren().size());
                        for (int j = messages.length-1; j >= 0; j--) {
                            JSONObject message = (JSONObject) messages[j];
                            Draw(((String) (message).get("msg")).replace("\\n", "\n"), (Long) (message).get("date"), (String) (message).get("sender"), ((String) (message).get("type")).equals("File"),  false);
                        }
                        LoadButton.setVisible((Boolean)(result.get("hasnext")));
                    });
                }else if(i == 4){
                    Platform.runLater(() ->{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event -> Platform.exit());
                        alert.show();
                    });

                }else{
                    throw new RuntimeException("Ошибка приложения!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private void Draw(String text, Long date, String sender, boolean isFile, Boolean needsAnim) {
        if(isFile)
            DrawFile(text, date, sender, needsAnim);
        else
            DrawText(text, date, sender, needsAnim);
    }

    private void DrawText(String text, Long date, String sender, Boolean needsAnim) {
        Date Date = new Date(date * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        VBox vBox = new VBox();
        Label textLabel = new Label();
        Label DateLabel = new Label();
        vBox.maxWidthProperty().bind(Messages.widthProperty().divide(2));
        vBox.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #d53369, #cbad6d); -fx-background-radius: 10;");
        DateLabel.setStyle("-fx-text-fill: #ffffff;");
        DateLabel.setText(sdf.format(Date) + ", " + sender);
        DateLabel.setPadding(new Insets(0, 10, 0, 10));
        textLabel.setText(text);
        textLabel.setFont(Font.font(15));
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        if(text.isEmpty()){
            textLabel.setText("Пустое сообщение");
            textLabel.setStyle("-fx-text-fill: grey;");
            textLabel.setFont(Font.font(textLabel.getFont().getFamily(), FontPosture.ITALIC, textLabel.getFont().getSize()));
        }
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(textLabel);
        vBox.getChildren().add(DateLabel);
        VBox.setMargin(vBox, new Insets(5, 0, 0, 0));

        final ContextMenu contextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Удалить");
        contextMenu.getItems().addAll(delete);
        delete.setOnAction(event -> {
            EventHandler r = (event1) -> {
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DelMessage&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id+"&Date="+date+"&Msg="+text.replace(" ","%20").replace("\n","\\n"));

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
                    int i = ((Long) result.getOrDefault("code", 2)).intValue();
                    if(i == 2){
                        throw new RuntimeException("Ошибка приложения!");
                    }else if(i == 1){
                        throw new RuntimeException("Неверные данные");
                    }else if(i == 0){
                    }else if(i == 4){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event2 -> Platform.exit());
                        alert.show();
                    }else{
                        throw new RuntimeException("Ошибка приложения!");
                    }
                    Messages.getChildren().remove(vBox);
                    if(Messages.getChildren().size() < 10)
                        LoadMore(10 - Messages.getChildren().size() + 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Destroy anim = new Destroy(vBox);
            anim.play(r);
        });
        vBox.setOnContextMenuRequested((EventHandler<Event>) event -> contextMenu.show(vBox, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y));
        Platform.runLater(() -> {
            Messages.getChildren().add(vBox);
            if (needsAnim) {
                Create anim = new Create(vBox);
                anim.play();
                scrollPane.vvalueProperty().bind(vBox.heightProperty().divide(vBox.heightProperty()));
            }
        });
    }

    private void DrawFile(String text, Long date, String sender, Boolean needsAnim) {
        if(text.toLowerCase().contains(".png") || text.toLowerCase().contains(".jpg") || text.toLowerCase().contains(".jpeg") || text.toLowerCase().contains("bmp") || text.toLowerCase().contains("gif")){
            DrawImage(text, date, sender, needsAnim);
            return;
        }
        if(text.toLowerCase().contains("mp4") || text.toLowerCase().contains("flv")){
            DrawVideo(text, date, sender, needsAnim);
            return;
        }
        if(text.toLowerCase().contains("wav") || text.toLowerCase().contains("mp3")){
            DrawAudio(text, date, sender, needsAnim);
            return;
        }
        Date Date = new java.util.Date(date * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd.MM.yyyy");
        VBox vBox = new VBox();
        Label textLabel = new Label();
        Label DateLabel = new Label();
        vBox.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #d53369, #cbad6d); -fx-background-radius: 10;");
        vBox.maxWidthProperty().bind(Messages.widthProperty().divide(2));
        DateLabel.setStyle("-fx-text-fill: #ffffff;");
        DateLabel.setText(sdf.format(Date) + ", " + sender);
        DateLabel.setPadding(new Insets(0, 10, 0, 10));
        textLabel.setText(text);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        textLabel.setFont(Font.font(15));
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        vBox.getChildren().add(textLabel);
        vBox.getChildren().add(DateLabel);

        File file = new File("src/sample/Images/Download.png");
        javafx.scene.image.Image image = new Image(file.toURI().toString());
        ImageView Icon = new ImageView(image);
        Icon.setFitWidth(150);
        Icon.setFitHeight(150);

        vBox.getChildren().add(0,Icon);
        VBox.setMargin(vBox, new Insets(5, 0, 0, 0));

        final ContextMenu contextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Удалить");
        MenuItem save = new MenuItem("Сохранить как");
        contextMenu.getItems().addAll(delete);
        contextMenu.getItems().addAll(save);
        delete.setOnAction(event -> {
            EventHandler r = (event1) -> {
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DelMessage&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id+"&Date="+date+"&Msg="+text.replace(" ","%20").replace("\n","\\n"));

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
                    int i = ((Long) result.getOrDefault("code", 2)).intValue();
                    if(i == 2){
                        throw new RuntimeException("Ошибка приложения!");
                    }else if(i == 1){
                        throw new RuntimeException("Неверные данные");
                    }else if(i == 0){
                    }else if(i == 4){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event2 -> Platform.exit());
                        alert.show();
                    }else{
                        throw new RuntimeException("Ошибка приложения!");
                    }
                    Messages.getChildren().remove(vBox);
                    if(Messages.getChildren().size() < 10)
                        LoadMore(10 - Messages.getChildren().size() + 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Destroy anim = new Destroy(vBox);
            anim.play(r);
        });

        save.setOnAction(event -> {
            DirectoryChooser fc = new DirectoryChooser();
            fc.setTitle("Выберите папку для сохранения");
            final File out = fc.showDialog(null);
            if (out == null) return;
            Runnable r = () -> {
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DownloadFile&RegDate="+regdate+"&MyName=" + name + "&Login=" + login + "&Id=" + id + "&FileName=" + text.replace(" ","%20") + "&Date=" + date);

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
                    String filebody = (String)result.get("filebody");
                    File out2 = new File(out,"MySweetyPhone");
                    out2.mkdirs();
                    FileOutputStream fos = new FileOutputStream(new File(out2, text));
                    fos.write(Hex.decodeHex(filebody.substring(2).toCharArray()));
                    fos.close();
                } catch (IOException | DecoderException e) {
                    e.printStackTrace();
                }
            };
            Thread t = new Thread(r);
            t.start();
        });

        vBox.setOnContextMenuRequested((EventHandler<Event>) event -> contextMenu.show(vBox, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y));
        Platform.runLater(() -> {
            Messages.getChildren().add(vBox);
            if (needsAnim) {
                Create anim = new Create(vBox);
                anim.play();
                scrollPane.vvalueProperty().bind(vBox.heightProperty().divide(vBox.heightProperty()));
            }
        });
    }

    private void DrawImage(String text, Long date, String sender, Boolean needsAnim) {
        Date Date = new java.util.Date(date * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd.MM.yyyy");
        VBox vBox = new VBox();
        VBox TextVBox = new VBox();
        Label textLabel = new Label();
        Label DateLabel = new Label();
        TextVBox.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #d53369, #cbad6d); -fx-background-radius: 0 0 10 10;");
        TextVBox.setMinWidth(300);
        TextVBox.setMaxWidth(460);
        DateLabel.setStyle("-fx-text-fill: #ffffff;");
        DateLabel.setText(sdf.format(Date) + ", " + sender);
        DateLabel.setPadding(new Insets(0, 10, 0, 10));
        textLabel.setText(text);
        TextVBox.setPadding(new Insets(5, 10, 10, 10));
        textLabel.setFont(Font.font(15));
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        TextVBox.getChildren().add(textLabel);
        TextVBox.getChildren().add(DateLabel);

        ImageView Image = new ImageView();
        Image.setStyle("-fx-background-radius: 10 10 0 0;");
        Image.setFitWidth(460);

        (new Thread(() -> {
            while (true)
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DownloadFile&RegDate="+regdate+"&MyName=" + name + "&Login=" + login + "&Id=" + id + "&FileName=" + text.replace(" ","%20") + "&Date=" + date);
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
                    String filebody = (String)result.get("filebody");

                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(Hex.decodeHex(filebody.substring(2).toCharArray())));
                    Platform.runLater(()->{
                        Image.setFitHeight(460*image.getHeight()/image.getWidth());
                        Image.setImage(SwingFXUtils.toFXImage(image, null));
                    });
                    break;
                } catch (IOException | DecoderException e) {
                    e.printStackTrace();
                }
        })).start();

        vBox.getChildren().add(0,Image);
        vBox.getChildren().add(TextVBox);
        VBox.setMargin(vBox, new Insets(5, 0, 0, 0));

        final ContextMenu contextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Удалить");
        MenuItem save = new MenuItem("Сохранить как");
        contextMenu.getItems().addAll(delete);
        contextMenu.getItems().add(save);
        delete.setOnAction(event -> {
            EventHandler r = (event1) -> {
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DelMessage&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id+"&Date="+date+"&Msg="+text.replace(" ","%20").replace("\n","\\n"));

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
                    int i = ((Long) result.getOrDefault("code", 2)).intValue();
                    if(i == 2){
                        throw new RuntimeException("Ошибка приложения!");
                    }else if(i == 1){
                        throw new RuntimeException("Неверные данные");
                    }else if(i == 0){
                    }else if(i == 4){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event2 -> Platform.exit());
                        alert.show();
                    }else{
                        throw new RuntimeException("Ошибка приложения!");
                    }
                    Messages.getChildren().remove(vBox);
                    if(Messages.getChildren().size() < 10)
                        LoadMore(10 - Messages.getChildren().size() + 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Destroy anim = new Destroy(vBox);
            anim.play(r);
        });
        save.setOnAction(event -> {
            DirectoryChooser fc = new DirectoryChooser();
            fc.setTitle("Выберите папку для сохранения");
            final File out = fc.showDialog(null);
            if(out == null) return;
            Runnable r = () -> {
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DownloadFile&RegDate="+regdate+"&MyName=" + name + "&Login=" + login + "&Id=" + id + "&FileName=" + text.replace(" ","%20") + "&Date=" + date);
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
                    String filebody = (String)result.get("filebody");
                    File out2 = new File(out,"MySweetyPhone");
                    out2.mkdirs();
                    FileOutputStream fos = new FileOutputStream(new File(out2, text));
                    fos.write(Hex.decodeHex(filebody.substring(2).toCharArray()));
                    fos.close();
                } catch (IOException | DecoderException e) {
                    e.printStackTrace();
                }
            };
            Thread t = new Thread(r);
            t.start();
        });

        vBox.setOnContextMenuRequested((EventHandler<Event>) event -> contextMenu.show(vBox, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y));
        Platform.runLater(() -> {
            Messages.getChildren().add(vBox);
            if (needsAnim) {
                Create anim = new Create(vBox);
                anim.play();
                scrollPane.vvalueProperty().bind(vBox.heightProperty().divide(vBox.heightProperty()));
            }
        });
    }

    private void DrawVideo(String text, Long date, String sender, Boolean needsAnim) {
        Date Date = new java.util.Date(date * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd.MM.yyyy");
        VBox vBox = new VBox();
        VBox TextVBox = new VBox();
        Label textLabel = new Label();
        Label DateLabel = new Label();
        TextVBox.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #d53369, #cbad6d); -fx-background-radius: 0 0 10 10;");
        TextVBox.setMinWidth(300);
        TextVBox.setMaxWidth(460);
        vBox.maxWidthProperty().bind(Messages.widthProperty().divide(2));
        vBox.setMinWidth(300);
        DateLabel.setStyle("-fx-text-fill: #ffffff;");
        DateLabel.setText(sdf.format(Date) + ", " + sender);
        DateLabel.setPadding(new Insets(0, 10, 0, 10));
        textLabel.setText(text);
        TextVBox.setPadding(new Insets(5, 10, 10, 10));
        textLabel.setFont(Font.font(15));
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        TextVBox.getChildren().add(textLabel);
        TextVBox.getChildren().add(DateLabel);

        MediaView Video = new MediaView();

        Video.setStyle("-fx-background-radius: 10 10 0 0;");
        Video.setFitWidth(460);

        Slider slider = new Slider(0,10,0);

        TextVBox.getChildren().add(0,slider);
        new Thread(() -> {
            while (true)
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DownloadFile&RegDate="+regdate+"&MyName=" + name + "&Login=" + login + "&Id=" + id + "&FileName=" + text.replace(" ","%20") + "&Date=" + date);
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
                    String filebody = (String)result.get("filebody");

                    File out = File.createTempFile(text, ".tmp");
                    Main.tempfiles.add(out);
                    FileOutputStream fos = new FileOutputStream(out);
                    fos.write(Hex.decodeHex(filebody.substring(2).toCharArray()));
                    fos.close();
                    Video.setMediaPlayer(new MediaPlayer(new Media(out.toURI().toString())));
                    Video.getMediaPlayer().setOnEndOfMedia(()->{
                        Video.getMediaPlayer().seek(Duration.ZERO);
                        Video.getMediaPlayer().pause();
                    });
                    Video.getMediaPlayer().setOnReady(() -> {
                        slider.setMax(Video.getMediaPlayer().getMedia().getDuration().toMillis());
                        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                            if (slider.isPressed())
                                Video.getMediaPlayer().seek(Duration.millis(newVal.doubleValue()));
                        });
                        Video.getMediaPlayer().currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                            slider.setValue(newValue.toMillis());
                        });
                        Video.getMediaPlayer().setOnReady(()->{});
                    });
                    break;
                } catch (IOException | DecoderException e) {}
        }).start();

        Video.setOnMouseClicked(event1 -> {
            if(Video.getMediaPlayer().getStatus().equals(MediaPlayer.Status.PLAYING))
                Video.getMediaPlayer().pause();
            else
                Video.getMediaPlayer().play();
        });
        vBox.getChildren().add(0,Video);
        vBox.getChildren().add(TextVBox);
        VBox.setMargin(vBox, new Insets(5, 0, 0, 0));

        final ContextMenu contextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Удалить");
        MenuItem save = new MenuItem("Сохранить как");
        contextMenu.getItems().addAll(delete);
        contextMenu.getItems().add(save);
        delete.setOnAction(event -> {
            if(Video.getMediaPlayer() != null) Video.getMediaPlayer().stop();
            EventHandler r = (event1) -> {
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DelMessage&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id+"&Date="+date+"&Msg="+text.replace(" ","%20").replace("\n","\\n"));

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
                    int i = ((Long) result.getOrDefault("code", 2)).intValue();
                    if(i == 2){
                        throw new RuntimeException("Ошибка приложения!");
                    }else if(i == 1){
                        throw new RuntimeException("Неверные данные");
                    }else if(i == 0){
                    }else if(i == 4){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event2 -> Platform.exit());
                        alert.show();
                    }else{
                        throw new RuntimeException("Ошибка приложения!");
                    }
                    Messages.getChildren().remove(vBox);
                    if(Messages.getChildren().size() < 10)
                        LoadMore(10 - Messages.getChildren().size() + 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Destroy anim = new Destroy(vBox);
            anim.play(r);
        });
        save.setOnAction(event -> {
            DirectoryChooser fc = new DirectoryChooser();
            fc.setTitle("Выберите папку для сохранения");
            final File out = fc.showDialog(null);
            if(out == null) return;
            Runnable r = () -> {
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DownloadFile&RegDate="+regdate+"&MyName=" + name + "&Login=" + login + "&Id=" + id + "&FileName=" + text.replace(" ","%20") + "&Date=" + date);
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
                    String filebody = (String)result.get("filebody");
                    File out2 = new File(out,"MySweetyPhone");
                    out2.mkdirs();
                    FileOutputStream fos = new FileOutputStream(new File(out2, text));
                    fos.write(Hex.decodeHex(filebody.substring(2).toCharArray()));
                    fos.close();
                } catch (IOException | DecoderException e) {
                    e.printStackTrace();
                }
            };
            Thread t = new Thread(r);
            t.start();
        });

        vBox.setOnContextMenuRequested((EventHandler<Event>) event -> contextMenu.show(vBox, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y));
        Platform.runLater(() -> {
            Messages.getChildren().add(vBox);
            if (needsAnim) {
                Create anim = new Create(vBox);
                anim.play();
                scrollPane.vvalueProperty().bind(vBox.heightProperty().divide(vBox.heightProperty()));
            }
        });
    }

    private void DrawAudio(String text, Long date, String sender, Boolean needsAnim) {
        Date Date = new java.util.Date(date * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd.MM.yyyy");
        VBox vBox = new VBox();
        VBox TextVBox = new VBox();
        Label textLabel = new Label();
        Label DateLabel = new Label();
        TextVBox.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #d53369, #cbad6d); -fx-background-radius: 10 10 10 10;");
        TextVBox.setMinWidth(300);
        TextVBox.setMaxWidth(460);
        vBox.maxWidthProperty().bind(Messages.widthProperty().divide(2));
        vBox.setMinWidth(300);
        DateLabel.setStyle("-fx-text-fill: #ffffff;");
        DateLabel.setText(sdf.format(Date) + ", " + sender);
        DateLabel.setPadding(new Insets(0, 10, 0, 10));
        textLabel.setText(text);
        TextVBox.setPadding(new Insets(5, 10, 10, 10));
        textLabel.setFont(Font.font(15));
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        textLabel.setStyle("-fx-text-fill: #ffffff;");
        TextVBox.getChildren().add(textLabel);
        TextVBox.getChildren().add(DateLabel);

        MediaView Video = new MediaView();

        Slider slider = new Slider(0,10,0);
        TextVBox.getChildren().add(0,slider);

        File file = new File("src/sample/Images/Download.png");
        javafx.scene.image.Image pause = new Image(file.toURI().toString());
        ImageView Icon = new ImageView(pause);
        Icon.setFitWidth(150);
        Icon.setFitHeight(150);

        Icon.setOnMouseClicked(event1 -> {
            if(Video.getMediaPlayer().getStatus().equals(MediaPlayer.Status.PLAYING))
                Video.getMediaPlayer().pause();
            else
                Video.getMediaPlayer().play();
        });
        TextVBox.getChildren().add(0,Icon);

        new Thread(() -> {

            while (true) {
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DownloadFile&RegDate=" + regdate + "&MyName=" + name + "&Login=" + login + "&Id=" + id + "&FileName=" + text.replace(" ", "%20") + "&Date=" + date);

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
                    String filebody = (String) result.get("filebody");

                    File out = File.createTempFile(text, ".tmp");
                    Main.tempfiles.add(out);
                    FileOutputStream fos = new FileOutputStream(out);
                    fos.write(Hex.decodeHex(filebody.substring(2).toCharArray()));
                    fos.close();
                    Video.setMediaPlayer(new MediaPlayer(new Media(out.toURI().toString())));
                    Video.getMediaPlayer().setOnEndOfMedia(() -> {
                        Video.getMediaPlayer().seek(Duration.ZERO);
                        Video.getMediaPlayer().pause();
                    });
                    Video.getMediaPlayer().setOnReady(() -> {
                        slider.setMax(Video.getMediaPlayer().getMedia().getDuration().toMillis());
                        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                            if (slider.isPressed())
                                Video.getMediaPlayer().seek(Duration.millis(newVal.doubleValue()));
                        });
                        Video.getMediaPlayer().currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                            slider.setValue(newValue.toMillis());
                        });
                        Video.getMediaPlayer().setOnReady(() -> {
                        });
                    });
                    break;
                } catch (DecoderException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        vBox.getChildren().add(TextVBox);
        VBox.setMargin(vBox, new Insets(5, 0, 0, 0));

        final ContextMenu contextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Удалить");
        MenuItem save = new MenuItem("Сохранить как");
        contextMenu.getItems().addAll(delete);
        contextMenu.getItems().add(save);
        delete.setOnAction(event -> {
            Video.getMediaPlayer().stop();
            EventHandler r = (event1) -> {
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DelMessage&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id+"&Date="+date+"&Msg="+text.replace(" ","%20").replace("\n","\\n"));

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
                    int i = ((Long) result.getOrDefault("code", 2)).intValue();
                    if(i == 2){
                        throw new RuntimeException("Ошибка приложения!");
                    }else if(i == 1){
                        throw new RuntimeException("Неверные данные");
                    }else if(i == 0){
                    }else if(i == 4){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event2 -> Platform.exit());
                        alert.show();
                    }else{
                        throw new RuntimeException("Ошибка приложения!");
                    }
                    Messages.getChildren().remove(vBox);
                    if(Messages.getChildren().size() < 10)
                        LoadMore(10 - Messages.getChildren().size() + 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Destroy anim = new Destroy(vBox);
            anim.play(r);
        });
        save.setOnAction(event -> {
            DirectoryChooser fc = new DirectoryChooser();
            fc.setTitle("Выберите папку для сохранения");
            final File out = fc.showDialog(null);
            if(out == null) return;
            Runnable r = () -> {
                try {
                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DownloadFile&RegDate="+regdate+"&MyName=" + name + "&Login=" + login + "&Id=" + id + "&FileName=" + text.replace(" ","%20") + "&Date=" + date);
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
                    String filebody = (String)result.get("filebody");
                    File out2 = new File(out,"MySweetyPhone");
                    out2.mkdirs();
                    FileOutputStream fos = new FileOutputStream(new File(out2, text));
                    fos.write(Hex.decodeHex(filebody.substring(2).toCharArray()));
                    fos.close();
                } catch (IOException | DecoderException e) {
                    e.printStackTrace();
                }
            };
            Thread t = new Thread(r);
            t.start();
        });

        vBox.setOnContextMenuRequested((EventHandler<Event>) event -> contextMenu.show(vBox, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y));
        Platform.runLater(() -> {
            Messages.getChildren().add(vBox);
            if (needsAnim) {
                Create anim = new Create(vBox);
                anim.play();
                scrollPane.vvalueProperty().bind(vBox.heightProperty().divide(vBox.heightProperty()));
            }
        });
    }

    @FXML
    private void onSendClick(){
        Runnable r;
        r = () -> {
            try {
                URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=SendMessage&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id+"&Msg="+MessageText.getText().replace(" ","%20").replace("\n","\\n"));
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
                int i = ((Long) result.getOrDefault("code", 2)).intValue();
                if(i == 2){
                    throw new RuntimeException("Ошибка приложения!");
                }else if(i == 1){
                    throw new RuntimeException("Неверные данные");
                }else if(i == 0){
                    Draw(MessageText.getText(), (Long) result.getOrDefault("time", 2), name, false, true);
                    MessageText.setText("");
                }else if(i == 4){
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event -> Platform.exit());
                        alert.show();
                    });
                }else{
                    throw new RuntimeException("Ошибка приложения!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    @FXML
    private void onFileClicked() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите файл для отправки");
        List files = fc.showOpenMultipleDialog(null);
        ArrayList<String> tooBig = new ArrayList<>();
        ArrayList<String> badName = new ArrayList<>();
        if (files == null) return;
        for (Object f : files) {
            File file = (File) f;
            if (file.length() >= 1100000) {
                tooBig.add(file.getName());
                continue;
            }
            if (!Charset.forName("US-ASCII").newEncoder().canEncode(file.getName())) {
                badName.add(file.getName());
                continue;
            }
            if (file != null) {
                Runnable r = () -> {
                    try {
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost("http://mysweetyphone.herokuapp.com/?Type=UploadFile&RegDate=" + regdate + "&MyName=" + name + "&Login=" + login + "&Id=" + id);
                        MultipartEntity entity = new MultipartEntity();
                        entity.addPart("fileToUpload", new FileBody(file));
                        entity.addPart("submit", new StringBody(""));
                        post.setEntity(entity);
                        String response = EntityUtils.toString(client.execute(post).getEntity(), "UTF-8");
                        JSONObject result = (JSONObject) JSONValue.parse(response);
                        System.out.println(response);
                        int i = ((Long) result.getOrDefault("code", 2)).intValue();
                        if (i == 2) {
                            throw new RuntimeException("Ошибка приложения!");
                        } else if (i == 1) {
                            throw new RuntimeException("Неверные данные");
                        } else if (i == 0) {
                            Draw(file.getName(), (Long) result.getOrDefault("time", 2), name, true, true);
                        } else if (i == 4) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Ошибка");
                            alert.setHeaderText(null);
                            alert.setContentText("Ваше устройство не зарегистрировано!");
                            alert.setOnCloseRequest(event -> Platform.exit());
                            alert.show();
                        } else if (i == 3) {
                            throw new RuntimeException("Файл не отправлен!");
                        } else {
                            throw new RuntimeException("Ошибка приложения!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
                Thread t = new Thread(r);
                t.start();

            }
        }
        String message = "";
        if(!tooBig.isEmpty()){
            message+="Размер файлов (";
            for(String str : tooBig)
                message+=str+", ";
            message=message.substring(0,message.length()-2);
            message+=") превышает допустимые значения!\n";
        }
        if(!badName.isEmpty()){
            message+="Имена файлов (";
            for(String str : badName)
                message+=str+", ";
            message=message.substring(0,message.length()-2);
            message+=") содержат недопустимые символы!\n";
        }
        if(!message.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        }
    }

    @FXML
    private void onKeyPressed(KeyEvent value){
        if(value.isShiftDown() && value.getCode() == KeyCode.ENTER){
            onSendClick();
        }
    }
}
