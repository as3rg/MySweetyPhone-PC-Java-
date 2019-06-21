package Utils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
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

    static Point start;
    static Dimension size;
    static{
        start = new Point(0,0);
        size = Toolkit.getDefaultToolkit().getScreenSize();
    }

    SessionClient sc;
    double width;
    double height;
    Stage s;
    String name;
    static final int MESSAGESIZE = 100;
    TextArea textArea;
    public MouseTracker(SessionClient sc, String name) throws IOException {
        this.sc = sc;
        this.name = name;
        Platform.runLater(()-> {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            width = screenSize.getWidth();
            height = screenSize.getHeight();
            s = new Stage();
            StackPane p = new StackPane();
            textArea = new TextArea();
            textArea.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);
            textArea.addEventFilter(KeyEvent.KEY_RELEASED, this::keyReleased);
            p.getChildren().add(textArea);
            textArea.setBackground(new Background(new BackgroundFill(Paint.valueOf("#202020"), CornerRadii.EMPTY, Insets.EMPTY)));
            textArea.setStyle("-fx-background-color: #202020;");
            p.setBackground(new Background(new BackgroundFill(Paint.valueOf("#202020"), CornerRadii.EMPTY, Insets.EMPTY)));
            p.setStyle("-fx-background-color: #202020;");
            if(sc.isPhone){
                textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue.isEmpty()) return;
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
                textArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        textArea.requestFocus();
                    }
                });

            }else {
                textArea.addEventFilter(MouseEvent.MOUSE_MOVED, this::mouseMoved);
                textArea.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::mouseMoved);
                textArea.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressed);
                textArea.addEventFilter(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
                textArea.addEventFilter(ScrollEvent.SCROLL, this::mouseWheelMoved);
                textArea.addEventFilter(DragEvent.DRAG_DROPPED, this::dragDropped);
                textArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
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
            }
            textArea.requestFocus();
            p.getChildren().add(new Label("Для выхода нажмите Ctrl+F4"));
            Scene scene = new Scene(p, 600, 600);
            scene.getStylesheets().add("/sample/style.css");
            s.setScene(scene);
            s.setMaximized(true);
            s.initStyle(StageStyle.UNDECORATED);
            s.setResizable(false);
            s.setOnCloseRequest(Event::consume);
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
            msg.put("Type", "PCMouseMoved");
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
            }else if(!sc.isPhone || e.getText().isEmpty()){
                msg.put("Type", "keyPressed");
                msg.put("value", e.getCode().getCode());
                msg.put("Name", name);
                Send(msg.toJSONString().getBytes());
            }
            textArea.setText("");
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
        textArea.setText("");
    }

    public void Send(byte[] b) throws IOException {
        Message[] messages = Message.getMessages(b, MESSAGESIZE);
        for(Message m : messages){
            sc.getDatagramSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,sc.getAddress(),sc.getPort()));
        }
    }
}