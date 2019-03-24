package Utils;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;

public class SessionClient extends Session{

    static ArrayList<SessionClient> servers;
    static ArrayList<InetAddress> ips;
    static boolean isSearching;
    static Thread searching;
    static DatagramSocket s;

    static{
        isSearching = false;
    }

    public static void Search(Pane v, Thread onFinishSearching) throws SocketException {
        v.getChildren().clear();
        if(isSearching) {
            System.err.println("Поиск уже запущен");
            return;
        }
        servers = new ArrayList<>();
        ips = new ArrayList<>();
        isSearching = true;
        s = new DatagramSocket(BroadCastingPort);
        s.setBroadcast(true);
        s.setSoTimeout(60000);
        byte[] buf = new byte[30];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        long time = (new Date()).getTime();
        searching = new Thread(() -> {
            try {
                while ((new Date()).getTime() - time <= 60000) {
                    s.receive(p);
                    JSONObject ans = (JSONObject) JSONValue.parse(new String(p.getData()));
                    if (!ips.contains(p.getAddress())) {
                        ips.add(p.getAddress());
                        servers.add(new SessionClient(p.getAddress(),((Long)ans.get("port")).intValue(), Type.values()[((Long)ans.get("type")).intValue()]));
                        Platform.runLater(() -> {
                            Button ip = new Button(p.getAddress().getHostAddress());
                            ip.setTextFill(Paint.valueOf("#F0F0F0"));
                            ip.setOnMouseClicked(event->{
                                servers.get(v.getChildren().indexOf(ip)).Start();
                                v.getChildren().remove(ip);
                            });
                            v.setDisable(false);
                            v.getChildren().add(ip);
                        });
                    }
                }
            } catch (SocketException e){
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            isSearching = false;
            s.close();
            Platform.runLater(onFinishSearching);
        });
        searching.start();
    }

    public static void StopSearching() {
        searching.interrupt();
        isSearching=false;
        s.close();
    }

    public SessionClient(InetAddress address, int port, Type type) throws Exception {
        this.address = address;
        this.port = port;
        this.type = type;
        switch (type) {
            case MOUSE:
                t = new Thread(() -> {
                    try {
                        socket = new Socket(address, port);
                        socket.setSoTimeout(60000);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        if(searching != null) StopSearching();
                        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                        double width = screenSize.getWidth();
                        double height = screenSize.getHeight();
                        Robot r = new Robot();
                        while (true) {
                            JSONObject msg = (JSONObject)JSONValue.parse(reader.readLine());
                            switch ((String)msg.get("Type")){
                                case "mouseMoved":
                                    r.mouseMove((int)(((Double)msg.get("X")).doubleValue() * width), (int)(((Double)msg.get("Y")).doubleValue() * height));
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
                                    SessionServer ss = new SessionServer(type,port,()->{});
                                    socket.close();
                                    Session.sessions.add(ss);
                                    Session.sessions.remove(this);
                                    ss.Start();
                                    return;
                                case "finish":
                                    r.keyRelease(KeyEvent.VK_ALT);
                                    Stop();
                                    return;
                            }

                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (AWTException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
                break;
            case SCREENMIRRORING:
                t = new Thread(() -> {
                    try {
                        socket = new Socket(address, port);
                        socket.setSoTimeout(60000);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        if(searching != null) StopSearching();
                        JFrame f = new JFrame("MouseListener");
                        f.setSize(600, 100);
                        f.setAlwaysOnTop(true);
                        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        f.setUndecorated(true);
                        f.setVisible(true);
                        JPanel p = new JPanel();
                        p.setLayout(new FlowLayout());
                        JLabel icon = new JLabel();
                        p.add(icon);
                        icon.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                        f.setBackground(Color.getColor("#202020"));
                        f.add(p);
                        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        f.show();
                        Robot r = new Robot();

                        while (true) {
                            BufferedImage bufferedWriter = ImageIO.read(socket.getInputStream());
                            if(bufferedWriter != null) {
                                ImageIcon ii = new ImageIcon(bufferedWriter);
                                icon.setIcon(ii);
                            }
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
                break;
            default:
                throw new Exception("Неизвестный тип сессии");
        }
    }

    public boolean isServer(){
        return false;
    }
}