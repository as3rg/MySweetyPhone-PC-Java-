package Utils;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.sikuli.script.Screen;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.*;

public class SessionServer extends Session{
    Thread onStop;
    ServerSocket ss;
    MessageParser messageParser;

    public SessionServer(int type, int Port, Runnable doOnStopSession) throws IOException {
        FileInputStream propFile = new FileInputStream(new File("properties.properties"));
        Properties props = new Properties();
        props.load(propFile);
        propFile.close();

        String name = (String) props.getOrDefault("name", ""),
                login = (String) props.getOrDefault("login", "");

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
        message.put("type", type);
        message.put("name", name);
        message.put("subtype", "PC");
        byte[] buf2 = String.format("%-100s", message.toJSONString()).getBytes();

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
                        Screen s = new Screen();
                        DatagramPacket p;
                        SimpleIntegerProperty gotAccess = new SimpleIntegerProperty(0);
                        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                        double width = screenSize.getWidth();
                        double height = screenSize.getHeight();
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

                            if(gotAccess.get() == 0 && msg.containsKey("Login") && msg.get("Login").equals(login))
                                gotAccess.set(2);
                            else if(gotAccess.get() == 0) {
                                gotAccess.set(1);
                                Platform.runLater(() -> {
                                    try {
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
                            }

                            if(gotAccess.get() == 2)
                                try {
                                    switch ((String) msg.get("Type")) {
                                        case "mouseMoved":
                                            Point point = MouseInfo.getPointerInfo().getLocation();
                                            r.mouseMove(((Number) msg.get("X")).intValue() + (int) point.getX(), ((Number) msg.get("Y")).intValue() + (int) point.getY());
                                            break;
                                        case "mouseReleased":
                                            r.mouseRelease(InputEvent.getMaskForButton(((Number) msg.get("Key")).intValue()));
                                            break;
                                        case "mouseClicked":
                                            r.mousePress(InputEvent.getMaskForButton(((Number) msg.get("Key")).intValue()));
                                            r.mouseRelease(InputEvent.getMaskForButton(((Number) msg.get("Key")).intValue()));
                                            break;
                                        case "mousePressed":
                                            r.mousePress(InputEvent.getMaskForButton(((Number) msg.get("Key")).intValue()));
                                            break;
                                        case "mouseWheel":
                                            r.mouseWheel(((Number) msg.get("value")).intValue());
                                            break;
                                        case "keyReleased":
                                            r.keyRelease(((Number) msg.get("value")).intValue());
                                            break;
                                        case "keyPressed":
                                            r.keyPress(((Number) msg.get("value")).intValue());
                                            break;
                                        case "keyClicked":
                                            r.keyPress(((Number) msg.get("value")).intValue());
                                            r.keyRelease(((Number) msg.get("value")).intValue());
                                            break;
                                        case "keysTyped":
                                            if (msg.get("Subtype").equals("hotkey")) {
                                                for (char i : (((String) msg.get("value")).toCharArray())) {
                                                    r.keyPress(KeyEvent.getExtendedKeyCodeForChar(i));
                                                    r.keyRelease(KeyEvent.getExtendedKeyCodeForChar(i));
                                                }
                                            } else {
                                                s.paste((String) msg.get("value"));
                                            }
                                            break;
                                        case "finish":
                                            r.keyRelease(KeyEvent.VK_ALT);
                                            r.keyRelease(KeyEvent.VK_WINDOWS);
                                            r.keyRelease(KeyEvent.VK_CONTROL);
                                            r.keyRelease(KeyEvent.VK_SHIFT);
                                            Stop();
                                            return;
                                        case "PCMouseMoved":
                                            r.mouseMove((int) (((Number) msg.get("X")).doubleValue() * width), (int) (((Number) msg.get("Y")).doubleValue() * height));
                                            break;
                                        case "draw":
                                            r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                        case "startDrawing":
                                            double x = ((Number) msg.get("X")).doubleValue(), y = ((Number) msg.get("Y")).doubleValue();
                                            x = Math.min(1,Math.max(0, x));
                                            y = Math.min(1,Math.max(0, y));
                                            r.mouseMove(
                                                (int)(MouseTracker.start.getX() + x * MouseTracker.size.getWidth()),
                                                (int)(MouseTracker.start.getY() + y * MouseTracker.size.getHeight())
                                            );
                                            break;
                                        case "start":
                                            break;
                                        default:
                                            System.err.println(msgString);
                                    }
                                }catch (IllegalArgumentException ignored){}
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
                            if(line == null){
                                Stop();
                                break;
                            }
                            JSONObject msg = (JSONObject) JSONValue.parse(line);

                            if(gotAccess.get() == 0 && msg.containsKey("Login") && msg.get("Login").equals(login))
                                gotAccess.set(2);
                            else if(gotAccess.get() == 0) {
                                gotAccess.set(1);
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                    alert.setTitle("Выполнить действие?");
                                    alert.setHeaderText("Вы действительно хотите предоставить доступ к файлам \"" + msg.get("Name") + "\"?");
                                    Optional<ButtonType> option = alert.showAndWait();

                                    if (option.get() != ButtonType.OK) {
                                        new Thread(() -> {
                                            try {
                                                JSONObject ans = new JSONObject();
                                                ans.put("Type", "finish");
                                                writer.println(ans.toJSONString());
                                                writer.flush();
                                                Stop();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }).start();
                                    } else {
                                        gotAccess.set(2);
                                    }

                                });
                            }

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
                                        if(((String)msg.get("Dir")).isEmpty() && (!msg.containsKey("DirName") || ((String)msg.get("DirName")).isEmpty())){
                                            files = File.listRoots();
                                            ans.put("Dir", "");
                                        }else{
                                            File dir;
                                            if(((String)msg.get("Dir")).isEmpty())
                                                dir = new File((String)msg.get("DirName"));
                                            else
                                                dir = new File((String)msg.get("Dir"), msg.containsKey("DirName") ? (String)msg.get("DirName") : "");
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
                                    case "deleteFile":
                                        File file = new File((String)msg.get("Dir"), (String)msg.get("FileName"));
                                        boolean result = file.delete();
                                        ans.put("State", result ? 0 : 1);        //0 - без ошибок, 1 - нет доступа
                                        ans.put("Type", "deleteFile");
                                        writer.println(ans.toJSONString());
                                        writer.flush();
                                        break;
                                    case "newDir":
                                        ans.put("Type", "newDirAnswer");
                                        file = new File((String)msg.get("Dir"), (String)msg.get("DirName"));
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
                                                Socket s = new Socket(((InetSocketAddress) Ssocket.getRemoteSocketAddress()).getAddress(), ((Number) msg.get("FileSocketPort")).intValue());
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
                                                Socket s = new Socket(((InetSocketAddress) Ssocket.getRemoteSocketAddress()).getAddress(), ((Number) msg.get("FileSocketPort")).intValue());
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
                                    case "finish":
                                        Stop();
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