package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.*;
import java.util.*;

abstract class Session{
    InetAddress address;
    Socket socket;
    int port;
    Type type;
    Thread t;
    Timer broadcasting;
    static int BroadCastingPort = 9000;

    enum Type{
        TEST,
        MOUSE
    }

    static ArrayList<Session> sessions;

    static {
        sessions=new ArrayList<>(10);
    }

    public Session(){
        sessions.add(this);
    }

    public void Start(){
        t.start();
    }

    public void Stop() throws IOException {
        broadcasting.cancel();
        t.interrupt();
        if(socket!=null) socket.close();
    }

    public int getPort() {
        return port;
    }

    public Type getType() {
        return type;
    }

    public InetAddress getAddress() {
        return address;
    }

    public abstract boolean isServer();

}

class SessionServer extends Session{

    SessionServer(Type type) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        this.port = ss.getLocalPort();
        JSONObject message = new JSONObject();
        message.put("port", port);
        message.put("type", type);
        byte[] buf = String.format("%-30s", message.toJSONString()).getBytes();
        System.out.println(new String(buf));
        DatagramSocket s = new DatagramSocket();
        s.setBroadcast(true);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, Inet4Address.getByName("255.255.255.255"), BroadCastingPort);

        broadcasting = new Timer();
        broadcasting.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    s.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 10000);

        switch (type) {
            case TEST:
                t = new Thread(() -> {          //Сменить для каждого режима
                    try {
                        socket = ss.accept();
                        this.type = type;
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                        broadcasting.cancel();
                        while (true)
                            writer.println("test");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            default:
                throw new Exception("Неизвестный тип сессии");
        }
    }

    public boolean isServer(){
        return true;
    }
}

class SessionClient extends Session{

    static ArrayList<SessionClient> servers;
    static ArrayList<InetAddress> ips;
    static boolean isSearching;

    static{
        isSearching = false;
    }

    static void Search(Pane v, Thread onFinishSearching) throws SocketException {
        if(isSearching) {
            System.err.println("Поиск уже запущен");
            return;
        }
        servers = new ArrayList<>();
        ips = new ArrayList<>();
        isSearching = true;
        DatagramSocket s = new DatagramSocket(BroadCastingPort);
        s.setBroadcast(true);
        s.setSoTimeout(60000);
        byte[] buf = new byte[30];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        long time = (new Date()).getTime();
        Thread t = new Thread(() -> {
            try {
                while ((new Date()).getTime() - time <= 60000) {
                    s.receive(p);
                    JSONObject ans = (JSONObject) JSONValue.parse(new String(p.getData()));
                    if (ips.contains(p.getAddress())) {
                        ips.add(p.getAddress());
                        servers.add(new SessionClient(p.getAddress(), (int) ans.get("port"), (Type) ans.get("type")));
                        Platform.runLater(() -> {
                            v.getChildren().add(new TextField(p.getAddress().getHostAddress()));
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            isSearching = false;
            s.close();
            Platform.runLater(onFinishSearching);
        });
        t.start();
    }

    SessionClient(InetAddress address, int port, Type type) throws Exception {
        this.address = address;
        this.port = port;

        switch (type) {
            case TEST:
                t = new Thread(() -> {/*Сменить для каждого режима*/});
                break;
            default:
                throw new Exception("Неизвестный тип сессии");
        }

        Runnable r = () -> {
            try {
                socket = new Socket(address, port);
                socket.setSoTimeout(60000);
                this.type = type;
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                broadcasting.cancel();
                while (true)
                    System.out.println(reader.readLine());
            }catch (SocketException e){

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        t = new Thread(r);
    }

    public boolean isServer(){
        return false;
    }
}

public class Sessions {


    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox ConnectToSession;

    @FXML
    private Button NewSession;

    @FXML
    private Button SearchSessions;

    @FXML
    private ChoiceBox<String> SessionType;

    private ArrayList<Session> sessions;

    @FXML
    public void initialize(){
        SessionType.getItems().add("Эмуляция мыши");
        NewSession.setOnMouseClicked(this::OpenSession);
    }

    @FXML
    public void Search() throws SocketException {

        NewSession.setDisable(true);
        SessionType.setDisable(true);
        SearchSessions.setDisable(true);
        SessionClient.Search(ConnectToSession, new Thread(()->{
            NewSession.setDisable(false);
            SessionType.setDisable(false);
            SearchSessions.setDisable(false);
        }));
    }

    public void OpenSession(MouseEvent e){
        try{
            if (SessionType.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Выберите тип сеанса");
                alert.show();
            } else {
                ConnectToSession.setDisable(true);
                NewSession.setOnMouseClicked(this::CloseSession);
                NewSession.setText("Закрыть сессию");
                SessionServer test = new SessionServer(Session.Type.TEST);
                test.Start();
                //s.close();
            }
        } catch (IOException err){
            err.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void CloseSession(MouseEvent e) {
        try {
            ConnectToSession.setDisable(false);
            NewSession.setOnMouseClicked(this::OpenSession);
            NewSession.setText("Открыть сессию");
            Session.sessions.get(Session.sessions.size() - 1).Stop();
            Session.sessions.remove(Session.sessions.size() - 1);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void TouchPadSession(){
        /*
        Runnable run = () -> {
            ServerSocket s = null;
            try {
                System.out.println("OK");
                s = new ServerSocket(0);
                String qrCodeData = InetAddress.getLocalHost().getHostAddress() + ":" + s.getLocalPort();
                String charset = "UTF-8";
                Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                BitMatrix matrix = new MultiFormatWriter().encode(
                        new String(qrCodeData.getBytes(charset), charset),
                        BarcodeFormat.QR_CODE, 200, 200, hintMap);
                MatrixToImageWriter.toBufferedImage(matrix);
                QRCode.setImage(SwingFXUtils.toFXImage(MatrixToImageWriter.toBufferedImage(matrix), null));
                s.setSoTimeout(600000);
                Socket socket = s.accept();
                s.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Robot r = new Robot();
                int x = GetX();
                int y = GetY();
                int X = 0, Y = 0;
                String line = "null";
                while(socket != null && line != null) {
                    line = reader.readLine();
                    if (line != null) {
                        switch (line.split(" ")[0]) {
                            case "M":
                                if (Integer.parseInt(line.split(" ")[1]) == 0 && Integer.parseInt(line.split(" ")[2]) == 0) {
                                    x = GetX();
                                    y = GetY();
                                    X = 0;
                                    Y = 0;
                                } else {
                                    X = Integer.parseInt(line.split(" ")[1]);
                                    Y = Integer.parseInt(line.split(" ")[2]);
                                    r.mouseMove(x + X, y + Y);
                                }
                                break;
                            case "L":
                                r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                break;
                            case "L-":
                                r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                break;
                            case "L+":
                                r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                break;
                            case "R":
                                r.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                                r.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                                break;
                        }
                    }
                }
                socket.close();
            } catch (SocketTimeoutException e) {
                try {s.close();} catch (IOException e1) {e1.printStackTrace();}
            } catch (Exception e) {
                try {s.close();} catch (IOException e1) {e1.printStackTrace();}
                e.printStackTrace();
            }
        };
        Thread t = new Thread(run);
        t.start();
    }

    static public int GetX(){
        Point a = MouseInfo.getPointerInfo().getLocation();
        Point b = a.getLocation();
        return (int)b.getX();
    }

    static public int GetY(){
        Point a = MouseInfo.getPointerInfo().getLocation();
        Point b = a.getLocation();
        return (int)b.getY();*/
    }

}
