package sample;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class Saved {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox Messages;

    private String name;
    private String login;
    private int id;

    @FXML
    void initialize() {
        VBox vBox = new VBox();
        Label text = new Label();
        Label date = new Label();
        vBox.setStyle("-fx-background-color: #1e90ff; -fx-background-radius: 10;");
        date.setText("today");
        date.setTextFill(Color.LIGHTGRAY);
        date.setPadding(new Insets(0,10,0,10));
        text.setText("test");
        text.setMaxWidth(460);
        text.setMinWidth(date.getPrefWidth());
        text.setMinHeight(75);
        text.setPadding(new Insets(10,10,10,10));
        text.setAlignment(Pos.CENTER_LEFT);
        text.setWrapText(true);
        vBox.getChildren().add(text);
        vBox.getChildren().add(date);
        Messages.getChildren().add(vBox);
        /*FileInputStream propFile = new FileInputStream(location.getPath()+"/../../properties.properties");
        Properties props = new Properties();
        props.load(propFile);
        propFile.close();
        id = Integer.parseInt((String)props.getOrDefault("id","-1"));
        login = (String)props.getOrDefault("login","");
        name = (String)props.getOrDefault("name","");

        URL url = new URL("http://mysweetyphone.herokuapp.com/");
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("Type", "DelMessage");
        params.put("MyName", name);
        params.put("Login", login);
        params.put("Id", id);
        params.put("Msg", "lol");
        params.put("Sender", name);
        params.put("Date", 1546246113);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        for (int c; (c = in.read()) >= 0;)
            System.out.print((char)c);*/
    }
}
