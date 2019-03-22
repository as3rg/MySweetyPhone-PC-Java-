package sample;

import Utils.Session;
import Utils.SessionClient;
import Utils.SessionServer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


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
        SearchSessions.setOnMouseClicked(this::Search);
    }

    @FXML
    public void Search(MouseEvent mouseEvent) {
        try {
            NewSession.setDisable(true);
            SessionType.setDisable(true);
            SearchSessions.setOnMouseClicked(this::StopSearching);
            SearchSessions.setText("Остановить поиск");
            SessionClient.Search(ConnectToSession, new Thread(() -> {
                NewSession.setDisable(false);
                SessionType.setDisable(false);
                SearchSessions.setOnMouseClicked(this::Search);
                SearchSessions.setText("Поиск...");
            }));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void StopSearching(MouseEvent mouseEvent) {
        SearchSessions.setText("Поиск...");
        SearchSessions.setOnMouseClicked(this::Search);
        SessionClient.StopSearching();
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
                SearchSessions.setDisable(true);
                ConnectToSession.setDisable(true);
                NewSession.setOnMouseClicked(this::CloseSession);
                NewSession.setText("Закрыть сессию");
                SessionServer test = new SessionServer(Session.Type.values()[SessionType.getItems().indexOf(SessionType.getValue())]);
                test.Start();
            }
        } catch (IOException err){
            err.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void CloseSession(MouseEvent e) {
        try {
            SearchSessions.setDisable(false);
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
