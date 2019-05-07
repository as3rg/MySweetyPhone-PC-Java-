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
    public MouseTracker(SessionClient sc) throws IOException, AWTException {
        Platform.runLater(()-> {
            this.sc = sc;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            width = screenSize.getWidth();
            height = screenSize.getHeight();
            s = new Stage();
            Pane p = new Pane();
            p.addEventFilter(MouseEvent.MOUSE_MOVED, this::mouseMoved);
//            p.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::mouseMoved);
            p.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressed);
            p.addEventFilter(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
            p.addEventFilter(MouseEvent.MOUSE_CLICKED, this::mouseClicked);
            p.addEventFilter(ScrollEvent.SCROLL, this::mouseWheelMoved);
//            p.addEventFilter(DragEvent.DRAG_DROPPED,this::dragDropped);
            p.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);
            p.addEventFilter(KeyEvent.KEY_RELEASED, this::keyReleased);
            p.addEventFilter(KeyEvent.KEY_TYPED, this::keyTyped);
            s.setAlwaysOnTop(true);
            s.setMaximized(true);
            s.initStyle(StageStyle.UNDECORATED);
            s.setResizable(false);
            s.setOnCloseRequest((e) -> {
            });
            s.setScene(new Scene(p, 600, 600));
            s.show();
        });
    }

    public void mousePressed(MouseEvent e)
    {
        try {
            JSONObject msg = new JSONObject();
            msg.put("Type","mousePressed");
            msg.put("Key",e.getButton().ordinal());
            Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
            for(Message m : messages){
                    sc.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,sc.getAddress(),sc.getPort()));
            }
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
            Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
            for(Message m : messages){
                sc.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,sc.getAddress(),sc.getPort()));
            }
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
            Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
            for(Message m : messages){
                sc.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,sc.getAddress(),sc.getPort()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void mouseClicked(MouseEvent t){
        mousePressed(t);
        mouseReleased(t);
    }

    public void mouseWheelMoved(ScrollEvent e){
        try{
            JSONObject msg = new JSONObject();
            msg.put("Type","mouseWheel");
            msg.put("value",e.getDeltaY());
            Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
            for(Message m : messages){
                sc.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,sc.getAddress(),sc.getPort()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void keyPressed(KeyEvent e) {
        try {
            JSONObject msg = new JSONObject();
            if (e.isAltDown() && e.getCode() == KeyCode.F4){
                msg.put("Type", "finish");
                Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
                for(Message m : messages){
                    sc.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,sc.getAddress(),sc.getPort()));
                }
                s.close();
                Thread.sleep(100);
                sc.Stop();
//            }else if(e.isAltDown() && e.getKeyCode() == KeyEvent.VK_S) {
//                msg.put("Type", "swap");
//                Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
//                for(Message m : messages){
//                    sc.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,sc.getAddress(),sc.getPort()));
//                }
//                sc.socket.close();
//                Thread.sleep(1000);
//                SessionServer ss = new SessionServer(sc.getAddress(),sc.getPort(),sc.getType());
//                Session.sessions.add(sc);
//                Session.sessions.remove(this);
//                sc.Start();
//                f.dispose();
            }else{
                msg.put("Type", "keyPressed");
                msg.put("value", e.getCode());
                Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
                for(Message m : messages){
                    sc.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,sc.getAddress(),sc.getPort()));
                }
            }
        } catch (InterruptedException | IOException e1) {
            e1.printStackTrace();
        }
    }

    public void keyReleased(KeyEvent e) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("Type", "keyReleased");
            msg.put("value", e.getCode());
            Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
            for (Message m : messages) {
                sc.getSocket().send(new DatagramPacket(m.getArr(), m.getArr().length, sc.getAddress(), sc.getPort()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void keyTyped(KeyEvent e) {
        keyPressed(e);
        keyReleased(e);
    }
}