package Utils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;

public class MouseTracker{

    SessionClient sc;
    double width;
    double height;
    Stage s;
    String name;
    static final int MESSAGESIZE = 100;
    public MouseTracker(SessionClient sc, String name) throws IOException {
        this.sc = sc;
        this.name = name;
        Platform.runLater(()-> {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            width = screenSize.getWidth();
            height = screenSize.getHeight();
            s = new Stage();

            if(sc.isPhone){
                StackPane p = new StackPane();
                TextArea textArea = new TextArea();
                textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                    new Thread(()->{
                        try {
                            JSONObject msg = new JSONObject();
                            msg.put("Type", "keysTyped");
                            msg.put("value", newValue);
                            msg.put("Name", name);
                            Send(msg.toJSONString().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    textArea.setText("");
                });
                textArea.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);
                textArea.addEventFilter(KeyEvent.KEY_RELEASED, this::keyReleased);
                textArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        textArea.requestFocus();
                    }
                });
                textArea.setBackground(new Background(new BackgroundFill(Paint.valueOf("#202020"), CornerRadii.EMPTY, Insets.EMPTY)));
                textArea.setStyle("-fx-background-color: #202020;");
                p.setBackground(new Background(new BackgroundFill(Paint.valueOf("#202020"), CornerRadii.EMPTY, Insets.EMPTY)));
                p.setStyle("-fx-background-color: #202020;");
                p.getChildren().add(textArea);
                s.setScene(new Scene(p, 600, 600));
            }else {
                BorderPane p = new BorderPane();
                p.addEventFilter(MouseEvent.MOUSE_MOVED, this::mouseMoved);
                p.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::mouseMoved);
                p.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressed);
                p.addEventFilter(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
                p.addEventFilter(ScrollEvent.SCROLL, this::mouseWheelMoved);
                p.addEventFilter(DragEvent.DRAG_DROPPED, this::dragDropped);
                p.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);
                p.addEventFilter(KeyEvent.KEY_RELEASED, this::keyReleased);
                p.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    try {
                        if (!isNowFocused) {
                            JSONObject msg = new JSONObject();
                            msg.put("Type", "keyReleased");
                            msg.put("Name", name);
                            msg.put("value", KeyCode.ALT.getCode());
                            Send(msg.toJSONString().getBytes());
                            msg.put("value", KeyCode.SHIFT.getCode());
                            Send(msg.toJSONString().getBytes());
                            msg.put("value", KeyCode.CONTROL.getCode());
                            Send(msg.toJSONString().getBytes());
                            msg.put("value", KeyCode.WINDOWS.getCode());
                            Send(msg.toJSONString().getBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                p.requestFocus();
                p.setStyle("-fx-background-color: #202020;");
                Label label = new Label("Для выхода нажмите Ctrl+F4");
                p.setCenter(label);
                s.setScene(new Scene(p, 600, 600));
            }
            s.setMaximized(true);
            s.initStyle(StageStyle.UNDECORATED);
            s.setResizable(false);
            s.setOnCloseRequest((e) -> {
                try {
                    JSONObject msg = new JSONObject();
                    msg.put("Type", "finish");
                    msg.put("Name", name);
                    Message[] messages = Message.getMessages(msg.toJSONString().getBytes(), MESSAGESIZE);
                    for (Message m : messages) {
                        sc.getDatagramSocket().send(new DatagramPacket(m.getArr(), m.getArr().length, sc.getAddress(), sc.getPort()));
                    }
                    s.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            s.show();
        });
        JSONObject msg = new JSONObject();
        msg.put("Type", "start");
        msg.put("Name", name);
        Send(msg.toJSONString().getBytes());
    }

    public void mousePressed(MouseEvent e) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("Type","mousePressed");
            msg.put("Key",e.getButton().ordinal());
            msg.put("Name", name);
            Send(msg.toJSONString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void dragDropped(DragEvent e){
        MouseEvent me = new MouseEvent(MouseEvent.MOUSE_RELEASED,0,0,0,0, MouseButton.PRIMARY,0,true,true,true,true,true,true,true,true,true,true,null);
        mouseReleased(me);
    }

    public void mouseReleased(MouseEvent e) {
        try{
            JSONObject msg = new JSONObject();
            msg.put("Type","mouseReleased");
            msg.put("Key",e.getButton().ordinal());
            msg.put("Name", name);
            Send(msg.toJSONString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void mouseMoved(MouseEvent t) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("Type", "startDrawing");
            msg.put("Name", name);
            msg.put("X", t.getX()/width);
            msg.put("Y", t.getY()/height);
            Send(msg.toJSONString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void mouseWheelMoved(ScrollEvent e){
        try{
            JSONObject msg = new JSONObject();
            msg.put("Type","mouseWheel");
            double value = -e.getDeltaY()/10;
            value = value > 0 ? Math.ceil(value) : -Math.ceil(-value);
            msg.put("value",value);
            msg.put("Name", name);
            Send(msg.toJSONString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void keyPressed(KeyEvent e) {
        try {
            JSONObject msg = new JSONObject();
            if(e.isControlDown() && e.getCode() == KeyCode.F4){
                msg.put("Type", "finish");
                msg.put("Name", name);
                Send(msg.toJSONString().getBytes());
                s.close();
            }else {
                if(!sc.isPhone || e.getText().isEmpty()) {
                    msg.put("Type", "keyPressed");
                    msg.put("value", e.getCode().getCode());
                    msg.put("Name", name);
                    Send(msg.toJSONString().getBytes());
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void keyReleased(KeyEvent e) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("Type", "keyReleased");
            msg.put("value", e.getCode().getCode());
            msg.put("Name", name);
            Send(msg.toJSONString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void Send(byte[] b) throws IOException {
        Message[] messages = Message.getMessages(b, MESSAGESIZE);
        for(Message m : messages){
            sc.getDatagramSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,sc.getAddress(),sc.getPort()));
        }
    }
}