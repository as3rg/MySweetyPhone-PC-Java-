package Utils;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;

public class AreaChooser{

    Group root;
    Stage primaryStage;
    Rectangle r;
    AnimationTimer loop;
    Point first = null, second = null;

    public void start() {
        primaryStage = new Stage();
        root = new Group();
        Label label = new Label("Для сохранения настроек нажмите Enter\nДля отмены нажмите Escape");
        label.setTextFill(Color.WHITE);
        root.getChildren().add(label);
        Scene scene = new Scene(root, 1920,1080, Color.valueOf("#00000088"));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setAlwaysOnTop(true);

        primaryStage.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);
        primaryStage.setOnCloseRequest(Event::consume);

        r = new Rectangle(0, 0);
        root.getChildren().add(r);
        r.setFill(Color.valueOf("#42aaff88"));
    }

    public void mousePressed(MouseEvent e) {
        if(first != null && second != null)
            first = second = null;
        if(first == null) {
            first = new Point((int) e.getScreenX(), (int) e.getScreenY());
            loop = new AnimationTimer() {
                @Override
                public void handle(long l) {
                    PointerInfo info = MouseInfo.getPointerInfo();
                    Point p = info.getLocation();
                    r.setX(Math.min(first.getX(), p.getX()));
                    r.setY(Math.min(first.getY(), p.getY()));
                    r.setHeight(Math.abs(p.getY() - first.getY()));
                    r.setWidth(Math.abs(p.getX() - first.getX()));
                }
            };
            loop.start();
        }else{
            second = new Point((int)e.getScreenX(), (int)e.getScreenY());
            loop.stop();
            r.setHeight(Math.abs(second.getY() - first.getY()));
            r.setWidth(Math.abs(second.getX() - first.getX()));
        }
    }

    public void keyPressed(KeyEvent e) {
        if(e.getCode() == KeyCode.ENTER){
            primaryStage.close();
            MouseTracker.start = new Point((int)Math.min(first.getX(), second.getX()), (int)Math.min(first.getY(), second.getY()));
            MouseTracker.size = new Dimension((int)Math.abs(second.getX() - first.getX()),(int)Math.abs(second.getY() - first.getY()));
        }else if(e.getCode() == KeyCode.ESCAPE){
            primaryStage.close();
        }
    }
}