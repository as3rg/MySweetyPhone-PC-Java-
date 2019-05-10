package Utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.*;
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
    static final int MESSAGESIZE = 100;
    public MouseTracker(SessionClient sc){
        Platform.runLater(()-> {
            this.sc = sc;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            width = screenSize.getWidth();
            height = screenSize.getHeight();
            s = new Stage();
            Pane p = new Pane();
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
                    Message[] messages = Message.getMessages(msg.toJSONString().getBytes(), MESSAGESIZE);
                    for (Message m : messages) {
                        sc.getSocket().send(new DatagramPacket(m.getArr(), m.getArr().length, sc.getAddress(), sc.getPort()));
                    }
                    s.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            s.setScene(new Scene(p, 600, 600));
            s.show();
            s.getScene().getRoot().requestFocus();
        });
    }

    public void mousePressed(MouseEvent e)
    {
        try {
            JSONObject msg = new JSONObject();
            msg.put("Type","mousePressed");
            msg.put("Key",e.getButton().ordinal());
            Send(msg.toJSONString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void dragDropped(DragEvent e){
        MouseEvent me = new MouseEvent(MouseEvent.MOUSE_RELEASED,0,0,0,0, MouseButton.PRIMARY,0,true,true,true,true,true,true,true,true,true,true,null);
        mouseReleased(me);
    }

    public void mouseReleased(MouseEvent e)
    {
        try{
            JSONObject msg = new JSONObject();
            msg.put("Type","mouseReleased");
            msg.put("Key",e.getButton().ordinal());
            Send(msg.toJSONString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void mouseMoved(MouseEvent t) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("Type", "mouseMoved");
            msg.put("X", t.getX());
            msg.put("Y", t.getY());
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
            Send(msg.toJSONString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void keyPressed(KeyEvent e) {
        try {
            JSONObject msg = new JSONObject();
            if(e.isAltDown() && e.getCode() == KeyCode.S) {
                msg.put("Type", "swap");
                Send(msg.toJSONString().getBytes());
                sc.socket.close();
                Thread.sleep(1000);
                SessionServer ss = new SessionServer(sc.getType(), sc.getPort(), ()->{});
                Session.sessions.add(ss);
                Session.sessions.remove(this);
                ss.Start();
                s.close();
            }else {
                msg.put("Type", "keyPressed");
                msg.put("value", e.getCode().getCode());
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
            Send(msg.toJSONString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void Send(byte[] b) throws IOException {
        Message[] messages = Message.getMessages(b, MESSAGESIZE);
        for(Message m : messages){
            sc.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,sc.getAddress(),sc.getPort()));
        }
    }
}