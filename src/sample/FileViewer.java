package sample;

import Utils.SessionClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class FileViewer {
    static public Stack<SessionClient> sessionClients;

    static {
        sessionClients = new Stack<>();
    }

    {
        sc = sessionClients.pop();
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
                sc.setSocket(new Socket(sc.getAddress(), sc.getPort()));
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
                        System.out.println(sc.getAddress().getHostAddress()+":"+sc.getPort());
                    }
                },0,2000);
                while (true) {
                    String line = reader.readLine();
                    t.cancel();
                    System.out.println(line);
                    JSONObject msg = (JSONObject) JSONValue.parse(line);
                    switch ((String) msg.get("Type")) {
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
                                    Label folder = new Label((String)((JSONObject)values.get(i)).get("Name"));
                                    files.add((String)((JSONObject)values.get(i)).get("Name"));
                                    folder.setPadding(new Insets(20, 20, 20, 20));
                                    folder.setFont(new Font(20));
//                                        Drawable d = getDrawable(values.getJSONObject(i).getString("Type").equals("Folder") ? R.drawable.ic_file_viewer_folder : R.drawable.ic_file_viewer_file);
//                                        d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
//                                        folder.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                                    Folders.getChildren().add(folder);
                                    final int i2 = i;

                                    if(((JSONObject)values.get(i)).get("Type").equals("Folder"))
                                        folder.setOnMouseClicked(v -> new Thread(() -> {
                                            JSONObject msg3 = new JSONObject();
                                            msg3.put("Type", "showDir");
                                            msg3.put("Name", name);
                                            msg3.put("Dir", new File((String)msg.get("Dir"), (String)((JSONObject)values.get(i2)).get("Name")).toPath());
                                            writer.println(msg3.toJSONString());
                                            writer.flush();
                                        }).start());
                                    else folder.setOnMouseClicked(v -> new Thread(() -> {
                                        try {
                                            DirectoryChooser fc = new DirectoryChooser();
                                            fc.setTitle("Выберите папку для сохранения");
                                            final File out = fc.showDialog(null);
                                            if(out == null) return;
                                            File out3 = new File(out,"MySweetyPhone");
                                            out.mkdirs();
                                            File out2 = new File(out3, (String)((JSONObject)values.get(i2)).get("Name"));
                                            out2.createNewFile();

                                            ServerSocket ss = new ServerSocket(0);
                                            JSONObject msg2 = new JSONObject();
                                            msg2.put("Type", "downloadFile");
                                            msg2.put("Name", name);
                                            msg2.put("FileName", ((JSONObject)values.get(i2)).get("Name"));
                                            msg2.put("FileSocketPort", ss.getLocalPort());
                                            msg2.put("Dir", Path.getText());
                                            writer.println(msg2.toJSONString());
                                            writer.flush();
                                            Socket socket = ss.accept();
                                            DataInputStream filein = new DataInputStream(socket.getInputStream());
                                            FileOutputStream fileout = new FileOutputStream(out2);
                                            IOUtils.copy(filein, fileout);
                                            fileout.close();
                                            socket.close();
                                            Platform.runLater(()->{
                                                try {
                                                    SystemTray tray = SystemTray.getSystemTray();
                                                    Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                                                    TrayIcon trayIcon = new TrayIcon(image, "");
                                                    trayIcon.setImageAutoSize(true);
                                                    tray.add(trayIcon);
                                                    trayIcon.displayMessage("Закачка завершена", "Файл "+out2.getName()+" скачен", TrayIcon.MessageType.INFO);
                                                } catch (AWTException e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }).start());
                                }
                            });
                            break;
                        case "newDirAnswer":
                            Platform.runLater(()-> {
                                Label folder = new Label((String)msg.get("DirName"));
                                files.add((String)msg.get("DirName"));
                                folder.setPadding(new Insets(20, 20, 20, 20));
                                folder.setFont(new Font(20));
//                                Drawable d = getDrawable(R.drawable.ic_file_viewer_folder);
//                                d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
//                                folder.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                                Folders.getChildren().add(folder);
                                folder.setOnMouseClicked(v -> {
                                    new Thread(() -> {
                                        JSONObject msg3 = new JSONObject();
                                        msg3.put("Type", "showDir");
                                        msg3.put("Name", name);
                                        msg3.put("Dir", new File((String)msg.get("Dir"), (String)msg.get("DirName")).toPath());
                                        writer.println(msg3.toJSONString());
                                        writer.flush();
                                    }).start();
                                });
                            });
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiving.start();
    }

    static public SessionClient sc;
    Thread receiving;
    static final int MESSAGESIZE = 100;
    PrintWriter writer;
    BufferedReader reader;
    String name;
    Set<String> files;

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
//        final TextField input = new TextField();
//        AlertDialog alert = new AlertDialog.Builder(this)
//                .setTitle("Имя папки")
//                .setMessage("Введите имя папки")
//                .setView(input)
//                .setPositiveButton("Создать", null)
//                .setNegativeButton("Отмена", (dialog, which) -> {})
//                .create();
//        alert.setOnShowListener(dialog -> {
//            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v2 -> {
//                if(
//                        input.getText().toString().contains("\\")
//                                || input.getText().toString().contains("/")
//                                || input.getText().toString().contains(":")
//                                || input.getText().toString().contains("*")
//                                || input.getText().toString().contains("?")
//                                || input.getText().toString().contains("\"")
//                                || input.getText().toString().contains("<")
//                                || input.getText().toString().contains(">")
//                                || input.getText().toString().contains("|")
//                ) {
//                    alert.setMessage("Имя содержит недопустимые символы");
//                }else if(files.contains(input.getText().toString())) {
//                    alert.setMessage("Такая папка уже существует");
//                }else if(input.getText().toString().isEmpty()) {
//                    alert.setMessage("Имя файла не может быть пустым");
//                }else {
//                    new Thread(() -> {
//                        try {
//                            JSONObject msg2 = new JSONObject();
//                            msg2.put("Type", "newDir");
//                            msg2.put("DirName", input.getText().toString());
//                            msg2.put("Name", name);
//                            msg2.put("Dir", ((TextView)findViewById(R.id.pathFILEVIEWER)).getText().toString());
//                            writer.println(msg2.toString());
//                            writer.flush();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }).start();
//                    alert.dismiss();
//                }
//            });
//        });
//        alert.show();
    }

    public void uploadFile(MouseEvent mouseEvent){
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите папку для сохранения");
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
}
