package Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

class Listener implements MouseMotionListener, MouseListener {

    int x = -1;
    int y = -1;

    JFrame j;

    Listener(JFrame j){
        this.j = j;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        x = e.getPoint().x;
        y = e.getPoint().y;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        j.setLocation(j.getLocation().x - x + e.getPoint().x, j.getLocation().y - y + e.getPoint().y);

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}

public class MouseTrackerBar {

    JFrame f;
    JGradientButton SwapButton;
    JGradientButton FinishButton;

    MouseTrackerBar() {
        f = new JFrame("MouseListener");
        f.setSize(50, 65);
        f.setAlwaysOnTop(true);
        f.setUndecorated(true);
        f.setVisible(true);
        JPanel p = new JPanel();
        Listener l = new Listener(f);
        f.addMouseMotionListener(l);
        f.addMouseListener(l);
        SwapButton = new JGradientButton("  Сменить  ");
        FinishButton = new JGradientButton("Закончить");
        p.add(SwapButton);
        p.add(FinishButton);
        p.setLayout(new FlowLayout());
        f.setBackground(Color.getColor("#202020"));
        f.add(p);
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    void Show(){
        f.show();
        int maxWidth = Math.max(SwapButton.getWidth(), FinishButton.getWidth());
        f.setSize(maxWidth, f.getHeight());
    }
}
