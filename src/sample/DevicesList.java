package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        devices = FXCollections.observableArrayList();
        FileInputStream propFile = new FileInputStream(location.getPath()+"/../../properties.properties");
        Properties props = new Properties();
        props.load(propFile);
        propFile.close();
        id = Integer.parseInt((String)props.getOrDefault("id","-1"));
        regdate = Integer.parseInt((String)props.getOrDefault("regdate","-1"));
        login = (String)props.getOrDefault("login","");
        name = (String)props.getOrDefault("name","");
        URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=Check&DeviceType=PC&RegDate="+regdate+"&Login=" + login + "&Id=" + id + "&Name=" + name);

        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Runnable r = () -> {
            try {
                URL obj2 = new URL("http://mysweetyphone.herokuapp.com/?Type=ShowDevices&RegDate="+regdate+"&Login=" + login + "&Id=" + id + "&MyName=" + name);

                HttpURLConnection connection2 = (HttpURLConnection) obj2.openConnection();
                connection2.setRequestMethod("GET");

                BufferedReader in2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
                String inputLine2;
                StringBuffer response2 = new StringBuffer();

                while ((inputLine2 = in2.readLine()) != null) {
                    response2.append(inputLine2);
                }
                in2.close();

                JSONObject result = (JSONObject) JSONValue.parse(response2.toString());
                Long i = (Long) result.getOrDefault("code", 2);
                if(i.equals(0L)){
                    for(String device : (ArrayList<String>)result.get("PCs")){
                        devices.add(new Device(device, false));
                    }
                    for(String device : (ArrayList<String>)result.get("Phones")){
                        devices.add(new Device(device, true));
                    }
                }else if(i.equals(4L)){
                    throw new Exception("Ваше устройство не зарегистрировано!");
                }
            } catch (Exception e) {
                Alert alert2 = new Alert(Alert.AlertType.ERROR);
                alert2.setTitle("Ошибка");
                alert2.setHeaderText(null);
                alert2.setContentText(e.getMessage());
                alert2.setOnCloseRequest(event -> Platform.exit());
                alert2.show();
            }
        };
        Thread t = new Thread(r);
        t.run();


        Type.setCellValueFactory(new PropertyValueFactory<Device, String>("Type"));
        Name.setCellValueFactory(new PropertyValueFactory<Device, String>("Name"));

        Callback<TableColumn<Device, Void>, TableCell<Device, Void>> cellFactory = new Callback<TableColumn<Device, Void>, TableCell<Device, Void>>() {
            @Override
            public TableCell<Device, Void> call(final TableColumn<Device, Void> param) {
                final TableCell<Device, Void> cell = new TableCell<Device, Void>() {

                    javafx.scene.control.Button button = new javafx.scene.control.Button("Удалить");
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        button.setTextFill(Paint.valueOf("#FFFFFF"));

                        if (devices.size() > getIndex() && getIndex() != -1)
                            if (name.equals(devices.get(getIndex()).getName())){
                                button.setTextFill(Paint.valueOf("linear-gradient(from 0% 0% to 100% 100%, #FC354C, #0ABFBC)"));
                                button.setStyle("-fx-background-color: #ffffff");
                            }
                        button.setOnMouseClicked(event -> {
                            Runnable r = () -> {
                                try {
                                    URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=RemoveDevice&RegDate="+regdate+"&Login=" + login + "&MyName=" + name + "&Id=" + id + "&Name=" + devices.get(getIndex()).getName());

                                    HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.getInputStream();
                                    if (name.equals(devices.get(getIndex()).getName())){
                                        devices.remove(getIndex());
                                        throw new Exception("Ваше устройство не зарегистрировано!");
                                    }
                                    devices.remove(getIndex());

                                } catch (Exception e) {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Ошибка");
                                    alert.setHeaderText(null);
                                    alert.setContentText(e.toString());
                                    alert.setOnCloseRequest(event2 -> Platform.exit());
                                    alert.show();
                                }
                            };
                            Thread t = new Thread(r);
                            t.run();
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
