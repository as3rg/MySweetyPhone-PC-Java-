package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class RegDevice {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField PhoneName;

    @FXML
    private Button Next;

    @FXML
    private Label Error;

    @FXML
    private AnchorPane MainPane;

    @FXML
    void initialize() {

    }

    @FXML
    private void onNextClick() throws IOException {
        Next.setDisable(true);
        PhoneName.setDisable(true);
        FileInputStream propFile = new FileInputStream("properties.properties");
        Properties props = new Properties();
        props.load(propFile);
        int id = Integer.parseInt((String)props.getOrDefault("id","-1"));
        String login = (String)props.getOrDefault("login","");
        Runnable r = () -> {
            try {
                URL obj = new URL("http://mysweetyphone.herokuapp.com/?Type=AddDevice&DeviceType=PC&Id="+id+"&Login="+login+"&Name="+PhoneName.getText());

                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();


                JSONObject result = (JSONObject) JSONValue.parse(response.toString());
                Long i = (Long) result.getOrDefault("code", 2);
                if(i.equals(3L)){
                    Error.setVisible(true);
                    Error.setText("Вы должны указать имя!!");
                }else if(i.equals(2L)){
                    Error.setVisible(true);
                    Error.setText("Ошибка приложения!");
                }else if(i.equals(1L)){
                    Error.setVisible(true);
                    Error.setText("Вы уже используете это имя!");
                }else if(i.equals(0L)){
                    Platform.runLater(()-> {
                        try {
                            props.setProperty("name", PhoneName.getText());
                            props.setProperty("regdate", ((Long) result.get("regdate")).toString());
                            props.store(new FileOutputStream("properties.properties"), "");
                            AnchorPane pane = FXMLLoader.load(getClass().getResource("MainActivity.fxml"));
                            MainPane.getChildren().setAll(pane);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }else{
                    Error.setVisible(true);
                    Error.setText("Ошибка приложения!");
                }
                Next.setDisable(false);
                PhoneName.setDisable(false);
            }catch (Exception e){
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();
        propFile.close();
    }
}