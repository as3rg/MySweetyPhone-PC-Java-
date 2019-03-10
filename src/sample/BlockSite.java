package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class BlockSite {

    public static class Site{
        private String Name;
        private Button Unlock;
        private Button Remove;

        public Site(String name) {
            Name = name;
            Unlock = new Button();
            Remove = new Button();
        }

        public Button getUnlock(){
            return Unlock;
        }

        public void setUnlock(Button b){
            Unlock = b;
        }

        public String getName(){
            return Name;
        }

        public Button getRemove(){
            return Remove;
        }

        public void setRemove(Button b){
            Remove = b;
        }
    }

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane MainPane;

    @FXML
    private TableColumn<Site, Void> Unlock;

    @FXML
    private TableColumn<Site, String> Name;

    @FXML
    private TableView Table;

    @FXML
    private TableColumn<Site, Void> Remove;

    private String name;
    private String login;
    private int id;
    private int regdate;

    ObservableList<Site> sites;

    @FXML
    public void initialize() throws IOException {
        sites = FXCollections.observableArrayList();
        sites.addAll(GetBlockedSites());

        Name.setCellValueFactory(new PropertyValueFactory<Site, String>("Name"));

        Callback<TableColumn<Site, Void>, TableCell<Site, Void>> removeCellFactory = new Callback<TableColumn<Site, Void>, TableCell<Site, Void>>() {
            @Override
            public TableCell<Site, Void> call(TableColumn<Site, Void> param) {
                final TableCell<Site, Void> cell = new TableCell<Site, Void>(){
                    javafx.scene.control.Button button = new javafx.scene.control.Button("Удалить");
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        button.setOnMouseClicked(event -> {
                            try {
                                UnblockSite(Name.getCellData(getIndex()));
                                sites.remove(getIndex());
                            }catch (Exception e){
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Ошибка");
                                alert.setHeaderText(null);
                                alert.setContentText(e.toString());
                                alert.setOnCloseRequest(event2 -> Platform.exit());
                                alert.show();
                                e.printStackTrace();
                            }
                        });
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(button);
                        }
                    }
                };
                return cell;
            }
        };

        Callback<TableColumn<Site, Void>, TableCell<Site, Void>> unlockCellFactory = new Callback<TableColumn<Site, Void>, TableCell<Site, Void>>() {
            @Override
            public TableCell<Site, Void> call(TableColumn<Site, Void> param) {
                final TableCell<Site, Void> cell = new TableCell<Site, Void>(){
                    Button button = new javafx.scene.control.Button("Разблокировать");
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        button.setOnMouseClicked(event -> {
                            try {
                                if(UnblockSite(Name.getCellData(getIndex()))) {
                                    button.setDisable(true);
                                    sites.get(getIndex()).getRemove().setDisable(true);
                                    AtomicReference<Long> time = new AtomicReference<>(9L);
                                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), arg0 -> {
                                        try {
                                            time.set(time.get() - 1);
                                            button.setText(Long.toString(time.get() / 60) + ':' + Long.toString(time.get() % 60));
                                            if (time.get().equals(0L) && Name.getCellData(getIndex()) != null) {
                                                if (!BlockSite(Name.getCellData(getIndex()))) {
                                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                                    alert.setTitle("Ошибка");
                                                    alert.setHeaderText(null);
                                                    alert.setContentText("Нужно Ваше разрешение для завершения процесса");
                                                    alert.setOnCloseRequest(event2 -> {
                                                        try {
                                                            BlockSite(Name.getCellData(getIndex()));
                                                        } catch (Exception e) {
                                                            alert.setTitle("Ошибка");
                                                            alert.setHeaderText(null);
                                                            alert.setContentText(e.toString());
                                                            alert.setOnCloseRequest(event4 -> Platform.exit());
                                                            alert.show();
                                                            e.printStackTrace();
                                                        }
                                                    });
                                                    alert.show();
                                                    button.setText("Разблокировать");
                                                    button.setDisable(false);
                                                }
                                            }
                                        } catch (Exception e) {
                                            Alert alert = new Alert(Alert.AlertType.ERROR);
                                            alert.setTitle("Ошибка");
                                            alert.setHeaderText(null);
                                            alert.setContentText(e.toString());
                                            alert.setOnCloseRequest(event2 -> Platform.exit());
                                            alert.show();
                                            e.printStackTrace();
                                        }
                                    }));
                                    timeline.setCycleCount(time.get().intValue());
                                    timeline.play();
                                }else{
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Ошибка");
                                    alert.setHeaderText(null);
                                    alert.setContentText("Нужно Ваше разрешение");
                                    alert.show();
                                }
                            }catch (Exception e){
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Ошибка");
                                alert.setHeaderText(null);
                                alert.setContentText(e.toString());
                                alert.setOnCloseRequest(event2 -> Platform.exit());
                                alert.show();
                                e.printStackTrace();
                            }
                        });
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(button);
                        }
                    }
                };
                return cell;
            }
        };

        Remove.setCellFactory(removeCellFactory);
        Unlock.setCellFactory(unlockCellFactory);

        Table.setItems(sites);
    }

    private static boolean UnblockSite(String url) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
                "Start-Process java -ArgumentList '-cp " + System.getProperty("user.dir").replace("\\", "\\\\") + " BlockSiteClass --UnblockSite "+url+"' -Verb RunAs");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        p.waitFor();
        return p.exitValue() == 0;
    }

    private static boolean BlockSite(String url) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("powershell.exe", "/c",
                "Start-Process java -ArgumentList '-cp " + System.getProperty("user.dir").replace("\\", "\\\\") + " BlockSiteClass --BlockSite "+url+"' -Verb RunAs");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        p.waitFor();
        return p.exitValue() == 0;
    }

    private static ArrayList<Site> GetBlockedSites() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("powershell.exe", "/c",
                "java -cp " + System.getProperty("user.dir").replace("\\", "\\\\") + " BlockSiteClass --GetBlockedSites");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        ArrayList<Site> lines = new ArrayList<Site>();
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            if (!line.contains("#"))
                lines.add(new Site(line.split(" ")[1]));
        }
        return lines;
    }
}
