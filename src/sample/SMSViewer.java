package sample;

import Utils.Request;
import Utils.SessionClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.http.entity.mime.MultipartEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import sample.Anims.Create;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SMSViewer {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button SendButton1;

    @FXML
    private Button SendButton2;

    @FXML
    private FlowPane SendBar;

    @FXML
    private VBox Messages;

    @FXML
    private ListView<String> Contacts;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private BorderPane MainPane;

    @FXML
    private BorderPane MainPane2;

    @FXML
    private TextArea MessageText;

    @FXML
    private AnchorPane RootPane;

    private String name;
    private String login;
    private int id;
    private int regdate;
    Thread receiving;
    PrintWriter writer;
    BufferedReader reader;
    SessionClient sc;
    Stage stage;
    static public Stack<Pair<SessionClient, Stage>> sessionClients;

    static {
        sessionClients = new Stack<>();
    }

    {
        Pair<SessionClient, Stage> p = sessionClients.pop();
        sc = p.getKey();
        stage = p.getValue();

    }

    @FXML
    void initialize() throws IOException {
        Thread Resize = new Thread(()->{
            try {
                while (Messages.getScene() == null) Thread.sleep(100);
                scrollPane.prefWidthProperty().bind(stage.widthProperty());
                Contacts.prefHeightProperty().bind(stage.heightProperty());
                Contacts.prefWidthProperty().bind(stage.widthProperty().divide(5));
                MessageText.prefWidthProperty().bind(stage.widthProperty().divide(10).multiply(8).subtract(50).subtract(Contacts.widthProperty()));
                SendButton1.prefWidthProperty().bind(stage.widthProperty().divide(10));
                SendButton2.prefWidthProperty().bind(stage.widthProperty().divide(10));
                SendBar.prefWidthProperty().bind(stage.widthProperty());
                MainPane.prefWidthProperty().bind(stage.widthProperty());
                MainPane.prefHeightProperty().bind(stage.heightProperty().subtract(40));
                Messages.setFillWidth(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Resize.start();
        Messages.setFillWidth(false);

        File file = new File("properties.properties");
        FileInputStream propFile = new FileInputStream(file);
        Properties props = new Properties();
        props.load(propFile);
        propFile.close();
        name = (String) props.getOrDefault("name", "");

        Contacts.setOnMouseClicked(event -> new Thread(()->{
            if(Contacts.getItems().isEmpty()) return;
            JSONObject msg = new JSONObject();
            msg.put("Type", "showSMSs");
            msg.put("Name", name);
            Pattern r = Pattern.compile(".*\\((.+)\\)");
            Matcher m = r.matcher(Contacts.getSelectionModel().getSelectedItem());
            if(m.find())
                msg.put("Number", m.group(1));
            else
                msg.put("Number", Contacts.getSelectionModel().getSelectedItem());
            System.out.println(msg.toJSONString());
            writer.println(msg.toJSONString());
            writer.flush();
        }).start());

        receiving = new Thread(()-> {
            try {
                writer = new PrintWriter(sc.getSocket().getOutputStream());
                reader = new BufferedReader(new InputStreamReader(sc.getSocket().getInputStream()));
                JSONObject msg2 = new JSONObject();
                msg2.put("Type", "start");
                msg2.put("Name", name);
                writer.println(msg2.toJSONString());
                writer.flush();
                SimpleStringProperty Sim1 = new SimpleStringProperty(""), Sim2 = new SimpleStringProperty("");
                while (true) {
                    String line = reader.readLine();
                    if(line == null){
                        sc.Stop();
                        Platform.runLater(()-> stage.close());
                        break;
                    }
                    JSONObject msg = (JSONObject) JSONValue.parse(line);
                    switch ((String) msg.get("Type")) {
                        case "start":
                            Sim1.set((String) msg.getOrDefault("Sim1", "Sim1"));
                            Sim2.set((String) msg.getOrDefault("Sim2", "Sim2"));
                            Platform.runLater(()->{
                                if(!msg.containsKey("Sim1") && !msg.containsKey("Sim2")){
                                    MainPane2.getChildren().remove(SendBar);
                                }else if(!msg.containsKey("Sim1")){
                                    SendBar.getChildren().remove(SendButton1);
                                    SendButton2.prefWidthProperty().bind(stage.widthProperty().divide(5));
                                    SendButton2.setText(Sim2.get());
                                }else if(!msg.containsKey("Sim12")){
                                    SendBar.getChildren().remove(SendButton2);
                                    SendButton1.prefWidthProperty().bind(stage.widthProperty().divide(5));
                                    SendButton1.setText(Sim1.get());
                                }else {
                                    SendButton1.setText(Sim1.get());
                                    SendButton2.setText(Sim2.get());
                                }
                            });
                            break;
                        case "accepted":
                            writer.println(msg2.toJSONString());
                            writer.flush();
                            break;
                        case "getContacts":
                            JSONArray values = (JSONArray) msg.get("Contacts");
                            Platform.runLater(() -> {
                                Contacts.getItems().clear();
                                for (int i = 0; i < values.size(); i++) {
                                    String name = (String)values.get(i);
                                    if(!Contacts.getItems().contains(name)) Contacts.getItems().add(name);
                                }
                            });
                            break;
                        case "showSMSs":
                            values = (JSONArray) msg.get("SMS");
                            Platform.runLater(() -> {
                                Messages.getChildren().clear();
                                for (int i = 0; i < values.size(); i++) {
                                    JSONObject message = (JSONObject)values.get(i);
                                    DrawText((String)message.get("text"), ((Long)message.get("date")).longValue(), false, ((Long)message.get("type")).intValue(), ((Long)message.get("sim")).intValue() == 1 ? Sim1.get() : Sim2.get());
                                }
                            });
                            break;
                        case "newSMSs":
                            values = (JSONArray) msg.get("SMS");
                            Platform.runLater(() -> {
                                for (int i = 0; i < values.size(); i++) {
                                    JSONObject message = (JSONObject)values.get(i);
                                    DrawText((String)message.get("text"), ((Long)message.get("date")).longValue(), true, ((Long)message.get("type")).intValue(), ((Long)message.get("sim")).intValue() == 1 ? Sim1.get() : Sim2.get());
                                }
                            });
                            break;
                        case "finish":
                            sc.Stop();
                            Platform.runLater(()->stage.close());
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiving.start();

        stage.setOnCloseRequest(e ->{
            receiving.interrupt();
            new Thread(() -> {
                JSONObject msg2 = new JSONObject();
                msg2.put("Type", "finish");
                msg2.put("Name", name);
                writer.println(msg2.toJSONString());
                writer.flush();
            }).start();
        });
    }

    private void DrawText(String text, long date, Boolean needsAnim, int type, String sender) {
        Date Date = new Date(date * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        VBox vBox = new VBox();
        Label textLabel = new Label();
        Label DateLabel = new Label();
        vBox.maxWidthProperty().bind(Messages.widthProperty().divide(2));
        DateLabel.setStyle("-fx-text-fill: #ffffff;");
        DateLabel.setText(sdf.format(Date)+", "+sender);
        DateLabel.setPadding(new Insets(0, 10, 0, 10));
        textLabel.setText(text);
        textLabel.setFont(Font.font(15));
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-text-fill: #ffffff; -fx-wrap-text: true;");
        if(text.isEmpty()){
            textLabel.setText("Пустое сообщение");
            textLabel.setStyle("-fx-text-fill: grey;");
            textLabel.setFont(Font.font(textLabel.getFont().getFamily(), FontPosture.ITALIC, textLabel.getFont().getSize()));
        }
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(textLabel);
        vBox.getChildren().add(DateLabel);
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 10,0,10));
        hBox.getChildren().add(vBox);
        if(type == 1){
            vBox.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #d53369, #cbad6d); -fx-background-radius: 10;");
            hBox.setAlignment(Pos.CENTER_LEFT);
        }else if(type == 2){
            vBox.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #CF8BF3, #FDB99B); -fx-background-radius: 10;");
            hBox.setAlignment(Pos.CENTER_RIGHT);
        }else if(type == 5){
            vBox.setStyle("-fx-background-color: #FF0000; -fx-background-radius: 10;");
            hBox.setAlignment(Pos.CENTER_RIGHT);
        }else return;
        Platform.runLater(() -> {
            Messages.getChildren().add(hBox);
            if (needsAnim) {
                Create anim = new Create(vBox);
                anim.play();
                scrollPane.vvalueProperty().bind(vBox.heightProperty().divide(vBox.heightProperty()));
            }
        });
    }

    @FXML
    private void onSendClick1(){
        onSendClick(1);
    }

    @FXML
    private void onSendClick2(){
        onSendClick(2);
    }



    @FXML
    private void onSendClick(int i){
        new Thread(() -> {
            JSONObject msg2 = new JSONObject();
            msg2.put("Type", "sendSMS");
            msg2.put("Number", Contacts.getSelectionModel().getSelectedItem());
            msg2.put("Text",MessageText.getText());
            msg2.put("Name", name);
            msg2.put("Sim", i);
            writer.println(msg2.toJSONString());
            writer.flush();
            Platform.runLater(()->MessageText.setText(""));
        }).start();
    }
}
