package sample;

import Utils.SessionClient;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class FileViewer {
    static public Stack<Pair<SessionClient, Stage>> sessionClients;
    SessionClient sc;
    Thread receiving;
    PrintWriter writer;
    BufferedReader reader;
    String name;
    Set<String> files;
    Stage stage;

    static {
        sessionClients = new Stack<>();
    }

    {
        Pair<SessionClient, Stage> p = sessionClients.pop();
        sc = p.getKey();
        stage = p.getValue();

    }

    @FXML
    private VBox Folders;

    @FXML
    private Button Back;

    @FXML
    private Label Path;

    @FXML
    private Button Reload;

    @FXML
    private Button NewFolder;

    @FXML
    private Button Upload;

    @FXML
    void initialize() throws IOException {
        File file = new File("properties.properties");
        FileInputStream propFile = new FileInputStream(file);
        Properties props = new Properties();
        props.load(propFile);
        propFile.close();
        name = (String) props.getOrDefault("name", "");
        files = new HashSet<>();

        receiving = new Thread(()-> {
            try {
                writer = new PrintWriter(sc.getSocket().getOutputStream());
                reader = new BufferedReader(new InputStreamReader(sc.getSocket().getInputStream()));
                Timer t = new Timer();
                t.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        JSONObject msg2 = new JSONObject();
                        msg2.put("Type", "start");
                        msg2.put("Name", name);
                        writer.println(msg2.toJSONString());
                        writer.flush();
                    }
                },0,2000);
                while (true) {
                    String line = reader.readLine();
                    if(line == null){
                        sc.Stop();
                        Platform.runLater(()-> stage.close());
                        break;
                    }
                    t.cancel();
                    JSONObject msg = (JSONObject) JSONValue.parse(line);
                    switch ((String) msg.get("Type")) {
                        case "finish":
                            sc.Stop();
                            Platform.runLater(()->stage.close());
                            break;
                        case "deleteFile":
                            if (((Long)msg.get("State")).intValue() == 1)
                                Platform.runLater(() -> {
                                    try {
                                        SystemTray tray = SystemTray.getSystemTray();
                                        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                                        TrayIcon trayIcon = new TrayIcon(image, "");
                                        trayIcon.setImageAutoSize(true);
                                        tray.add(trayIcon);
                                        trayIcon.displayMessage("Ошибка", "Нет доступа", TrayIcon.MessageType.INFO);
                                    } catch (AWTException e) {
                                        e.printStackTrace();
                                    }
                                });
                            else reloadFolder(null);
                            break;
                        case "showDir":
                            JSONArray values = (JSONArray) msg.get("Inside");
                            files.clear();
                            Platform.runLater(() -> {
                                if (((Long)msg.get("State")).intValue() == 1) {
                                    try {
                                        SystemTray tray = SystemTray.getSystemTray();
                                        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                                        TrayIcon trayIcon = new TrayIcon(image, "");
                                        trayIcon.setImageAutoSize(true);
                                        tray.add(trayIcon);
                                        trayIcon.displayMessage("Ошибка", "Нет доступа", TrayIcon.MessageType.INFO);
                                    } catch (AWTException e) {
                                        e.printStackTrace();
                                    }
                                    return;
                                }
                                Folders.getChildren().clear();
                                Path.setText((String)msg.get("Dir"));
                                Back.setVisible(!Path.getText().isEmpty());
                                NewFolder.setVisible(!Path.getText().isEmpty());
                                Upload.setVisible(!Path.getText().isEmpty());
                                Reload.setVisible(!Path.getText().isEmpty());
                                for (int i = 0; i < values.size(); i++) {
                                    JSONObject folder = (JSONObject)values.get(i);
                                    Draw((String) folder.get("Name"),folder.get("Type").equals("Folder"), (String) msg.get("Dir"));
                                }
                            });
                            break;
                        case "newDirAnswer":
                            Platform.runLater(()-> Draw((String)msg.get("DirName"), true, (String)msg.get("Dir")));
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiving.start();

        stage.setOnCloseRequest(e ->{
            receiving.interrupt();
            new Thread(() -> {
                try {
                    JSONObject msg2 = new JSONObject();
                    msg2.put("Type", "finish");
                    msg2.put("Name", name);
                    writer.println(msg2.toJSONString());
                    writer.flush();
                }catch (NullPointerException e2){
                    e2.printStackTrace();
                }
            }).start();
        });
    }

//    @Override
//    public void onDestroy() {
//        new Thread(()-> {
//            receiving.interrupt();
//            JSONObject msg = new JSONObject();
//            msg.put("Type", "finish");
//            msg.put("Name", name);
//            writer.println(msg.toJSONString());
//            writer.flush();
//        }).start();
//    }

    public void back(MouseEvent mouseEvent){
        new Thread(() -> {
            JSONObject msg2 = new JSONObject();
            msg2.put("Type", "back");
            msg2.put("Name", name);
            msg2.put("Dir", Path.getText());
            writer.println(msg2.toJSONString());
            writer.flush();
        }).start();
    }

    public void newFolder(MouseEvent mouseEvent){
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Имя папки");
            dialog.setHeaderText("Введите имя папки");
            Optional<String> s = dialog.showAndWait();
            if (!s.isEmpty()) {
                if (
                        s.get().contains("\\")
                                || s.get().contains("/")
                                || s.get().contains(":")
                                || s.get().contains("*")
                                || s.get().contains("?")
                                || s.get().contains("\"")
                                || s.get().contains("<")
                                || s.get().contains(">")
                                || s.get().contains("|")
                ) {
                    dialog.setContentText("Имя содержит недопустимые символы");
                } else if (files.contains(s.get())) {
                    dialog.setContentText("Такая папка уже существует");
                } else if (s.get().isEmpty()) {
                    dialog.setContentText("Имя файла не может быть пустым");
                } else {
                    new Thread(() -> {
                        JSONObject msg2 = new JSONObject();
                        msg2.put("Type", "newDir");
                        msg2.put("DirName", s.get());
                        msg2.put("Name", name);
                        msg2.put("Dir", Path.getText());
                        writer.println(msg2.toString());
                        writer.flush();
                    }).start();
                    break;
                }
            }else break;
        }
    }

    public void uploadFile(MouseEvent mouseEvent){
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите файл для загрузки");
        final File file = fc.showOpenDialog(null);
        if(file == null) return;

        new Thread(() -> {
            try {
                ServerSocket ss = new ServerSocket(0);
                JSONObject msg2 = new JSONObject();
                msg2.put("Type", "uploadFile");
                msg2.put("Name", name);
                msg2.put("FileName", file.getName());
                msg2.put("FileSocketPort", ss.getLocalPort());
                msg2.put("Dir", Path.getText());
                writer.println(msg2.toJSONString());
                writer.flush();
                Socket socket = ss.accept();
                DataOutputStream fileout = new DataOutputStream(socket.getOutputStream());
                FileInputStream filein = new FileInputStream(file);
                IOUtils.copy(filein, fileout);
                fileout.flush();
                filein.close();
                socket.close();
                Platform.runLater(()->{
                    try {
                        SystemTray tray = SystemTray.getSystemTray();
                        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                        TrayIcon trayIcon = new TrayIcon(image, "");
                        trayIcon.setImageAutoSize(true);
                        tray.add(trayIcon);
                        trayIcon.displayMessage("Отправка завершена", "Файл \""+file.getName()+"\" загружен", TrayIcon.MessageType.INFO);
                        reloadFolder(null);
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void reloadFolder(MouseEvent mouseEvent){
        new Thread(() -> {
            JSONObject msg3 = new JSONObject();
            msg3.put("Type", "showDir");
            msg3.put("Name", name);
            msg3.put("Dir", Path.getText());
            writer.println(msg3.toJSONString());
            writer.flush();
        }).start();
    }

    public void Draw(String fileName, boolean isFolder, String dir){
        Label folder = new Label(fileName);
        files.add(fileName);
        folder.setPadding(new Insets(20, 20, 20, 20));
        folder.setFont(new Font(14));
//                                        Drawable d = getDrawable(values.getJSONObject(i).getString("Type").equals("Folder") ? R.drawable.ic_file_viewer_folder : R.drawable.ic_file_viewer_file);
//                                        d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
//                                        folder.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        Folders.getChildren().add(folder);

        final ContextMenu contextMenu = new ContextMenu();
        javafx.scene.control.MenuItem delete = new javafx.scene.control.MenuItem("Удалить");
        contextMenu.getItems().addAll(delete);
        delete.setOnAction(event -> {
            new Thread(()-> {
                JSONObject msg2 = new JSONObject();
                msg2.put("Type", "deleteFile");
                msg2.put("Name", name);
                msg2.put("FileName", fileName);
                msg2.put("Dir", dir);
                writer.println(msg2.toJSONString());
                writer.flush();
            }).start();
        });

        if(isFolder) {
            folder.setText("📁 " + folder.getText());
            folder.setOnMouseClicked(v -> new Thread(() -> {
                JSONObject msg3 = new JSONObject();
                msg3.put("Type", "showDir");
                msg3.put("Name", name);
                msg3.put("Dir", new File(dir, fileName).getPath());
                writer.println(msg3.toJSONString());
                writer.flush();
            }).start());
        }else {
            folder.setText("📄 "+folder.getText());
            javafx.scene.control.MenuItem save = new javafx.scene.control.MenuItem("Сохранить как");
            contextMenu.getItems().addAll(save);
            save.setOnAction(v -> {
                DirectoryChooser fc = new DirectoryChooser();
                fc.setTitle("Выберите папку для сохранения");
                final File out = fc.showDialog(null);
                if (out == null) return;
                new Thread(() -> {
                    try {
                        File out3 = new File(out, "MySweetyPhone");
                        out3.mkdirs();
                        File out2 = new File(out3, fileName);

                        ServerSocket ss = new ServerSocket(0);
                        JSONObject msg2 = new JSONObject();
                        msg2.put("Type", "downloadFile");
                        msg2.put("Name", name);
                        msg2.put("FileName", fileName);
                        msg2.put("FileSocketPort", ss.getLocalPort());
                        msg2.put("Dir", dir);
                        writer.println(msg2.toJSONString());
                        writer.flush();
                        Socket socket = ss.accept();
                        DataInputStream filein = new DataInputStream(socket.getInputStream());
                        FileOutputStream fileout = new FileOutputStream(out2);
                        IOUtils.copy(filein, fileout);
                        fileout.close();
                        socket.close();
                        Platform.runLater(() -> {
                            try {
                                SystemTray tray = SystemTray.getSystemTray();
                                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                                TrayIcon trayIcon = new TrayIcon(image, "");
                                trayIcon.setImageAutoSize(true);
                                tray.add(trayIcon);
                                trayIcon.displayMessage("Загрузка завершена", "Файл \"" + out2.getName() + "\" загружен", TrayIcon.MessageType.INFO);
                            } catch (AWTException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            });
        }
        folder.setOnContextMenuRequested((EventHandler<Event>) event -> contextMenu.show(folder, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y));
    }
}
