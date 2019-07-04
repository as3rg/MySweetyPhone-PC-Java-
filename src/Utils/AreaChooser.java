package Utils;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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
    java.awt.Rectangle screenRect;
    Point first = null, second = null;

    public void start() {
        primaryStage = new Stage();
        root = new Group();
        Label label = new Label("Для сохранения настроек нажмите E̲n̲t̲e̲r̲\nДля сброса выбранной области нажмите E̲s̲c̲a̲pe̲\nДля выхода нажмите E̲s̲c̲a̲pe̲ еще раз");
        label.setTextFill(Color.BLACK);
        label.setPadding(new Insets(5,5,5,5));
        label.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        Scene scene = new Scene(root, width,height, Color.valueOf("#00000088"));
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

        root.getChildren().add(label);
        new AnimationTimer() {
            @Override
            public void handle(long l) {
                Point p = MouseInfo.getPointerInfo().getLocation();
                label.setLayoutX(25 + p.getX() - screenRect.getX());
                label.setLayoutY(p.getY() - 50 - screenRect.getY());
            }
        }.start();

        screenRect = new java.awt.Rectangle(0, 0, 0, 0);
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
            screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());
        primaryStage.setX(screenRect.getX());
        primaryStage.setY(screenRect.getY());
        primaryStage.setWidth(screenRect.getWidth());
        primaryStage.setHeight(screenRect.getHeight());
    }

    public void mousePressed(MouseEvent e) {
        if(first != null && second != null)
            first = second = null;
        if(first == null) {
            first = new Point((int) e.getX(), (int) e.getY());
            loop = new AnimationTimer() {
                @Override
                public void handle(long l) {
                    Point p2 = MouseInfo.getPointerInfo().getLocation();
                    Point p = new Point((int)(p2.getX() - screenRect.getX()), (int)(p2.getY() - screenRect.getY()));
                    r.setX(Math.min(first.getX(), p.getX()));
                    r.setY(Math.min(first.getY(), p.getY()));
                    r.setHeight(Math.abs(p.getY() - first.getY()));
                    r.setWidth(Math.abs(p.getX() - first.getX()));
                }
            };
            loop.start();
        }else{
            second = new Point((int) e.getX(), (int) e.getY());
            loop.stop();
            r.setHeight(Math.abs(second.getY() - first.getY()));
            r.setWidth(Math.abs(second.getX() - first.getX()));
        }
    }

    public void keyPressed(KeyEvent e) {
        if(e.getCode() == KeyCode.ENTER){
            primaryStage.close();
            MouseTracker.start = new Point((int)Math.min(first.getX()+screenRect.getX(), second.getX()+screenRect.getX()), (int)Math.min(first.getY()+screenRect.getY(), second.getY()+screenRect.getY()));
            MouseTracker.size = new Dimension((int)Math.abs(second.getX() - first.getX()),(int)Math.abs(second.getY() - first.getY()));
        }else if(e.getCode() == KeyCode.ESCAPE && first != null) {
            first = second = null;
            loop.stop();
            r.setHeight(0);
            r.setWidth(0);
        }else if(e.getCode() == KeyCode.ESCAPE){
            primaryStage.close();
        }
    }
}