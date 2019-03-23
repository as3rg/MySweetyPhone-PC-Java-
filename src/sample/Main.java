package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;

class Listener implements MouseMotionListener, MouseListener{

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

public class Main extends Application {

    static ArrayList<File> tempfiles;
    @Override
    public void start(Stage primaryStage) throws Exception{
        tempfiles = new ArrayList<>();
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        primaryStage.setTitle("MySweetyPhone");
        Scene scene = new Scene(root,1270,720 );
        scene.getStylesheets().add(Main.class.getResource("Style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("Images/Icon.png")));
        primaryStage.setOnCloseRequest(event -> Platform.exit());
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception{
        for(File f : tempfiles) {
            f.deleteOnExit();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        JFrame f = new JFrame("MouseListener");
        f.setSize(50,65);
        f.setAlwaysOnTop(true);
        f.setUndecorated(true);
        f.setVisible(true);
        JPanel p = new JPanel();
        Listener l = new Listener(f);
        f.addMouseMotionListener(l);
        f.addMouseListener(l);
        JButton SwapButton = new JButton("Swap");
        JButton FinishButton = new JButton("Finish");
        GradientPaint gp = new GradientPaint(0, 0, Color.BLACK, 0, SwapButton.getHeight(), Color.WHITE);
        p.add(SwapButton);
        p.add(FinishButton);
        p.setLayout(new FlowLayout());
        f.setBackground(Color.getColor("#202020"));
        f.add(p);
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        f.show();


        launch(args);
    }
}
