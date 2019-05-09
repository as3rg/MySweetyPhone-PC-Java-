package sample;

import Utils.Message;
import Utils.MessageParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class Receiving {
    private static final int PORT = 950;
    private static final int MESSAGESIZE = Message.BODYMAXIMUM;
    private static final int BROADCASTINGSIZE = 100;
    MessageParser messageParser;
    Thread broadcasting;
    Thread t;
    DatagramSocket socket;
    Receiving() throws SocketException, UnknownHostException {
        messageParser = new MessageParser();
        JSONObject message = new JSONObject();
        message.put("port", PORT);
        message.put("type", "receiving");
        Message[] messages = Message.getMessages(message.toJSONString().getBytes(), BROADCASTINGSIZE);
        DatagramSocket s = new DatagramSocket();
        s.setBroadcast(true);
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    for(Message m : messages)
                        s.send(new DatagramPacket(m.getArr(), m.getArr().length, Inet4Address.getByName("255.255.255.255"), PORT));
                    System.out.println("sending");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        broadcasting = new Thread(()->timer.scheduleAtFixedRate(timerTask,0,2000));

        t = new Thread(()->{

            try {
                socket = new DatagramSocket();
                System.out.println(socket.getLocalPort());
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
                    if(msg.containsKey("type")) switch ((String)msg.get("type")){
                        case "openSite":
                            Desktop.getDesktop().browse(new URI((String)msg.get("site")));
                            break;
                    }
                }
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    public void Start(){
        t.start();
        broadcasting.start();
    }
}
