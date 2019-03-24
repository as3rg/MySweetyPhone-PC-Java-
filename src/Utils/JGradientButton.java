package Utils;

import javax.swing.*;
import java.awt.*;

public class JGradientButton extends JButton {
    public JGradientButton(String s) {
        super(s);
        setContentAreaFilled(false);
        setFocusPainted(false); // used for demonstration
    }

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(
                new Point(0, 0),
                Color.decode("#d53369"),
                new Point(getWidth(), getHeight()),
                Color.decode("#cbad6d")));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();

        super.paintComponent(g);
    }

    public static JGradientButton newInstance() {
        return new JGradientButton("");
    }
}