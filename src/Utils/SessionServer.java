package Utils;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class SessionServer extends Session{
    Thread onStop;
    ServerSocket ss;
    MessageParser messageParser;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    double width = screenSize.getWidth();
    double height = screenSize.getHeight();

    public SessionServer(Type type, int Port, Runnable doOnStopSession) throws IOException {
        onStop = new Thread(doOnStopSession);
        messageParser = new MessageParser();
        JSONObject message = new JSONObject();
        switch (type){
            case MOUSE:
                Dsocket = new DatagramSocket(Port);
                Dsocket.setBroadcast(true);
                port = Dsocket.getLocalPort();
                break;
            case FILEVIEW:
                ss = new ServerSocket(Port);
                port = ss.getLocalPort();
        }
        message.put("port", port);
        message.put("type", type.ordinal());
        byte[] buf2 = String.format("%-30s", message.toJSONString()).getBytes();

        broadcastingSocket = new DatagramSocket();
        broadcastingSocket.setBroadcast(true);
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length, Inet4Address.getByName("255.255.255.255"), BroadCastingPort);

        broadcasting = new Timer();
        TimerTask broadcastingTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    broadcastingSocket.send(packet);
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
                        Dsocket.setBroadcast(true);
                        Robot r = new Robot();
                        DatagramPacket p;
                        SimpleIntegerProperty gotAccess = new SimpleIntegerProperty(0);
                        while (!Dsocket.isClosed()) {
                            if(gotAccess.get() == 1)
                                continue;
                            Message m = null;
                            int head = -1;
                            p = null;
                            do{
                                byte[] buf = new byte[Message.getMessageSize(MouseTracker.MESSAGESIZE)];
                                p = new DatagramPacket(buf, buf.length);
                                try {
                                    Dsocket.receive(p);
                                    broadcasting.cancel();
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
                            }while (!Dsocket.isClosed() && (m == null || m.getNext() != -1));
                            if(messageParser.messageMap.get(head) == null) continue;
                            String msgString = new String(messageParser.parse(head));
                            JSONObject msg = (JSONObject) JSONValue.parse(msgString);

                            if(gotAccess.get() == 0)
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

                            System.out.println(msgString);
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
                                    case "keyClicked":
                                        r.keyPress(((Long)msg.get("value")).intValue());
                                        r.keyRelease(((Long)msg.get("value")).intValue());
                                        break;
                                    case "keysTyped":
                                        if(msg.get("Subtype").equals("hotkey")){
                                            for(char i : (((String)msg.get("value")).toCharArray())) {
                                                r.keyPress(KeyEvent.getExtendedKeyCodeForChar(i));
                                                r.keyRelease(KeyEvent.getExtendedKeyCodeForChar(i));
                                            }
                                        }else {
                                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                            clipboard.setContents(new StringSelection((String) msg.get("value")), null);

                                            r.keyPress(KeyEvent.VK_CONTROL);
                                            r.keyPress(KeyEvent.VK_V);
                                            r.keyRelease(KeyEvent.VK_V);
                                            r.keyRelease(KeyEvent.VK_CONTROL);
//                                        for(char Char : ((String) msg.get("value")).toCharArray()) {
//                                            r.keyPress(KeyEvent.VK_ALT);
//                                            String Char2 = Integer.toString(charToAltCode(Char));
//                                            for (char c : Char2.toCharArray()) {
//                                                int keyToPress;
//                                                switch (c) {
//                                                    case '0':
//                                                        keyToPress = KeyEvent.VK_NUMPAD0;
//                                                        break;
//                                                    case '1':
//                                                        keyToPress = KeyEvent.VK_NUMPAD1;
//                                                        break;
//                                                    case '2':
//                                                        keyToPress = KeyEvent.VK_NUMPAD2;
//                                                        break;
//                                                    case '3':
//                                                        keyToPress = KeyEvent.VK_NUMPAD3;
//                                                        break;
//                                                    case '4':
//                                                        keyToPress = KeyEvent.VK_NUMPAD4;
//                                                        break;
//                                                    case '5':
//                                                        keyToPress = KeyEvent.VK_NUMPAD5;
//                                                        break;
//                                                    case '6':
//                                                        keyToPress = KeyEvent.VK_NUMPAD6;
//                                                        break;
//                                                    case '7':
//                                                        keyToPress = KeyEvent.VK_NUMPAD7;
//                                                        break;
//                                                    case '8':
//                                                        keyToPress = KeyEvent.VK_NUMPAD8;
//                                                        break;
//                                                    case '9':
//                                                        keyToPress = KeyEvent.VK_NUMPAD9;
//                                                        break;
//                                                    default:
//                                                        throw new RuntimeException();
//                                                }
//                                                r.keyPress(keyToPress);
//                                                Thread.sleep(10);
//                                                r.keyRelease(keyToPress);
//                                                Thread.sleep(10);
//                                            }
//                                            r.keyRelease(KeyEvent.VK_ALT);
//                                            Thread.sleep(100);
//                                        }
                                        }
                                        break;
                                    case "swap":
                                        SessionClient sc = new SessionClient(p.getAddress(),port,type);
                                        Dsocket.close();
                                        Session.sessions.add(sc);
                                        Session.sessions.remove(this);
                                        sc.Start();
                                        return;
                                    case "finish":
                                        r.keyRelease(KeyEvent.VK_ALT);
                                        Stop();
                                        return;
                                    case "startDrawing":
                                        r.mouseMove((int)((Double) msg.get("X") * width), (int)((Double) msg.get("Y") * height));
                                        break;
                                    case "draw":
                                        r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                                        double width = screenSize.getWidth();
                                        double height = screenSize.getHeight();
                                        r.mouseMove((int)((Double) msg.get("X") * width), (int)((Double) msg.get("Y") * height));
                                        break;
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
            case FILEVIEW:
                t = new Thread(()->{
                    try {
                        Ssocket = ss.accept();
                        broadcasting.cancel();
                        if(onStop != null){
                            Platform.runLater(onStop);
                            onStop = null;
                        }
                        PrintWriter writer = new PrintWriter(Ssocket.getOutputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(Ssocket.getInputStream()));
                        SimpleIntegerProperty gotAccess = new SimpleIntegerProperty(0);
                        while (true) {
                            String line = reader.readLine();
                            System.out.println(line);
                            JSONObject msg = (JSONObject) JSONValue.parse(line);
                            if(gotAccess.get() == 0)
                                Platform.runLater(()-> {
                                    try {
                                        gotAccess.set(1);
                                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                        alert.setTitle("Выполнить действие?");
                                        alert.setHeaderText("Вы действительно хотите предоставить доступ к файлам \"" + msg.get("Name") + "\"?");
                                        Optional<ButtonType> option = alert.showAndWait();

                                        if (option.get() != ButtonType.OK) {
                                            Stop();
                                        } else {
                                            gotAccess.set(2);
                                        }

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });

                            if(gotAccess.get() == 2){
                                JSONObject ans = new JSONObject();
                                if(msg.get("Type").equals("showDir") && ((String)msg.get("Dir")).isEmpty())
                                    msg.put("Type", "start");
                                switch ((String)msg.get("Type")){
                                    case "back":
                                        String parentDir = new File((String)msg.get("Dir")).getParent();
                                        msg.put("Dir", parentDir == null ? "" : parentDir);
                                    case "start":
                                        if(msg.get("Type").equals("start")) msg.put("Dir", "");
                                    case "showDir":
                                        File[] files;
                                        if(((String)msg.get("Dir")).isEmpty()){
                                            files = File.listRoots();
                                            ans.put("Dir", "");
                                        }else{
                                            File dir = new File((String)msg.get("Dir"));
                                            files = dir.listFiles();
                                            ans.put("Dir", dir.getPath());
                                        }
                                        JSONArray files2 = new JSONArray();
                                        ans.put("State", files != null ? 0 : 1);        //0 - без ошибок, 1 - нет доступа
                                        if(files != null) for(File f : files){
                                            JSONObject file = new JSONObject();
                                            file.put("Name", f.getName().isEmpty() ? f.getPath() : f.getName());
                                            file.put("Type", f.isDirectory() ? "Folder" : "File");
                                            files2.add(file);
                                        }
                                        ans.put("Inside", files2);
                                        ans.put("Type", "showDir");
                                        writer.println(ans.toJSONString());
                                        writer.flush();
                                        break;
                                    case "newDir":
                                        ans.put("Type", "newDirAnswer");
                                        File file = new File((String)msg.get("Dir"), (String)msg.get("DirName"));
                                        boolean state = file.mkdirs();
                                        ans.put("State", state ? 1 : 0);
                                        ans.put("Dir", msg.get("Dir"));
                                        ans.put("DirName", msg.get("DirName"));
                                        writer.println(ans.toJSONString());
                                        writer.flush();
                                        break;
                                    case "uploadFile":
                                        new Thread(()->{
                                            try {
                                                Socket s = new Socket(((InetSocketAddress) Ssocket.getRemoteSocketAddress()).getAddress(), ((Long) msg.get("FileSocketPort")).intValue());
                                                File getFile = new File((String) msg.get("Dir"), (String) msg.get("FileName"));
                                                getFile.createNewFile();
                                                FileOutputStream fileout = new FileOutputStream(getFile);
                                                DataInputStream filein = new DataInputStream(s.getInputStream());
                                                IOUtils.copy(filein, fileout);
                                                s.close();
                                                fileout.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }).start();
                                        break;
                                    case "downloadFile":
                                        new Thread(()->{
                                            try {
                                                Socket s = new Socket(((InetSocketAddress) Ssocket.getRemoteSocketAddress()).getAddress(), ((Long) msg.get("FileSocketPort")).intValue());
                                                File getFile = new File((String) msg.get("Dir"), (String) msg.get("FileName"));
                                                FileInputStream filein = new FileInputStream(getFile);
                                                DataOutputStream fileout = new DataOutputStream(s.getOutputStream());
                                                IOUtils.copy(filein, fileout);
                                                fileout.flush();
                                                s.close();
                                                filein.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }).start();
                                        break;
                                }
                            }
                        }
                    } catch (IOException e) {
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

    @Override
    public void Stop() throws IOException {
        super.Stop();
        if(ss!=null)
            ss.close();
    }
}