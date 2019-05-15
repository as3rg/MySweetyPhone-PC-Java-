package Utils;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class SessionServer extends Session{
    Thread onStop;
    MessageParser messageParser;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    double width = screenSize.getWidth();
    double height = screenSize.getHeight();

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
                                        socket.close();
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
                        socket.setBroadcast(true);
                        DatagramPacket p;
                        DatagramSocket answerer;
                        SimpleIntegerProperty gotAccess = new SimpleIntegerProperty(0);
                        while (!socket.isClosed()) {
                            if(gotAccess.get() == 1)
                                continue;
                            Message m = null;
                            int head = -1;
                            p = null;
                            do{
                                byte[] buf = new byte[Message.getMessageSize(100)];
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
                                answerer = new DatagramSocket();
                                answerer.setBroadcast(true);
                                JSONObject ans = new JSONObject();
                                if(msg.get("Type").equals("showDir") && ((String)msg.get("Dir")).isEmpty())
                                    msg.put("Type", "start");
                                switch ((String)msg.get("Type")){
                                    case "showDir":
                                        File[] files = ((String)msg.get("Dir")).isEmpty() ? File.listRoots() : new File((String)msg.get("Dir")).listFiles();
                                        JSONArray files2 = new JSONArray();
                                        for(File f : files){
                                            JSONObject file = new JSONObject();
                                            file.put("Name", f.getName());
                                            file.put("Type", f.isDirectory() ? "Folder" : "File");
                                            files2.add(file);
                                        }
                                        ans.put("Inside", files2);
                                        ans.put("Dir", msg.get("Dir"));
                                        for(Message ansmsg : Message.getMessages(ans.toJSONString().getBytes(), 100)){
                                            System.out.println(new String(ansmsg.getBody())+":"+ansmsg.getNext());
                                            answerer.send(new DatagramPacket(ansmsg.getArr(), ansmsg.getArr().length, p.getAddress(), ((Long)msg.get("BackChatPort")).intValue()));
                                        }
                                        break;
                                    case "start":
                                        files = File.listRoots();
                                        files2 = new JSONArray();
                                        for(File f : files){
                                            JSONObject file = new JSONObject();
                                            file.put("Name", f.getPath());
                                            file.put("Type", "Folder");
                                            files2.add(file);
                                        }
                                        ans.put("Inside", files2);
                                        ans.put("Dir", "");
                                        for(Message ansmsg : Message.getMessages(ans.toJSONString().getBytes(), 100)){
                                            answerer.send(new DatagramPacket(ansmsg.getArr(), ansmsg.getArr().length, p.getAddress(), ((Long)msg.get("BackChatPort")).intValue()));
                                        }
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

//    public static int charToAltCode(char c){
//        switch (c){
//            case '☺':
//                return 1;
//            case '☻':
//                return 2;
//            case '♥':
//                return 3;
//            case '♦':
//                return 4;
//            case '♣':
//                return 5;
//            case '♠':
//                return 6;
//            case '•':
//                return 7;
//            case '◘':
//                return 8;
//            case '○':
//                return 9;
//            case '◙':
//                return 10;
//            case '♂':
//                return 11;
//            case '♀':
//                return 12;
//            case '♪':
//                return 13;
//            case '♫':
//                return 14;
//            case '☼':
//                return 15;
//            case '►':
//                return 16;
//            case '◄':
//                return 17;
//            case '↕':
//                return 18;
//            case '‼':
//                return 19;
//            case '¶':
//                return 20;
//            case '§':
//                return 21;
//            case '▬':
//                return 22;
//            case '↨':
//                return 23;
//            case '↑':
//                return 24;
//            case '↓':
//                return 25;
//            case '→':
//                return 26;
//            case '←':
//                return 27;
//            case '∟':
//                return 28;
//            case '↔':
//                return 29;
//            case '▲':
//                return 30;
//            case '▼':
//                return 31;
//            case ' ':
//                return 32;
//            case '!':
//                return 33;
//            case '"':
//                return 34;
//            case '#':
//                return 35;
//            case '$':
//                return 36;
//            case '%':
//                return 37;
//            case '&':
//                return 38;
//            case '\'':
//                return 39;
//            case '(':
//                return 40;
//            case ')':
//                return 41;
//            case '*':
//                return 42;
//            case '+':
//                return 43;
//            case ',':
//                return 44;
//            case '-':
//                return 45;
//            case '.':
//                return 46;
//            case '/':
//                return 47;
//            case '0':
//                return 48;
//            case '1':
//                return 49;
//            case '2':
//                return 50;
//            case '3':
//                return 51;
//            case '4':
//                return 52;
//            case '5':
//                return 53;
//            case '6':
//                return 54;
//            case '7':
//                return 55;
//            case '8':
//                return 56;
//            case '9':
//                return 57;
//            case ':':
//                return 58;
//            case ';':
//                return 59;
//            case '<':
//                return 60;
//            case '=':
//                return 61;
//            case '>':
//                return 62;
//            case '?':
//                return 63;
//            case '@':
//                return 64;
//            case 'A':
//                return 65;
//            case 'B':
//                return 66;
//            case 'C':
//                return 67;
//            case 'D':
//                return 68;
//            case 'E':
//                return 69;
//            case 'F':
//                return 70;
//            case 'G':
//                return 71;
//            case 'H':
//                return 72;
//            case 'I':
//                return 73;
//            case 'J':
//                return 74;
//            case 'K':
//                return 75;
//            case 'L':
//                return 76;
//            case 'M':
//                return 77;
//            case 'N':
//                return 78;
//            case 'O':
//                return 79;
//            case 'P':
//                return 80;
//            case 'Q':
//                return 81;
//            case 'R':
//                return 82;
//            case 'S':
//                return 83;
//            case 'T':
//                return 84;
//            case 'U':
//                return 85;
//            case 'V':
//                return 86;
//            case 'W':
//                return 87;
//            case 'X':
//                return 88;
//            case 'Y':
//                return 89;
//            case 'Z':
//                return 90;
//            case '[':
//                return 91;
//            case '\\':
//                return 92;
//            case ']':
//                return 93;
//            case '^':
//                return 94;
//            case '_':
//                return 95;
//            case '`':
//                return 96;
//            case 'a':
//                return 97;
//            case 'b':
//                return 98;
//            case 'c':
//                return 99;
//            case 'd':
//                return 100;
//            case 'e':
//                return 101;
//            case 'f':
//                return 102;
//            case 'g':
//                return 103;
//            case 'h':
//                return 104;
//            case 'i':
//                return 105;
//            case 'j':
//                return 106;
//            case 'k':
//                return 107;
//            case 'l':
//                return 108;
//            case 'm':
//                return 109;
//            case 'n':
//                return 110;
//            case 'o':
//                return 111;
//            case 'p':
//                return 112;
//            case 'q':
//                return 113;
//            case 'r':
//                return 114;
//            case 's':
//                return 115;
//            case 't':
//                return 116;
//            case 'u':
//                return 117;
//            case 'v':
//                return 118;
//            case 'w':
//                return 119;
//            case 'x':
//                return 120;
//            case 'y':
//                return 121;
//            case 'z':
//                return 122;
//            case '{':
//                return 123;
//            case '|':
//                return 124;
//            case '}':
//                return 125;
//            case '~':
//                return 126;
//            case '⌂':
//                return 127;
//            case 'А':
//                return 128;
//            case 'Б':
//                return 129;
//            case 'В':
//                return 130;
//            case 'Г':
//                return 131;
//            case 'Д':
//                return 132;
//            case 'Е':
//                return 133;
//            case 'Ж':
//                return 134;
//            case 'З':
//                return 135;
//            case 'И':
//                return 136;
//            case 'Й':
//                return 137;
//            case 'К':
//                return 138;
//            case 'Л':
//                return 139;
//            case 'М':
//                return 140;
//            case 'Н':
//                return 141;
//            case 'О':
//                return 142;
//            case 'П':
//                return 143;
//            case 'Р':
//                return 144;
//            case 'С':
//                return 145;
//            case 'Т':
//                return 146;
//            case 'У':
//                return 147;
//            case 'Ф':
//                return 148;
//            case 'Х':
//                return 149;
//            case 'Ц':
//                return 150;
//            case 'Ч':
//                return 151;
//            case 'Ш':
//                return 152;
//            case 'Щ':
//                return 153;
//            case 'Ъ':
//                return 154;
//            case 'Ы':
//                return 155;
//            case 'Ь':
//                return 156;
//            case 'Э':
//                return 157;
//            case 'Ю':
//                return 158;
//            case 'Я':
//                return 159;
//            case 'а':
//                return 160;
//            case 'б':
//                return 161;
//            case 'в':
//                return 162;
//            case 'г':
//                return 163;
//            case 'д':
//                return 164;
//            case 'е':
//                return 165;
//            case 'ж':
//                return 166;
//            case 'з':
//                return 167;
//            case 'и':
//                return 168;
//            case 'й':
//                return 169;
//            case 'к':
//                return 170;
//            case 'л':
//                return 171;
//            case 'м':
//                return 172;
//            case 'н':
//                return 173;
//            case 'о':
//                return 174;
//            case 'п':
//                return 175;
//            case '░':
//                return 176;
//            case '▒':
//                return 177;
//            case '▓':
//                return 178;
//            case '│':
//                return 179;
//            case '┤':
//                return 180;
//            case '╡':
//                return 181;
//            case '╢':
//                return 182;
//            case '╖':
//                return 183;
//            case '╕':
//                return 184;
//            case '╣':
//                return 185;
//            case '║':
//                return 186;
//            case '╗':
//                return 187;
//            case '╝':
//                return 188;
//            case '╜':
//                return 189;
//            case '╛':
//                return 190;
//            case '┐':
//                return 191;
//            case '└':
//                return 192;
//            case '┴':
//                return 193;
//            case '┬':
//                return 194;
//            case '├':
//                return 195;
//            case '─':
//                return 196;
//            case '┼':
//                return 197;
//            case '╞':
//                return 198;
//            case '╟':
//                return 199;
//            case '╚':
//                return 200;
//            case '╔':
//                return 201;
//            case '╩':
//                return 202;
//            case '╦':
//                return 203;
//            case '╠':
//                return 204;
//            case '═':
//                return 205;
//            case '╬':
//                return 206;
//            case '╧':
//                return 207;
//            case '╨':
//                return 208;
//            case '╤':
//                return 209;
//            case '╥':
//                return 210;
//            case '╙':
//                return 211;
//            case '╘':
//                return 212;
//            case '╒':
//                return 213;
//            case '╓':
//                return 214;
//            case '╫':
//                return 215;
//            case '╪':
//                return 216;
//            case '┘':
//                return 217;
//            case '┌':
//                return 218;
//            case '█':
//                return 219;
//            case '▄':
//                return 220;
//            case '▌':
//                return 221;
//            case '▐':
//                return 222;
//            case '▀':
//                return 223;
//            case 'р':
//                return 224;
//            case 'с':
//                return 225;
//            case 'т':
//                return 226;
//            case 'у':
//                return 227;
//            case 'ф':
//                return 228;
//            case 'х':
//                return 229;
//            case 'ц':
//                return 230;
//            case 'ч':
//                return 231;
//            case 'ш':
//                return 232;
//            case 'щ':
//                return 233;
//            case 'ъ':
//                return 234;
//            case 'ы':
//                return 235;
//            case 'ь':
//                return 236;
//            case 'э':
//                return 237;
//            case 'ю':
//                return 238;
//            case 'я':
//                return 239;
//            case 'Ё':
//                return 240;
//            case 'ё':
//                return 241;
//            case 'Є':
//                return 242;
//            case 'є':
//                return 243;
//            case 'Ї':
//                return 244;
//            case 'ї':
//                return 245;
//            case 'Ў':
//                return 246;
//            case 'ў':
//                return 247;
//            case '°':
//                return 248;
//            case '∙':
//                return 249;
//            case '·':
//                return 250;
//            case '√':
//                return 251;
//            case '№':
//                return 252;
//            case '¤':
//                return 253;
//            case '■':
//                return 254;
//            case ' ':
//                return 255;
//        }
//        return 0;
//    }
}