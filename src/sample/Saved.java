package sample;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import sample.Anims.Destroy;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class Saved {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox Messages;

    @FXML
    private TextArea MessageText;

    private String name;
    private String login;
    private int id;

    @FXML
    void initialize() {
        Runnable r = () -> {
            try {
                FileInputStream propFile = new FileInputStream(location.getPath() + "/../../properties.properties");
                Properties props = new Properties();
                props.load(propFile);
                propFile.close();
                id = Integer.parseInt((String) props.getOrDefault("id", "-1"));
                login = (String) props.getOrDefault("login", "");
                name = (String) props.getOrDefault("name", "");

                URL url = new URL("http://mysweetyphone.herokuapp.com/");
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("Type", "GetMessages");
                params.put("MyName", name);
                params.put("Login", login);
                params.put("Id", id);

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                String responce = "";
                for (int c; (c = in.read()) >= 0; )
                    responce += (char) c;
                JSONObject result = (JSONObject) JSONValue.parse(responce);
                Long i = (Long) result.getOrDefault("code", 2);
                if(i.equals(2L)){
                    throw new Exception("Ошибка приложения!");
                }else if(i.equals(1L)){
                    throw new Exception("Неверные данные");
                }else if(i.equals(0L)){
                    for(Object message : (JSONArray)result.get("messages")){
                        DrawMessage((String)((JSONObject)message).get("msg"),(Long)((JSONObject)message).get("date"),(String)((JSONObject)message).get("sender"), (String)((JSONObject)message).get("type"));
                    }
                }else{
                    throw new Exception("Ошибка приложения!");
                }
            }catch (Exception e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText(e.toString());
                alert.show();
            }
        };
        Thread t = new Thread(r);
        t.run();
    }

    private void DrawMessage(String text, Long date, String sender, String type) {
        if (type.equals("Text")) {
            Date Date = new java.util.Date(date * 1000L);
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd.MM.yyyy");
            VBox vBox = new VBox();
            Label TextLabel = new Label();
            Label DateLabel = new Label();
            vBox.setStyle("-fx-background-color: #1e90ff; -fx-background-radius: 10;");
            DateLabel.setText(sdf.format(Date) + ", " + sender);
            DateLabel.setTextFill(Color.LIGHTGRAY);
            DateLabel.setPadding(new Insets(0, 10, 0, 10));
            TextLabel.setText(text);
            TextLabel.setMaxWidth(460);
            TextLabel.setMinWidth(DateLabel.getPrefWidth());
            TextLabel.setPadding(new Insets(10, 10, 10, 10));
            TextLabel.setAlignment(Pos.CENTER_LEFT);
            TextLabel.setWrapText(true);
            vBox.getChildren().add(TextLabel);
            vBox.getChildren().add(DateLabel);
            VBox.setMargin(vBox, new Insets(5, 0, 0, 0));

            final ContextMenu contextMenu = new ContextMenu();
            MenuItem delete = new MenuItem("Удалить");
            contextMenu.getItems().addAll(delete);
            delete.setOnAction(event -> {
                Runnable r = () -> {
                    try {
                        URL url = new URL("http://mysweetyphone.herokuapp.com/");
                        Map<String, Object> params = new LinkedHashMap<>();
                        params.put("Type", "DelMessage");
                        params.put("MyName", name);
                        params.put("Login", login);
                        params.put("Id", id);
                        params.put("Msg", text);
                        params.put("Date", date);

                        StringBuilder postData = new StringBuilder();
                        for (Map.Entry<String, Object> param : params.entrySet()) {
                            if (postData.length() != 0) postData.append('&');
                            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                            postData.append('=');
                            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                        }
                        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                        conn.setDoOutput(true);
                        conn.getOutputStream().write(postDataBytes);

                        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                        String responce = "";
                        for (int c; (c = in.read()) >= 0; )
                            responce += (char) c;
                        JSONObject result = (JSONObject) JSONValue.parse(responce);
                        Long i = (Long) result.getOrDefault("code", 2);
                        if(i.equals(2L)){
                            throw new Exception("Ошибка приложения!");
                        }else if(i.equals(1L)){
                            throw new Exception("Неверные данные");
                        }else if(i.equals(0L)){
                        }else{
                            throw new Exception("Ошибка приложения!");
                        }
                        Destroy anim = new Destroy(vBox, Messages);
                        anim.play();
                    }catch (Exception e){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText(e.toString());
                        alert.show();
                    }
                };
                Thread t = new Thread(r);
                t.run();
            });

            vBox.setOnContextMenuRequested((EventHandler<Event>) event -> contextMenu.show(vBox, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y));

            Messages.getChildren().add(vBox);
        }
    }

    @FXML
    private void onSendClick(){
        Long time = Calendar.getInstance().getTimeInMillis()/1000;
        Runnable r = () -> {
            try {
                URL url = new URL("http://mysweetyphone.herokuapp.com/");
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("Type", "SendMessage");
                params.put("MyName", name);
                params.put("Login", login);
                params.put("Id", id);
                params.put("Msg", MessageText.getText());
                params.put("MsgType", "Text");
                params.put("Time", time);

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                String responce = "";
                for (int c; (c = in.read()) >= 0; )
                    responce += (char) c;
                JSONObject result = (JSONObject) JSONValue.parse(responce);
                Long i = (Long) result.getOrDefault("code", 2);
                if(i.equals(2L)){
                    throw new Exception("Ошибка приложения!");
                }else if(i.equals(1L)){
                    throw new Exception("Неверные данные");
                }else if(i.equals(0L)){
                }else{
                    throw new Exception("Ошибка приложения!");
                }
            }catch (Exception e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText(e.toString());
                alert.show();
            }
        };
        Thread t = new Thread(r);
        t.run();
        DrawMessage(MessageText.getText(), time, name, "Text");
        MessageText.setText("");
    }
}
