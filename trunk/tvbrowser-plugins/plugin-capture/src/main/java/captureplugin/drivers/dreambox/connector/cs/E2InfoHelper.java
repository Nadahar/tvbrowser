package captureplugin.drivers.dreambox.connector.cs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import captureplugin.drivers.dreambox.DreamboxConfig;

/**
 * @author fishhead
 * 
 */
public class E2InfoHelper {
  // Logger
  private static final Logger mLog = Logger.getLogger(E2InfoHelper.class.getName());
  // Class
  private static Map<String, E2InfoHelper> singletonMap = new HashMap<String, E2InfoHelper>();

  // Member
  private final DreamboxConfig mConfig;
  private final Thread mThreadWaitFor;
  private Thread mThread;
  private List<Map<String, String>> mInfos = null;

  /**
   * Factory
   * 
   * @param config
   *          for dreambox
   * @param thread
   *          to wait for
   * 
   * @return infoThread
   */
  public static E2InfoHelper getInstance(DreamboxConfig config, Thread thread) {
    String id = config.getId();
    E2InfoHelper singleton = null;
    synchronized (singletonMap) {
      singleton = singletonMap.get(id);
      if (singleton == null) {
        singleton = new E2InfoHelper(config, thread);
        singletonMap.put(id, singleton);
      }
    }
    // Info immer aktuell besorgen
    singleton.run();
    return singleton;
  }

  /**
   * Konstruktor
   * 
   * @param config
   * @param thread
   */
  private E2InfoHelper(DreamboxConfig config, Thread thread) {
    mLog.setLevel(Level.INFO);    
    this.mConfig = config;
    this.mThreadWaitFor = thread;
  }

  /**
   * get thread
   * 
   * @return Thread
   */
  public Thread getThread() {
    return mThread;
  }

  /**
   * get movies
   * 
   * @return movies
   */
  public synchronized List<Map<String, String>> getInfos() {
    if (!mThread.isAlive() && (mInfos == null)) {
      run();
    }

    try {
      mThread.join();
    } catch (InterruptedException e) {
      while ((mThread.getState() == Thread.State.RUNNABLE) && (mInfos == null)) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          mLog.log(Level.WARNING, "InterruptedException", e1);
        }
      }
    }
    return mInfos;
  }

  /**
   * read info from dreambox
   */
  private void run() {
    mThread = new Thread() {

      @Override
      public void run() {

        if (mInfos == null) {

          // wait
          if (mThreadWaitFor != null) {
            try {
              mThreadWaitFor.join();
            } catch (InterruptedException e) {
              mLog.log(Level.WARNING, "InterruptedException", e);
            }
          }

          final Calendar cal = new GregorianCalendar();
          String data = "";

          try {
            URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/about");
            URLConnection connection = url.openConnection();

            String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(mConfig.getTimeout());
            InputStream stream = connection.getInputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = stream.read(buf)) != -1) {
              data += new String(buf, 0, len, "UTF-8");
            }
            stream.close();
            E2ListMapHandler handler = new E2ListMapHandler("e2abouts", "e2about");
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(new StringReader(data)), handler);

            mInfos = handler.getList();

          } catch (ParserConfigurationException e) {
            mLog.log(Level.WARNING, "ParserConfigurationException", e);
          } catch (SAXException e) {
            mLog.log(Level.WARNING, "SAXException", e);
            mInfos = new ArrayList<Map<String, String>>();
            Map<String, String> map = new TreeMap<String, String>();
            map.put("Fehlertext", e.getLocalizedMessage());
            mInfos.add(map);
          } catch (MalformedURLException e) {
            mLog.log(Level.WARNING, "MalformedURLException", e);
          } catch (SocketTimeoutException e) {
            mLog.log(Level.WARNING, "SocketTimeoutException", e);
          } catch (IOException e) {
            mLog.log(Level.WARNING, "IOException", e);
          } catch (IllegalArgumentException e) {
            mLog.log(Level.WARNING, "IllegalArgumentException", e);
          }

          mLog.info("[" + mConfig.getDreamboxAddress() + "] " + "GET about - "
              + (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis()) + " ms");
        }
      }
    };
    mThread.start();
  }

}
