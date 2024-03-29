package sample;

import Utils.AreaChooser;
import Utils.ServerMode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Main extends Application {

    public static TrayIcon trayIcon;

    public static ArrayList<File> tempfiles;
    @Override
    public void start(Stage primaryStage) throws Exception{
        tempfiles = new ArrayList<>();
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        primaryStage.setTitle("MySweetyPhone");
        Scene scene = new Scene(root,1280,720);
        scene.getStylesheets().add(Main.class.getResource("Style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("Images/Icon.png")));

        primaryStage.setMinHeight(760);
        primaryStage.setMinWidth(500);
        Platform.setImplicitExit(false);
        primaryStage.show();

        trayIcon = new TrayIcon(ImageIO.read(getClass().getResourceAsStream("Images/TrayIcon.png")), "MySweetyPhone");
        SystemTray systemTray = SystemTray.getSystemTray();
        PopupMenu menu = new PopupMenu();

        primaryStage.setOnCloseRequest(windowEvent -> {
            primaryStage.hide();
        });
        trayIcon.addActionListener(event-> Platform.runLater(()-> {
            if(primaryStage.isShowing())
                primaryStage.hide();
            else
                primaryStage.show();
        }));
        MenuItem Show = new MenuItem("Свернуть/Развернуть");
        Show.addActionListener(trayIcon.getActionListeners()[0]);
        MenuItem Close = new MenuItem("Закрыть");
        Close.addActionListener(actionEvent -> {
            try {
                stop();
                Platform.exit();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        MenuItem DrawingZone = new MenuItem("Выбрать Область для Граф.Планшета");
        DrawingZone.addActionListener(actionEvent -> Platform.runLater(new AreaChooser()::start));
        menu.add(DrawingZone);
        menu.add(Show);
        menu.add(Close);

        trayIcon.setPopupMenu(menu);

        systemTray.add(trayIcon);
    }

    @Override
    public void stop() throws IOException {
        SystemTray systemTray = SystemTray.getSystemTray();
        systemTray.remove(trayIcon);

        FileInputStream propFileIn = new FileInputStream("properties.properties");
        Properties props = new Properties();
        props.load(propFileIn);
        propFileIn.close();

        FileOutputStream propFileOut = new FileOutputStream("properties.properties");
        props.setProperty("serverMode", Boolean.toString(ServerMode.getState()));
        props.store(propFileOut, null);
        propFileOut.close();

        if (tempfiles != null)
            for (File f : tempfiles) {
                f.deleteOnExit();
            }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
