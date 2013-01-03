package captureplugin.drivers.dreambox.connector.cs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import sun.net.TelnetInputStream;
import sun.net.TelnetOutputStream;
import sun.net.ftp.FtpClient;

/**
 * @author fishhead
 * 
 */
public class FtpHelper extends FtpClient {

  // Logger
  private static final Logger mLog = Logger
      .getLogger(FtpHelper.class.getName());

  private static final String ENCODING = "UTF-8";

  private static final String NAME = "name";
  private static final String SIZE = "size";
  private static final String[] FTP_COLS = new String[] { "rights", "type",
      "user", "group", SIZE, "date", "date", "date", NAME };

  /**
   * main
   * 
   * @param args
   */
  public static void main(String[] args) {
    String server = "dreambox";
    String user = "root";
    String password = "";
    FtpHelper ftpHelper = new FtpHelper();
    System.out.println(ftpHelper.cmd("OPEN", server));
    System.out.println(ftpHelper.cmd("LOGIN", user, password));
    System.out.println(ftpHelper.cmd("CD", "/hdd/movie/"));
    System.out.println(ftpHelper.cmd("SYSTEM"));
    System.out.println(ftpHelper.cmd("LIST", "/hdd/movie/"));
    try {
      for (Entry<String, String> entry : ftpHelper.getFileSize("/hdd/movie/")
          .entrySet()) {
        System.out.println(entry.toString());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    String s = ftpHelper.cmd("GET", 
        "/hdd/movie/20101225 1439 - Das Erste HD - Rapunzel.ts.meta");
    String t = s;
    mLog.info(t);
    System.out.println(ftpHelper.cmd("CLOSE"));
  }

  /**
   * ftp Commands ausfuehren
   * 
   * @param args
   * 
   * @return
   */
  String cmd(String... args) {
    String s = null;
    String cmd = args[0];
    try {
      if (cmd.equalsIgnoreCase("OPEN")) {
        // OPEN
        this.openServer(args[1]);
        s = getResponseString();
      } else if (cmd.equalsIgnoreCase("CLOSE")) {
        // CLOSE
        this.closeServer();
        s = getResponseString();
      } else if (cmd.equalsIgnoreCase("SYSTEM")) {
        // SYSTEM
        s = this.system();
      } else if (cmd.equalsIgnoreCase("LOGIN")) {
        // LOGIN
        String user = args[1];
        String password = args[2];
        if (user.length() == 0) {
          user = "root";
        }
        if (password.length() == 0) {
          password = " ";
        }
        this.login(user, password);
        s = getResponseString();
      } else if (cmd.equalsIgnoreCase("CD")) {
        // CD
        this.cd(args[1]);
        s = this.getResponseString();
      } else if (cmd.equalsIgnoreCase("PWD")) {
        // PWD
        s = this.pwd().trim();
      } else if (cmd.equalsIgnoreCase("LIST")) {
        // LIST
        s = "";
        for (Map<String, String> map : getListOfFiles(args[1])) {
          for (Entry<String, String> entry : map.entrySet()) {
            s += entry.getKey() + "=" + entry.getValue() + "\t";
          }
          s += "\n";
        }
      } else if (cmd.equalsIgnoreCase("GET")) {
        // GET file
        this.binary();
        String filename = new String(args[1].getBytes(ENCODING));
        TelnetInputStream tis = this.get(filename);
        byte[] buf = new byte[1024];
        int len = 0;
        s = "";
        while ((len = tis.read(buf)) != -1) {
          s += new String(buf, 0, len, ENCODING);
        }
        tis.close();
      } else if (cmd.equalsIgnoreCase("PUT")) {
        // PUT file
        this.binary();
        String filename = new String(args[1].getBytes(ENCODING));
        TelnetOutputStream tos = this.put(filename);
        tos.write(args[2].getBytes(ENCODING));
        tos.close();
      } else {
        mLog.warning("unkown command : " + cmd);
        for (int i = 1; i < args.length; i++) {
          mLog.warning(String.format("parameter %4d : %s", i, args[i]));
        }
      }
    } catch (sun.net.ftp.FtpLoginException e) {
      JOptionPane.showMessageDialog(null, e.getLocalizedMessage(),
          "FTP-Error: " + cmd, JOptionPane.ERROR_MESSAGE);
    } catch (FileNotFoundException e) {
      s = null;
    } catch (IOException e) {
      mLog.log(Level.WARNING, "IOException", e);
    }

    return s;
  }

  /**
   * Rekursiv alle Dateien auflisten
   * 
   * @param dir
   * 
   * @return list files
   * 
   * @throws IOException
   */
  public List<Map<String, String>> getListOfFiles(String dir)
      throws IOException {

    if (!dir.endsWith("/")) {
      dir += "/";
    }

    // CD
    cd(dir);

    // LIST
    TelnetInputStream tis = list();
    byte[] buf = new byte[4096];
    int len = 0;
    String s = "";
    while ((len = tis.read(buf)) != -1) {
      s += new String(buf, 0, len, ENCODING);
    }
    tis.close();

    // Parse
    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
    for (String row : s.split("\\n")) {
      Map<String, String> map = new TreeMap<String, String>();
      list.add(map);

      int i = 0;
      for (String col : row.split(" +")) {
        if (i < FTP_COLS.length) {
          String key = FTP_COLS[i];
          if (map.containsKey(key)) {
            map.put(key, map.get(key) + " " + col);
          } else {
            map.put(key, col);
          }
        } else {
          String lastKey = FTP_COLS[FTP_COLS.length - 1];
          map.put(lastKey, map.get(lastKey) + " " + col);
        }
        i++;
      }
      // Absolute Path
      map.put(NAME, dir + map.get(NAME));
    }

    return list;
  }

  /**
   * get filename and size
   * 
   * @param dir
   * @return map filename/size
   * @throws IOException
   */
  public Map<String, String> getFileSize(String dir) throws IOException {
    Map<String, String> mapFileSize = new TreeMap<String, String>();
    for (Map<String, String> map : getListOfFiles(dir)) {
      mapFileSize.put(map.get(NAME), map.get(SIZE));
    }
    return mapFileSize;
  }
}
