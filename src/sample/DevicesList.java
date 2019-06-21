package sample;

import Utils.Request;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Callback;
import org.apache.http.entity.mime.MultipartEntity;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

public class DevicesList {

    public class Device{
        private String Name;
        //ImageView Type;
        private String Type;
        private Button Remove;

        public Device(String name, boolean isPhone) {
            Name = name;
            Type = isPhone ? "Phone" : "PC";
            Remove = new Button();
        }

        public String getType(){
            return Type;
        }

        public String getName(){
            return Name;
        }

        public Button getRemove(){
            return Remove;
        }
    }

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane MainPane;

    @FXML
    private TableColumn<Device, String> Type;

    @FXML
    private TableColumn<Device, String> Name;

    @FXML
    private TableView Table;

    @FXML
    private TableColumn<Device, Void> Remove;


    ObservableList<Device> devices;

    private String name;
    private String login;
    private int id;
    private int regdate;

    @FXML
    public void initialize() throws IOException {
        Thread Resize = new Thread(()->{
            try {
                while (MainPane.getScene() == null || MainPane.getScene().getWindow() == null) Thread.sleep(100);
                Type.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty().divide(7));
                Remove.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty().divide(4));
                Name.prefWidthProperty().bind(MainActivity.controller.Replace.widthProperty().divide(5).multiply(3));
                Table.prefHeightProperty().bind(MainActivity.controller.Replace.heightProperty());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Resize.start();
        devices = FXCollections.observableArrayList();
        FileInputStream propFile = new FileInputStream("properties.properties");
        Properties props = new Properties();
        props.load(propFile);
        propFile.close();
        id = Integer.parseInt((String)props.getOrDefault("id","-1"));
        regdate = Integer.parseInt((String)props.getOrDefault("regdate","-1"));
        login = (String)props.getOrDefault("login","");
        name = (String)props.getOrDefault("name","");

        Request request = new Request() {
            @Override
            protected void On0() {

            }

            @Override
            protected void On1() {

            }

            @Override
            protected void On2() {

            }

            @Override
            protected void On3() {

            }

            @Override
            protected void On4() {

            }
        };
        request.Start("http://mysweetyphone.herokuapp.com/?Type=Check&DeviceType=PC&RegDate="+regdate+"&Login=" + URLEncoder.encode(login, "UTF-8") + "&Id=" + id + "&Name=" + URLEncoder.encode(name, "UTF-8"), new MultipartEntity());

        Runnable r = () -> {
            Request request2 = new Request() {
                @Override
                protected void On0() {
                    for (String device : (ArrayList<String>) result.get("PCs")) {
                        devices.add(new Device(device, false));
                    }
                    for (String device : (ArrayList<String>) result.get("Phones")) {
                        devices.add(new Device(device, true));
                    }
                }

                @Override
                protected void On1() {

                }

                @Override
                protected void On2() {

                }

                @Override
                protected void On3() {

                }

                @Override
                protected void On4() {
                    throw new RuntimeException("Ваше устройство не зарегистрировано!");
                }
            };
            try {
                request2.Start("http://mysweetyphone.herokuapp.com/?Type=ShowDevices&RegDate=" + regdate + "&Login=" + URLEncoder.encode(login, "UTF-8") + "&Id=" + id + "&MyName=" + URLEncoder.encode(name, "UTF-8"), new MultipartEntity());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();


        Type.setCellValueFactory(new PropertyValueFactory<Device, String>("Type"));
        Name.setCellValueFactory(new PropertyValueFactory<Device, String>("Name"));

        Callback<TableColumn<Device, Void>, TableCell<Device, Void>> cellFactory = new Callback<TableColumn<Device, Void>, TableCell<Device, Void>>() {
            @Override
            public TableCell<Device, Void> call(final TableColumn<Device, Void> param) {
                final TableCell<Device, Void> cell = new TableCell<Device, Void>() {

                    Button button = new Button("Удалить");
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        button.setTextFill(Paint.valueOf("#FFFFFF"));
                        button.setFont(new Font( 15));

                        if (devices.size() > getIndex() && getIndex() != -1)
                            if (name.equals(devices.get(getIndex()).getName())){
                                button.setTextFill(Paint.valueOf("linear-gradient(from 0% 0% to 100% 100%, #FC354C, #0ABFBC)"));
                                button.setStyle("-fx-background-color: #ffffff");
                            }else{
                                button.setTextFill(Paint.valueOf("#ffffff"));
                                button.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #d53369, #cbad6d)");
                            }
                        button.setOnMouseClicked(event -> {
                            Runnable r = () -> {
                                Request request = new Request() {
                                    @Override
                                    protected void On0() {

                                    }

                                    @Override
                                    protected void On1() {

                                    }

                                    @Override
                                    protected void On2() {

                                    }

                                    @Override
                                    protected void On3() {

                                    }

                                    @Override
                                    protected void On4() {

                                    }
                                };
                                try {
                                    request.Start("http://mysweetyphone.herokuapp.com/?Type=RemoveDevice&RegDate=" + regdate + "&Login=" + URLEncoder.encode(login, "UTF-8") + "&MyName=" + URLEncoder.encode(name, "UTF-8") + "&Id=" + id + "&Name=" + URLEncoder.encode(devices.get(getIndex()).getName(), "UTF-8"), new MultipartEntity());
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                if (name.equals(devices.get(getIndex()).getName())) {
                                    devices.remove(getIndex());
                                    throw new RuntimeException("Ваше устройство не зарегистрировано!");
                                }
                                devices.remove(getIndex());
                            };
                            Thread t = new Thread(r);
                            t.start();
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

        Remove.setCellFactory(cellFactory);

        Table.setItems(devices);
    }
}
