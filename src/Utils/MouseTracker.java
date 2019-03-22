package Utils;

import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class MouseTracker extends Frame implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    PrintWriter outputStream;
    double width;
    double height;
    public MouseTracker(SessionServer ss) throws IOException {
        outputStream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(ss.getSocket().getOutputStream())), true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = screenSize.getWidth();
        height = screenSize.getHeight();
        JFrame f = new JFrame("MouseListener");
        f.setSize(600, 100);
        f.setAlwaysOnTop(true);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        f.setUndecorated(true);
        f.setVisible(true);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        f.addMouseListener(this);
        f.addMouseMotionListener(this);
        f.addKeyListener(this);
        f.add(p);

        f.show();
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        JSONObject msg = new JSONObject();
        msg.put("Type","mousePressed");
        msg.put("X",e.getX()/width);
        msg.put("Y",e.getY()/height);
        msg.put("Key",e.getButton());
        outputStream.println(msg.toJSONString());
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        JSONObject msg = new JSONObject();
        msg.put("Type","mouseReleased");
        msg.put("X",e.getX()/width);
        msg.put("Y",e.getY()/height);
        msg.put("Key",e.getButton());
        outputStream.println(msg.toJSONString());
    }

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e){}

    @Override
    public void mouseClicked(MouseEvent e){}

    @Override
    public void mouseMoved(MouseEvent e){
        JSONObject msg = new JSONObject();
        msg.put("Type","mouseMoved");
        msg.put("X",e.getX()/width);
        msg.put("Y",e.getY()/height);
        outputStream.println(msg.toJSONString());
    }

    @Override
    public void mouseDragged(MouseEvent e){
        JSONObject msg = new JSONObject();
        msg.put("Type","mouseMoved");
        msg.put("X",e.getX()/width);
        msg.put("Y",e.getY()/height);
        outputStream.println(msg.toJSONString());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e){
        JSONObject msg = new JSONObject();
        msg.put("Type","mouseWheel");
        msg.put("value",e.getWheelRotation());
        outputStream.println(msg.toJSONString());
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        JSONObject msg = new JSONObject();
        msg.put("Type","keyPressed");
        msg.put("value",e.getKeyCode());
        outputStream.println(msg.toJSONString());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        JSONObject msg = new JSONObject();
        msg.put("Type","keyReleased");
        msg.put("value",e.getKeyCode());
        outputStream.println(msg.toJSONString());
    }
}