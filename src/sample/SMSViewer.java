package sample;

import Utils.SessionClient;
import javafx.application.Platform;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import sample.Anims.Create;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private Button SendButton;

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
                SendButton.prefWidthProperty().bind(stage.widthProperty().divide(5));
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
            JSONObject msg = new JSONObject();
            msg.put("Type", "showSMS");
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
                while (true) {
                    String line = reader.readLine();
                    JSONObject msg = (JSONObject) JSONValue.parse(line.strip());
                    switch ((String) msg.get("Type")) {
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
                        case "showSMS":
                            values = (JSONArray) msg.get("SMS");
                            Platform.runLater(() -> {
                                Messages.getChildren().clear();
                                for (int i = 0; i < values.size(); i++) {
                                    JSONObject message = (JSONObject)values.get(i);
                                    DrawText((String)message.get("text"), ((Long)message.get("date")).longValue(), false, ((Long)message.get("type")).intValue());
                                }
                            });
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

    private void DrawText(String text, long date, Boolean needsAnim, int type) {
        Date Date = new Date(date * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        VBox vBox = new VBox();
        Label textLabel = new Label();
        Label DateLabel = new Label();
        vBox.maxWidthProperty().bind(Messages.widthProperty().divide(2));
        DateLabel.setStyle("-fx-text-fill: #ffffff;");
        DateLabel.setText(sdf.format(Date));
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
        }else {
            System.out.println(type);
        }

//        final ContextMenu contextMenu = new ContextMenu();
//        MenuItem delete = new MenuItem("Удалить");
//        contextMenu.getItems().addAll(delete);
//        delete.setOnAction(event -> {
//            EventHandler r = (event1) -> {
//                try {
//                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=DelMessage&RegDate="+regdate+"&MyName="+name+"&Login="+login+"&Id="+id+"&Date="+date+"&Msg="+text.replace(" ","%20").replace("\n","\\n"));
//
//                    HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
//                    connection.setRequestMethod("GET");
//
//                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                    String inputLine;
//                    StringBuffer response = new StringBuffer();
//
//                    while ((inputLine = in.readLine()) != null) {
//                        response.append(inputLine);
//                    }
//                    in.close();
//
//                    JSONObject result = (JSONObject) JSONValue.parse(response.toString().strip());
//                    int i = ((Long) result.getOrDefault("code", 2)).intValue();
//                    if(i == 2){
//                        throw new RuntimeException("Ошибка приложения!");
//                    }else if(i == 1){
//                        throw new RuntimeException("Неверные данные");
//                    }else if(i == 0){
//                    }else if(i == 4){
//                        Alert alert = new Alert(Alert.AlertType.ERROR);
//                        alert.setTitle("Ошибка");
//                        alert.setHeaderText(null);
//                        alert.setContentText("Ваше устройство не зарегистрировано!");
//                        alert.setOnCloseRequest(event2 -> Platform.exit());
//                        alert.show();
//                    }else{
//                        throw new RuntimeException("Ошибка приложения!");
//                    }
//                    Messages.getChildren().remove(vBox);
//                    if(Messages.getChildren().size() < 10)
//                        LoadMore(10 - Messages.getChildren().size() + 1);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            };
//            Destroy anim = new Destroy(vBox);
//            anim.play(r);
//        });
//        vBox.setOnContextMenuRequested((EventHandler<Event>) event -> contextMenu.show(vBox, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y));
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

                JSONObject result = (JSONObject) JSONValue.parse(response.toString().strip());
                int i = ((Long) result.getOrDefault("code", 2)).intValue();
                if(i == 2){
                    throw new RuntimeException("Ошибка приложения!");
                }else if(i == 1){
                    throw new RuntimeException("Неверные данные");
                }else if(i == 0){
                    DrawText(MessageText.getText(), (Integer) result.getOrDefault("time", 2), false, 2);
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
    private void onKeyPressed(KeyEvent value){
        if(value.isShiftDown() && value.getCode() == KeyCode.ENTER){
            onSendClick();
        }
    }
}
