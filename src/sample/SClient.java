package sample;

import Utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class SClient {

    @FXML
    private ScrollPane ConnectToSessionScrollPane;

    @FXML
    private AnchorPane MainPane;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox ConnectToSession;

    @FXML
    private Button SearchSessions;

    @FXML
    private VBox ConnectToSessionMainPane;

    private ArrayList<Session> sessions;

    @FXML
    public void initialize(){
        Thread Resize = new Thread(()->{
            try {
                while (MainPane.getScene() == null) Thread.sleep(100);
                MainPane.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty());
                MainPane.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty());
                ConnectToSessionMainPane.prefHeightProperty().bind(MainPane.heightProperty());
                ConnectToSessionMainPane.prefWidthProperty().bind(MainPane.widthProperty());
                ConnectToSessionScrollPane.prefHeightProperty().bind(MainPane.prefHeightProperty().subtract(SearchSessions.heightProperty().subtract(10)));
                ConnectToSession.minHeightProperty().bind(ConnectToSessionScrollPane.heightProperty());
                MainPane.prefWidthProperty().bind(MainPane.getScene().widthProperty());
                MainPane.prefHeightProperty().bind(MainPane.getScene().heightProperty());
                SearchSessions.setDisable(false);
                ConnectToSession.setDisable(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Resize.start();
        SearchSessions.setOnMouseClicked(this::Search);
    }

    @FXML
    public void Search(MouseEvent mouseEvent) {
        try {
            SearchSessions.setOnMouseClicked(this::StopSearching);
            SearchSessions.setText("Остановить поиск");
            Utils.SessionClient.Search(ConnectToSession, new Thread(() -> {
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
        Utils.SessionClient.StopSearching();
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
