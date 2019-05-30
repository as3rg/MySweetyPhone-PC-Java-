package Utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import sample.FileViewer;
import sample.Main;
import sample.SMSViewer;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class SessionClient extends Session{

    static private class Server{
        public int value;
        public Button b;
        SessionClient sc;
        Server(Button b, SessionClient sc){
            this.b = b;
            this.sc = sc;
            value = 5;
        }
    }

    static Map<String, Server> ips;
    static boolean isSearching;
    static Thread searching;
    static DatagramSocket s;

    static{
        isSearching = false;
    }

    public static void Search(Pane v, Thread onFinishSearching) throws SocketException {
        v.getChildren().clear();
        if(isSearching) {
            StopSearching();
        }
        ips = new TreeMap<>();
        isSearching = true;
        s = new DatagramSocket(BroadCastingPort);
        s.setBroadcast(true);
        s.setSoTimeout(60000);
        byte[] buf = new byte[100];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        long time = System.currentTimeMillis();
        Timer t = new Timer();
        searching = new Thread(() -> {
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    for (String item : ips.keySet()) {
                        if (ips.get(item).value == 1) {
                            Button b = ips.get(item).b;
                            Platform.runLater(() -> v.getChildren().remove(b));
                            ips.remove(item);
                        }else
                            ips.get(item).value--;
                    }
                }
            }, 0, 2000);
            try {
                while (System.currentTimeMillis() - time <= 60000) {
                    s.receive(p);
                    JSONObject ans = (JSONObject) JSONValue.parse(new String(p.getData()));
                    String name = ans.get("name") + "(" + p.getAddress().getHostAddress() + "): " + decodeType(((Long)ans.get("type")).intValue());
                    if (!ips.containsKey(name)) {
                        Server s = new Server(null, new SessionClient(p.getAddress(),((Long)ans.get("port")).intValue(), ((Long)ans.get("type")).intValue()));
                        ips.put(name,s);
                        Platform.runLater(() -> {
                            Button ip = new Button(name);
                            s.b = ip;
                            ip.setTextFill(Paint.valueOf("#F0F0F0"));
                            ip.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #CF8BF3, #FDB99B); -fx-background-radius: 0;");
                            VBox.setMargin(ip, new Insets(5, 10, 5,0));
                            ip.prefWidthProperty().bind(v.widthProperty());
                            ip.setOnMouseClicked(event->{
                                s.sc.Start();
                                v.getChildren().remove(ip);
                            });
                            v.setDisable(false);
                            v.getChildren().add(ip);
                        });
                    }else
                        ips.get(name).value=5;
                }
            } catch (SocketException ignored){
            } catch (IOException e) {
                e.printStackTrace();
            }
            isSearching = false;
            s.close();
            t.cancel();
            Platform.runLater(onFinishSearching);
        });
        searching.start();
    }

    public static void StopSearching() {
        searching.interrupt();
        isSearching=false;
        s.close();
    }

    public SessionClient(InetAddress address, int Port, int type) throws IOException {
        this.address = address;
        this.port = Port;
        this.type = type;

        File file = new File("properties.properties");
        FileInputStream propFile = new FileInputStream(file);
        Properties props = new Properties();
        props.load(propFile);
        propFile.close();
        String name = (String) props.getOrDefault("name", "");

        switch (type) {
            case MOUSE:
                t = new Thread(() -> {
                    try {
                        Dsocket = new DatagramSocket();
                        Dsocket.setBroadcast(true);
                        if(searching != null) StopSearching();
                        MouseTracker mt = new MouseTracker(this, name);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case FILEVIEW:
                t = new Thread(()->{
                    try {
                        if (searching != null) StopSearching();
                        Ssocket = new Socket(address, port);
                        Platform.runLater(() -> {
                            try {
                                Stage stage = new Stage();
                                FileViewer.sessionClients.push(new Pair<>(this, stage));
                                FXMLLoader loader = new FXMLLoader();
                                loader.setLocation(new File(new File("src", "sample"), "FileViewer.fxml").toURL());
                                BorderPane pane = loader.load();
                                stage.setMinHeight(760);
                                stage.setMinWidth(500);
                                Scene scene = new Scene(pane, 1270, 720);
                                scene.getStylesheets().add(Main.class.getResource("Style.css").toExternalForm());
                                stage.setScene(scene);
                                stage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (ConnectException e){
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Ошибка");
                            alert.setHeaderText(null);
                            alert.setContentText("Сессия закрыта");
                            alert.show();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case SMSVIEW:
                t = new Thread(()->{
                    try {
                        if (searching != null) StopSearching();
                        Ssocket = new Socket(address, port);
                        Platform.runLater(() -> {
                            try {
                                Stage stage = new Stage();
                                SMSViewer.sessionClients.push(new Pair<>(this, stage));
                                FXMLLoader loader = new FXMLLoader();
                                loader.setLocation(new File(new File("src", "sample"), "SMSViewer.fxml").toURL());
                                AnchorPane pane = loader.load();
                                stage.setMinHeight(760);
                                stage.setMinWidth(500);
                                Scene scene = new Scene(pane, 1270, 720);
                                scene.getStylesheets().add(Main.class.getResource("Style.css").toExternalForm());
                                stage.setScene(scene);
                                stage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (ConnectException e){
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Ошибка");
                            alert.setHeaderText(null);
                            alert.setContentText("Сессия закрыта");
                            alert.show();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            default:
                throw new RuntimeException("Неизвестный тип сессии");
        }
    }

    public boolean isServer(){
        return false;
    }

}