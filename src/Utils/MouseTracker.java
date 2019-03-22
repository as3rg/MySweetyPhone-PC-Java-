package Utils;


import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.OutputStream;
import java.io.PrintWriter;

public class MouseTracker extends Frame implements MouseListener, MouseMotionListener {

    PrintWriter s;

    public MouseTracker(OutputStream s)
    {
        this.s = new PrintWriter(s);
        JFrame f = new JFrame("MouseListener");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        f.addMouseListener(this);
        f.addMouseMotionListener(this);
        f.show();
    }
    public void mousePressed(MouseEvent e)
    {
        JSONObject msg = new JSONObject();
        msg.put("Type","pressed");
        msg.put("X",e.getX());
        msg.put("Y",e.getY());
        msg.put("Key",e.getButton());
        s.println(msg.toJSONString());
    }

    public void mouseReleased(MouseEvent e)
    {
        JSONObject msg = new JSONObject();
        msg.put("Type","released");
        msg.put("X",e.getX());
        msg.put("Y",e.getY());
        msg.put("Key",e.getButton());
        s.println(msg.toJSONString());
    }

    public void mouseExited(MouseEvent e)
    {

    }

    public void mouseEntered(MouseEvent e)
    {

    }


    public void mouseClicked(MouseEvent e)
    {

    }

    public void mouseMoved(MouseEvent e)
    {
        JSONObject msg = new JSONObject();
        msg.put("Type","moved");
        msg.put("X",e.getX());
        msg.put("Y",e.getY());
        s.println(msg.toJSONString());
    }

    public void mouseDragged(MouseEvent e)
    {

    }
}