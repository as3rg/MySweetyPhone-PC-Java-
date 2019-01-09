import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

class BlockSiteClass{
    public static void main(String[] args) throws IOException {
        switch (args[0]){
            case "--GetBlockedSites":
                getBlockedSites();
                break;
            case "--BlockSite":
                blockSite(args[1]);
                break;
            case "--UnblockSite":
                unblockSite(args[1]);
                break;
        }
    }

    public static void blockSite(String url) throws IOException {
        // Note that this code only works in Java 7+,
        // refer to the above link about appending files for more info

        // Get OS name
        String OS = System.getProperty("os.name").toLowerCase();

        // Use OS name to find correct location of hosts file
        String hostsFile = "";
        if ((OS.indexOf("win") >= 0)) {
            // Doesn't work before Windows 2000
            hostsFile = "C:\\Windows\\System32\\drivers\\etc\\hosts";
        } else if ((OS.indexOf("mac") >= 0)) {
            // Doesn't work before OS X 10.2
            hostsFile = "etc/hosts";
        } else if ((OS.indexOf("nux") >= 0)) {
            hostsFile = "/etc/hosts";
        } else {
            // Handle error when platform is not Windows, Mac, or Linux
            System.err.println("Sorry, but your OS doesn't support blocking.");
            System.exit(0);
        }

        // Actually block site
        Files.write(Paths.get(hostsFile),
                ("\n127.0.0.1 " + url).getBytes(),
                StandardOpenOption.APPEND);
    }

    public static void unblockSite(String url) throws IOException {
        // Note that this code only works in Java 7+,
        // refer to the above link about appending files for more info

        // Get OS name
        String OS = System.getProperty("os.name").toLowerCase();

        // Use OS name to find correct location of hosts file
        String hostsFile = "";
        if ((OS.indexOf("win") >= 0)) {
            // Doesn't work before Windows 2000
            hostsFile = "C:\\Windows\\System32\\drivers\\etc\\hosts";
        } else if ((OS.indexOf("mac") >= 0)) {
            // Doesn't work before OS X 10.2
            hostsFile = "etc/hosts";
        } else if ((OS.indexOf("nux") >= 0)) {
            hostsFile = "/etc/hosts";
        } else {
            // Handle error when platform is not Windows, Mac, or Linux
            System.err.println("Sorry, but your OS doesn't support blocking.");
            System.exit(0);
        }

        String out="";
        for(Object a : Arrays.stream(Files.readAllLines(Paths.get(hostsFile)).toArray()).filter(x -> !((String)x).contains(url) && !((String)x).equals(null)).toArray())
            out+=(String)a+'\n';
        Files.write(Paths.get(hostsFile),
                out.getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void getBlockedSites() throws IOException {
        // Note that this code only works in Java 7+,
        // refer to the above link about appending files for more info

        // Get OS name
        String OS = System.getProperty("os.name").toLowerCase();

        // Use OS name to find correct location of hosts file
        String hostsFile = "";
        if ((OS.indexOf("win") >= 0)) {
            // Doesn't work before Windows 2000
            hostsFile = "C:\\Windows\\System32\\drivers\\etc\\hosts";
        } else if ((OS.indexOf("mac") >= 0)) {
            // Doesn't work before OS X 10.2
            hostsFile = "etc/hosts";
        } else if ((OS.indexOf("nux") >= 0)) {
            hostsFile = "/etc/hosts";
        } else {
            // Handle error when platform is not Windows, Mac, or Linux
            System.err.println("Sorry, but your OS doesn't support blocking.");
            System.exit(0);
        }

        for(Object a : Arrays.stream(Files.readAllLines(Paths.get(hostsFile)).toArray()).filter(x -> !((String)x).contains("#") && !((String)x).equals("\n") && !((String)x).equals("") ).toArray()){
            System.out.println((String)a);
        }
        // Actually block site

    }
}