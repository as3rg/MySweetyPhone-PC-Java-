package sample;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Sessions {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ImageView test;

    @FXML
    private Button TouchPad;

    @FXML
    public void initialize(){

    }

    private Socket socket;

    public void TouchPadSession(){
        TouchPad.setDisable(true);
        StackPane pane = new StackPane();
        ImageView imageView = new ImageView();

        pane.getChildren().add(imageView);
        Scene scene = new Scene(pane, 400, 400);
        Stage win = new Stage();
        win.setResizable(false);

        AnimationTimer hide=new AnimationTimer() {
            @Override
            public void handle(long now) {
                win.hide();
            }
        };
        AnimationTimer close=new AnimationTimer() {
            @Override
            public void handle(long now) {
                TouchPad.setDisable(false);
                win.close();
            }
        };

        Runnable run = () -> {
            ServerSocket s = null;
            try {
                s = new ServerSocket(0);
                String qrCodeData = InetAddress.getLocalHost().getHostAddress() + ":" + s.getLocalPort();
                String charset = "UTF-8";
                Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                BitMatrix matrix = new MultiFormatWriter().encode(
                        new String(qrCodeData.getBytes(charset), charset),
                        BarcodeFormat.QR_CODE, 200, 200, hintMap);
                MatrixToImageWriter.toBufferedImage(matrix);
                imageView.setImage(SwingFXUtils.toFXImage(MatrixToImageWriter.toBufferedImage(matrix), null));
                s.setSoTimeout(600000);
                Socket socket = s.accept();
                s.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                hide.start();
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
                close.start();
            } catch (SocketTimeoutException e) {
                try {s.close();} catch (IOException e1) {e1.printStackTrace();}
                close.start();
            } catch (Exception e) {
                try {s.close();} catch (IOException e1) {e1.printStackTrace();}
                e.printStackTrace();
                close.start();
            }
        };
        Thread t = new Thread(run);
        t.setDaemon(true);


        win.setOnShown(event -> {
            t.start();
        });
        win.setOnCloseRequest(event -> {
                    try {
                        if(this.socket != null) this.socket.close();
                        t.interrupt();
                        TouchPad.setDisable(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        win.setHeight(200);
        win.setWidth(200);
        win.setScene(scene);
        win.show();
    }

    static public int GetX(){
        Point a = MouseInfo.getPointerInfo().getLocation();
        Point b = a.getLocation();
        return (int)b.getX();
    }

    static public int GetY(){
        Point a = MouseInfo.getPointerInfo().getLocation();
        Point b = a.getLocation();
        return (int)b.getY();
    }

}
