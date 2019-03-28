package Utils;

import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class SessionServer extends Session{
    ServerSocket ss;
    Thread onStop;

    public SessionServer(Type type, int initPort, Runnable doOnStopSession) throws Exception {
        onStop = new Thread(doOnStopSession);
        ss = new ServerSocket(initPort);
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
            case MOUSE:
                t = new Thread(() -> {
                    try {
                        socket = ss.accept();
                        if(onStop!=null) onStop.run();
                        this.type = type;
                        this.address = ((InetSocketAddress)(socket.getRemoteSocketAddress())).getAddress();
                        broadcasting.cancel();
                        MouseTracker mt = new MouseTracker(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case SCREENMIRRORING:
                t = new Thread(() -> {
                    try {
                        socket = ss.accept();
                        if (onStop != null) onStop.run();
                        this.type = type;
                        this.address = ((InetSocketAddress) (socket.getRemoteSocketAddress())).getAddress();
                        broadcasting.cancel();
                        Robot r = new Robot();
                        new Timer().scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                    System.out.println("test");
                                    if(bufferedWriter != null) {
                                        BufferedImage image = r.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                                        System.out.println(ImageIO.write(image, "png", socket.getOutputStream()));
                                        socket.getOutputStream().flush();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 1, 1);
//                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                        while (true) {
//                            BufferedImage image = r.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
//                            System.out.println(ImageIO.write(image, "png", socket.getOutputStream()));
//                            socket.getOutputStream().flush();
//                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }
                });
                break;
            default:
                throw new Exception("Неизвестный тип сессии");
        }
    }

    public boolean isServer(){
        return true;
    }
}