package Utils;

import Utils.Message;
import Utils.MessageParser;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Optional;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class Receiving {
    private static final int PORT = 9500;
    private static final int MESSAGESIZE = Message.BODYMAXIMUM/10;
    MessageParser messageParser;
    Thread broadcasting;
    Thread t;
    DatagramSocket socket;
    public Receiving() throws SocketException, UnknownHostException {
        messageParser = new MessageParser();
        JSONObject message = new JSONObject();
        socket = new DatagramSocket();
        message.put("Port", socket.getLocalPort());
        message.put("Type", "receiving");
        byte[] buf2 = String.format("%-100s", message.toJSONString()).getBytes();
        DatagramSocket s = new DatagramSocket();
        s.setBroadcast(true);
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length, Inet4Address.getByName("255.255.255.255"), PORT);
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    s.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        broadcasting = new Thread(()->timer.scheduleAtFixedRate(timerTask,0,2000));

        t = new Thread(()->{

            try {
                socket.setBroadcast(true);
                DatagramPacket p;
                while (!socket.isClosed()) {
                    Message m = null;
                    int head = -1;
                    p = null;
                    do{
                        byte[] buf = new byte[Message.getMessageSize(MESSAGESIZE)];
                        p = new DatagramPacket(buf, buf.length);
                        try {
                            socket.receive(p);
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
                    Platform.runLater(()-> {
                        try {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Выполнить действие?");
                            if (msg.containsKey("Type")) switch ((String) msg.get("Type")) {
                                case "openSite":
                                    alert.setHeaderText("Вы действительно хотите перейти по ссылке от \"" + msg.get("Name") + "\"?");
                                    break;
                                case "copy":
                                    alert.setHeaderText("Вы действительно хотите пройти поместить данные от \"" + msg.get("Name") + "\" в буфер обмена?");
                                    break;
                            }

                            Optional<ButtonType> option = null;

                            File file = new File("properties.properties");
                            FileInputStream propFile = new FileInputStream(file);
                            Properties props = new Properties();
                            props.load(propFile);
                            String login = (String) props.getOrDefault("login", "");
                            propFile.close();

                            if(!msg.containsKey("Login") || !msg.get("Login").equals(login)) option = alert.showAndWait();

                            if ((msg.containsKey("Login") && msg.get("Login").equals(login)) || option.get() == ButtonType.OK)
                                switch ((String) msg.get("Type")) {
                                    case "openSite":
                                        Desktop.getDesktop().browse(new URL((String) msg.get("Site")).toURI());
                                        break;
                                    case "copy":
                                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                        clipboard.setContents(new StringSelection((String) msg.get("Value")), null);
                                        break;
                                }
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void Start(){
        t.start();
        broadcasting.start();
    }
}
