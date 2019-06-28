package Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Stream;

public class ServerMode {
    private static ArrayList<SessionServer> opened = new ArrayList<>();

    static boolean State = false;

    static public boolean getState(){
        return State;
    }

    static {
        try {
            FileInputStream propFile = new FileInputStream("properties.properties");
            Properties props = new Properties();
            props.load(propFile);
            propFile.close();

            if(Boolean.parseBoolean((String) props.getOrDefault("ServerMode", Boolean.toString(false))))
                Start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static private void CreateServer(int i) {
        try {
            SessionServer sessionServer = new SessionServer(i, 0, null);
            sessionServer.setOnStop(() -> {
                opened.remove(sessionServer);
                CreateServer(i);
            });
            opened.add(sessionServer);
            sessionServer.Start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void Start() throws IOException {
        State = true;
        RewriteState();
        for(int i : SessionServer.allowedTypes){
            CreateServer(i);
        }
    }

    static public void Stop() throws IOException {
        State = false;
        RewriteState();
        for(SessionServer s : opened){
            s.setOnStop(null);
            s.Stop();
        }
    }

    static private void RewriteState() throws IOException {
        FileInputStream in = new FileInputStream("properties.properties");
        Properties props = new Properties();
        props.load(in);
        in.close();
        props.setProperty("ServerMode", Boolean.toString(State));
        FileOutputStream out = new FileOutputStream("properties.properties");
        props.store(out, null);
        out.close();
    }
}
