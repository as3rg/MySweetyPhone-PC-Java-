package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {

    static ArrayList<File> tempfiles;
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

        TrayIcon icon = new TrayIcon(ImageIO.read(getClass().getResourceAsStream("Images/TrayIcon.png")));
        SystemTray systemTray = SystemTray.getSystemTray();
        PopupMenu menu = new PopupMenu();

        primaryStage.setOnCloseRequest(windowEvent -> {
            primaryStage.hide();
        });
        icon.addActionListener(event-> Platform.runLater(()-> {
            if(primaryStage.isShowing())
                primaryStage.hide();
            else
                primaryStage.show();
        }));
        MenuItem Show = new MenuItem("Свернуть/Развернуть");
        Show.addActionListener(icon.getActionListeners()[0]);
        MenuItem Close = new MenuItem("Закрыть");
        Close.addActionListener(actionEvent -> {
            Platform.exit();
            System.exit(0);
        });
        menu.add(Close);
        menu.add(Show);

        icon.setPopupMenu(menu);

        systemTray.add(icon);
    }

    @Override
    public void stop() {
        if(tempfiles != null)
            for(File f : tempfiles) {
                f.deleteOnExit();
            }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
