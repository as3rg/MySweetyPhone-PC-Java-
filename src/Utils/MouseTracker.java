package Utils;

import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramPacket;

public class MouseTracker extends Frame implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    static int startX = 100,startY = 100;
    SessionClient ss;
    double width;
    double height;
    JFrame f;
    static Robot r;
    static {
        try {
            r = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    public MouseTracker(SessionClient ss) throws IOException, AWTException {
        this.ss = ss;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = screenSize.getWidth();
        height = screenSize.getHeight();
        f = new JFrame("MouseListener");
        f.setSize(600, 100);
        f.setAlwaysOnTop(true);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        f.setUndecorated(true);
        f.setVisible(true);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        JLabel icon = new JLabel(new ImageIcon("src/sample/Images/Icon.png"));
        p.add(icon);
        f.setBackground(Color.getColor("#202020"));
        f.addMouseListener(this);
        f.addMouseMotionListener(this);
        f.addMouseWheelListener(this);
        f.addKeyListener(this);
        f.add(p);
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(new byte[0]).getImage(),new Point(0,0), "custom"));
        f.show();
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        try {
            JSONObject msg = new JSONObject();
            msg.put("Type","mousePressed");
            msg.put("Key",e.getButton());
            Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
            for(Message m : messages){
                    ss.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,ss.getAddress(),ss.getPort()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        try{
            JSONObject msg = new JSONObject();
            msg.put("Type","mouseReleased");
            msg.put("Key",e.getButton());
            Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
            for(Message m : messages){
                ss.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,ss.getAddress(),ss.getPort()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e){}

    @Override
    public void mouseClicked(MouseEvent e){}

    @Override
    public void mouseMoved(MouseEvent e){
        try {
            if (e.getX() != startX || e.getY() != startY) {
                JSONObject msg = new JSONObject();
                msg.put("Type", "mouseMoved");
                msg.put("X", e.getX() - startX);
                msg.put("Y", e.getY() - startY);
                Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
                for(Message m : messages){
                    ss.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,ss.getAddress(),ss.getPort()));
                }
                r.mouseMove(startX, startY);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e){
        try{
            JSONObject msg = new JSONObject();
            msg.put("Type","mouseMoved");
            msg.put("X",e.getX()-startX);
            msg.put("Y",e.getY()-startY);
            Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
            for(Message m : messages){
                ss.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,ss.getAddress(),ss.getPort()));
            }
            r.mouseMove(startX, startY);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e){
        try{
            JSONObject msg = new JSONObject();
            msg.put("Type","mouseWheel");
            msg.put("value",e.getWheelRotation());
            Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
            for(Message m : messages){
                ss.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,ss.getAddress(),ss.getPort()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        try {
            JSONObject msg = new JSONObject();
            if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_F4){
                msg.put("Type", "finish");
                Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
                for(Message m : messages){
                    ss.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,ss.getAddress(),ss.getPort()));
                }
                f.dispose();
                Thread.sleep(100);
                ss.Stop();
            }else if(e.isAltDown() && e.getKeyCode() == KeyEvent.VK_S) {
                msg.put("Type", "swap");
                Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
                for(Message m : messages){
                    ss.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,ss.getAddress(),ss.getPort()));
                }
                ss.socket.close();
                Thread.sleep(1000);
                SessionClient sc = new SessionClient(ss.address,ss.port,ss.type);
                Session.sessions.add(sc);
                Session.sessions.remove(this);
                sc.Start();
                f.dispose();
            }else{
                msg.put("Type", "keyPressed");
                msg.put("value", e.getKeyCode());
                Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
                for(Message m : messages){
                    ss.getSocket().send(new DatagramPacket(m.getArr(),m.getArr().length,ss.getAddress(),ss.getPort()));
                }
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("Type", "keyReleased");
            msg.put("value", e.getKeyCode());
            Message[] messages = Message.getMessages(msg.toJSONString().getBytes());
            for (Message m : messages) {
                ss.getSocket().send(new DatagramPacket(m.getArr(), m.getArr().length, ss.getAddress(), ss.getPort()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}