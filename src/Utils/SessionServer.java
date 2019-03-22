package Utils;

import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.Timer;
import java.util.TimerTask;

public class SessionServer extends Session{

    public SessionServer(Type type) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        this.port = ss.getLocalPort();
        JSONObject message = new JSONObject();
        message.put("port", port);
        message.put("type", type.ordinal());
        byte[] buf = String.format("%-30s", message.toJSONString()).getBytes();
        DatagramSocket s = new DatagramSocket();
        s.setBroadcast(true);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, Inet4Address.getByName("255.255.255.255"), BroadCastingPort);

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
        broadcasting.schedule(broadcastingTask, 2000, 2000);

        switch (type) {
            case TEST:
                t = new Thread(() -> {
                    try {
                        socket = ss.accept();
                        this.type = type;
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                        broadcasting.cancel();
                        while (true)
                            writer.println("test");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case MOUSE:
                t = new Thread(() -> {
                    try {
                        socket = ss.accept();
                        this.type = type;
                        broadcasting.cancel();
                        MouseTracker mt = new MouseTracker(socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                MouseTracker mt = new MouseTracker(System.out);
                break;
            default:
                throw new Exception("Неизвестный тип сессии");
        }
    }

    public boolean isServer(){
        return true;
    }
}