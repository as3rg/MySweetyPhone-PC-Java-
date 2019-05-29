package Utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
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
            BorderPane p = new BorderPane();
            p.addEventFilter(MouseEvent.MOUSE_MOVED, this::mouseMoved);
            p.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::mouseMoved);
            p.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressed);
            p.addEventFilter(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
            p.addEventFilter(ScrollEvent.SCROLL, this::mouseWheelMoved);
            p.addEventFilter(DragEvent.DRAG_DROPPED,this::dragDropped);
            p.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);
            p.addEventFilter(KeyEvent.KEY_RELEASED, this::keyReleased);
            p.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    p.requestFocus();
                }
            });
            s.setAlwaysOnTop(true);
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
            s.setScene(new Scene(p, 600, 600));
            s.show();
            p.requestFocus();
            p.setStyle("-fx-background-color: #202020;");
            Label label = new Label("Для выхода нажмите Ctrl+F4\nДля смены ролей нажмите Alt+S");
            p.setCenter(label);
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
            if(e.isControlDown() && e.getCode() == KeyCode.S) {
                msg.put("Type", "swap");
                msg.put("Name", name);
                Send(msg.toJSONString().getBytes());
                sc.Dsocket.close();
                Thread.sleep(1000);
                SessionServer ss = new SessionServer(sc.getType(), sc.getPort(), ()->{});
                Session.sessions.add(ss);
                Session.sessions.remove(this);
                ss.Start();
                s.close();
            }else {
                msg.put("Type", "keyPressed");
                msg.put("value", e.getCode().getCode());
                msg.put("Name", name);
                Send(msg.toJSONString().getBytes());
            }
        } catch (IOException | InterruptedException e1) {
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