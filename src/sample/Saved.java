package sample;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.http.HttpResponse;
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

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;


public class Saved {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox Messages;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextArea MessageText;

    private String name;
    private String login;
    private int id;
    private int regdate;

    @FXML
    void initialize() {
        LoadMore();
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

                URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=GetMessages&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id+"&From="+(Messages.getChildren().size()-1)+"&Count="+Count);

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
                if(i.equals(2L)){
                    throw new Exception("Ошибка приложения!");
                }else if(i.equals(1L)){
                    throw new Exception("Неверные данные");
                }else if(i.equals(0L)){
                    Object[] messages = ((JSONArray)result.get("messages")).toArray();
                    for(int j = 0; j < messages.length; j++){
                        JSONObject message = (JSONObject)messages[j];
                        DrawMessage(((String)(message).get("msg")).replace("\\n","\n"),(Long)(message).get("date"),(String)(message).get("sender"), ((String)(message).get("type")).equals("File"));
                    }
                }else if(i.equals(4L)){
                    Platform.runLater(() ->{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event -> Platform.exit());
                        alert.show();
                    });

                }else{
                    throw new Exception("Ошибка приложения!");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private void DrawMessage(String text, Long date, String sender, Boolean isFile) {
        DrawMessage(text, date, sender, isFile, false);
    }
    private void DrawMessage(String text, Long date, String sender, Boolean isFile, Boolean needsAnim) {
        Date Date = new java.util.Date(date * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd.MM.yyyy");
        VBox vBox = new VBox();
        Text TextLabel = new Text();
        Label DateLabel = new Label();
        vBox.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #d53369, #cbad6d); -fx-background-radius: 10;");
        vBox.setMaxWidth(460);
        DateLabel.setStyle("-fx-text-fill: #ffffff;");
        DateLabel.setText(sdf.format(Date) + ", " + sender);
        DateLabel.setPadding(new Insets(0, 10, 0, 10));
        TextLabel.setText(text);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        TextLabel.setFont(Font.font(15));
        vBox.getChildren().add(TextLabel);
        vBox.getChildren().add(DateLabel);
        if(isFile){
            File file = new File("src/sample/Images/Download.png");
            javafx.scene.image.Image image = new Image(file.toURI().toString());
            ImageView Download = new ImageView(image);
            Download.setFitWidth(150);
            Download.setFitHeight(150);
            //TextLabel.setMaxWidth(150);
            Download.setOnMouseClicked(event -> {
                DirectoryChooser fc = new DirectoryChooser();
                fc.setTitle("Выберите папку для сохранения");
                final File out = fc.showDialog(null);
                if (file == null) return;
                Runnable r = () -> {
                    try {
                        URL website = new URL("http://mysweetyphone.herokuapp.com/?Type=DownloadFile&RegDate="+regdate+"&MyName=" + name + "&Login=" + login + "&Id=" + id + "&FileName=" + text + "&Date=" + date);
                        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                        File out2 = new File(out,"\\MySweetyPhone\\");
                        out2.mkdirs();
                        FileOutputStream fos = new FileOutputStream(out2.getPath()+'\\'+text);
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                Thread t = new Thread(r);
                t.start();
            });
            vBox.getChildren().add(0,Download);
        }
        VBox.setMargin(vBox, new Insets(5, 0, 0, 0));

        final ContextMenu contextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Удалить");
        contextMenu.getItems().addAll(delete);
        delete.setOnAction(event -> {
            EventHandler r = (event1) -> {
                try {
                    URL obj = new URL("https://mysweetyphone.herokuapp.com/?Type=DelMessage&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id+"&Date="+date+"&Msg="+text.replace(" ","%20").replace("\n","\\n"));

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
                    if(i.equals(2L)){
                        throw new Exception("Ошибка приложения!");
                    }else if(i.equals(1L)){
                        throw new Exception("Неверные данные");
                    }else if(i.equals(0L)){
                    }else if(i.equals(4L)){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event2 -> Platform.exit());
                        alert.show();
                    }else{
                        throw new Exception("Ошибка приложения!");
                    }
                    Messages.getChildren().remove(vBox);
                    if(Messages.getChildren().size() < 10)
                        LoadMore(10 - Messages.getChildren().size());
                }catch (Exception e){
                    e.printStackTrace();
                }
            };
            Destroy anim = new Destroy(vBox);
            anim.play(r);
        });
        vBox.setOnContextMenuRequested((EventHandler<Event>) event -> contextMenu.show(vBox, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y));
        if(needsAnim)
            Platform.runLater(() -> {
                Messages.getChildren().add(vBox);
            });
        else
            Platform.runLater(() -> {
                Messages.getChildren().add(1, vBox);
            });
        if (needsAnim) {
            Create anim = new Create(vBox);
            anim.play();
        }
    }

    @FXML
    private void onSendClick(){
        Runnable r = () -> {
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
                Long i = (Long) result.getOrDefault("code", 2);
                if(i.equals(2L)){
                    throw new Exception("Ошибка приложения!");
                }else if(i.equals(1L)){
                    throw new Exception("Неверные данные");
                }else if(i.equals(0L)){
                    DrawMessage(MessageText.getText(), (Long) result.getOrDefault("time", 2), name, false, true);
                }else if(i.equals(4L)){
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event -> Platform.exit());
                        alert.show();
                    });
                }else{
                    throw new Exception("Ошибка приложения!");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();
        MessageText.setText("");
    }

    @FXML
    private void onFileClicked(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите файл для отправки");
        File file = fc.showOpenDialog(null);
        if (file == null) return;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        if (file.length() > 1*1024*1024){
            alert.setContentText("Размер файла превышает допустимые размеры");
            alert.show();
            return;
        }
        if(!Charset.forName("US-ASCII").newEncoder().canEncode(file.getName())){
            alert.setContentText("Имя файла содержит недопустимые символы");
            alert.show();
            return;
        }
        if (file != null){
            Runnable r = () -> {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost("http://mysweetyphone.herokuapp.com/?Type=UploadFile&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id);
                    MultipartEntity entity = new MultipartEntity();
                    entity.addPart("fileToUpload", new FileBody(file));
                    entity.addPart("submit", new StringBody(""));
                    post.setEntity(entity);
                    HttpResponse response = client.execute(post);
                    JSONObject result = (JSONObject) JSONValue.parse(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    Long i = (Long) result.getOrDefault("code", 2);
                    if(i.equals(2L)){
                        throw new Exception("Ошибка приложения!");
                    }else if(i.equals(1L)){
                        throw new Exception("Неверные данные");
                    }else if(i.equals(0L)){
                        DrawMessage(file.getName(), (Long) result.getOrDefault("time", 2), name, true, true);
                    }else if(i.equals(4L)){
                        alert.setContentText("Ваше устройство не зарегистрировано!");
                        alert.setOnCloseRequest(event -> Platform.exit());
                        alert.show();
                    }else if(i.equals(3L)){
                        throw new Exception("Файл не отправлен!");
                    }else{
                        throw new Exception("Ошибка приложения!");
                    }
                }catch (Exception e){
                    alert.setContentText(e.toString());
                    alert.setOnCloseRequest(event -> Platform.exit());
                    alert.show();
                    e.printStackTrace();
                }
            };
            Thread t = new Thread(r);
            t.start();

        }
    }
}
