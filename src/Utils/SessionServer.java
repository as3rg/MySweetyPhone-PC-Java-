package Utils;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.*;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class SessionServer extends Session{
    Thread onStop;
    MessageParser messageParser;

    public SessionServer(Type type, int Port, Runnable doOnStopSession) throws IOException {
        onStop = new Thread(doOnStopSession);
        messageParser = new MessageParser();
        JSONObject message = new JSONObject();
        socket = new DatagramSocket();
        port = socket.getLocalPort();
        if(Port == -1)
            port = Port;
        message.put("port", port);
        message.put("type", type.ordinal());
        byte[] buf2 = String.format("%-30s", message.toJSONString()).getBytes();
        DatagramSocket s = new DatagramSocket();
        s.setBroadcast(true);
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length, Inet4Address.getByName("255.255.255.255"), BroadCastingPort);

        broadcasting = new Timer();
        TimerTask broadcastingTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    s.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        broadcasting.schedule(broadcastingTask, 2000, 1000);

        switch (type) {
            case MOUSE:
                t = new Thread(() -> {
                    try {
                        socket.setBroadcast(true);
                        Robot r = new Robot();
                        DatagramPacket p;
                        SimpleIntegerProperty gotAccess = new SimpleIntegerProperty(0);
                        while (!socket.isClosed()) {
                            if(gotAccess.get() == 1)
                                continue;
                            Message m = null;
                            int head = -1;
                            p = null;
                            do{
                                byte[] buf = new byte[Message.getMessageSize(MouseTracker.MESSAGESIZE)];
                                p = new DatagramPacket(buf, buf.length);
                                try {
                                    socket.receive(p);
                                    if(onStop != null){
                                        Platform.runLater(onStop);
                                        onStop = null;
                                    }
                                    m = new Message(p.getData());
                                    messageParser.messageMap.put(m.getId(), m);
                                    if (head == -1)
                                        head = m.getId();
                                } catch (SocketException ignored){
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }while (!socket.isClosed() && (m == null || m.getNext() != -1));
                            if(messageParser.messageMap.get(head) == null) continue;
                            String msgString = new String(messageParser.parse(head));
                            JSONObject msg = (JSONObject) JSONValue.parse(msgString);

                            if(gotAccess.get() != 2)
                                Platform.runLater(()-> {
                                    try {
                                        gotAccess.set(1);
                                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                        alert.setTitle("Выполнить действие?");
                                        alert.setHeaderText("Вы действительно хотите предоставить доступ к управлению компьютером \"" + msg.get("Name") + "\"?");
                                        Optional<ButtonType> option = alert.showAndWait();

                                        if (option.get() != ButtonType.OK) {
                                            Stop();
                                        } else
                                            gotAccess.set(2);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });

                            if(gotAccess.get() == 2 && msg!=null)
                                switch ((String)msg.get("Type")){
                                    case "mouseMoved":
                                        Point point = MouseInfo.getPointerInfo().getLocation();
                                        r.mouseMove(((Long) msg.get("X")).intValue() + (int)point.getX(), ((Long) msg.get("Y")).intValue() + (int)point.getY());
                                        break;
                                    case "mouseReleased":
                                        r.mouseRelease(InputEvent.getMaskForButton(((Long) msg.get("Key")).intValue()));
                                        break;
                                    case "mousePressed":
                                        r.mousePress(InputEvent.getMaskForButton(((Long) msg.get("Key")).intValue()));
                                        break;
                                    case "mouseWheel":
                                        r.mouseWheel(((Long)msg.get("value")).intValue());
                                        break;
                                    case "keyReleased":
                                        r.keyRelease(((Long)msg.get("value")).intValue());
                                        break;
                                    case "keyPressed":
                                        r.keyPress(((Long)msg.get("value")).intValue());
                                        break;
                                    case "swap":
                                        SessionClient sc = new SessionClient(p.getAddress(),port,type);
                                        socket.close();
                                        Session.sessions.add(sc);
                                        Session.sessions.remove(this);
                                        sc.Start();
                                        return;
                                    case "finish":
                                        r.keyRelease(KeyEvent.VK_ALT);
                                        Stop();
                                        return;
                                    case "start":
                                        return;
                                    default:
                                        System.out.println(msgString);
                                }
                        }
                    } catch (AWTException | IOException e) {
                        e.printStackTrace();
                    }

                });
                break;
            default:
                throw new RuntimeException("Неизвестный тип сессии");
        }
    }

    public boolean isServer(){
        return true;
    }
}