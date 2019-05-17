package Utils;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class SessionClient extends Session{

    static private class Server{
        public int value;
        public Button b;
        Server(Button b){
            this.b = b;
            value = 5;
        }
    }

    static ArrayList<SessionClient> servers;
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
            System.err.println("Поиск уже запущен");
            return;
        }
        servers = new ArrayList<>();
        ips = new TreeMap<>();
        isSearching = true;
        s = new DatagramSocket(BroadCastingPort);
        s.setBroadcast(true);
        s.setSoTimeout(60000);
        byte[] buf = new byte[30];
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
                    if (!ips.containsKey(p.getAddress().getHostAddress())) {
                        servers.add(new SessionClient(p.getAddress(),((Long)ans.get("port")).intValue(), Type.values()[((Long)ans.get("type")).intValue()]));
                        Server s = new Server(null);
                        ips.put(p.getAddress().getHostAddress(),s);
                        Platform.runLater(() -> {
                            Button ip = new Button(p.getAddress().getHostAddress());
                            s.b = ip;
                            ip.setTextFill(Paint.valueOf("#F0F0F0"));
                            ip.setOnMouseClicked(event->{
                                servers.get(v.getChildren().indexOf(ip)).Start();
                                v.getChildren().remove(ip);
                            });
                            v.setDisable(false);
                            v.getChildren().add(ip);
                        });
                    }else
                        ips.get(p.getAddress().getHostAddress()).value=5;
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

    public SessionClient(InetAddress address, int Port, Type type) throws IOException {
        this.address = address;
        this.port = Port;
        this.type = type;
        switch (type){
            case MOUSE:
                Dsocket = new DatagramSocket();
                Dsocket.setBroadcast(true);
                break;
            case FILEVIEW:
                Ssocket = new Socket(address, port);
        }

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
                        if(searching != null) StopSearching();
                        MouseTracker mt = new MouseTracker(this, name);
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