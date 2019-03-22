package Utils;

import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class MouseTracker extends Frame implements MouseListener, MouseMotionListener, MouseWheelListener {

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
        f.add(p);

        f.show();
    }
    public void mousePressed(MouseEvent e)
    {
        JSONObject msg = new JSONObject();
        msg.put("Type","pressed");
        msg.put("X",e.getX()/width);
        msg.put("Y",e.getY()/height);
        msg.put("Key",e.getButton());
        outputStream.println(msg.toJSONString());
    }

    public void mouseReleased(MouseEvent e)
    {
        JSONObject msg = new JSONObject();
        msg.put("Type","released");
        msg.put("X",e.getX()/width);
        msg.put("Y",e.getY()/height);
        msg.put("Key",e.getButton());
        outputStream.println(msg.toJSONString());
    }

    public void mouseExited(MouseEvent e) {}

    public void mouseEntered(MouseEvent e){}

    public void mouseClicked(MouseEvent e){}

    public void mouseMoved(MouseEvent e){
        JSONObject msg = new JSONObject();
        msg.put("Type","moved");
        msg.put("X",e.getX()/width);
        msg.put("Y",e.getY()/height);
        outputStream.println(msg.toJSONString());
    }

    public void mouseDragged(MouseEvent e){
        JSONObject msg = new JSONObject();
        msg.put("Type","moved");
        msg.put("X",e.getX()/width);
        msg.put("Y",e.getY()/height);
        outputStream.println(msg.toJSONString());
    }

    public void mouseWheelMoved(MouseWheelEvent e){
        JSONObject msg = new JSONObject();
        msg.put("Type","wheel");
        msg.put("value",e.getWheelRotation());
        outputStream.println(msg.toJSONString());
    }
} 