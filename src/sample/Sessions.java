package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.*;
import java.util.ResourceBundle;

public class Sessions {

    int port = 9000;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox ConnectToSession;

    @FXML
    private Button NewSession;

    @FXML
    private ChoiceBox<String> SessionType;

    @FXML
    public void initialize(){
        SessionType.getItems().add("Эмуляция мыши");
        NewSession.setOnMouseClicked(this::OpenSession);
    }

    @FXML
    public void Search(){
        try {
            byte buf[] = new byte[100];
            DatagramSocket s = new DatagramSocket(port);
            s.setBroadcast(true);
            DatagramPacket p = new DatagramPacket(buf, buf.length);
            while (s.isConnected()) {
                s.receive(p);
                System.out.println(new String(p.getData(),0, p.getLength()));
            }
            s.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
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
                DatagramSocket s = new DatagramSocket(port);
                s.setBroadcast(true);
                byte buf[] = new byte[100];
                for(int i = 0; i < SessionType.getValue().getBytes().length; i++){
                    buf[i] = SessionType.getValue().getBytes()[i];
                }
                DatagramPacket packet = new DatagramPacket(buf, buf.length, Inet4Address.getLocalHost(), port);
                s.send(packet);
                s.close();
            }
        } catch (IOException err){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(err.toString());
            alert.show();
            err.printStackTrace();
        }
    }

    public void CloseSession(MouseEvent e){
        ConnectToSession.setDisable(false);
        NewSession.setOnMouseClicked(this::OpenSession);
        NewSession.setText("Открыть сессию");
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
