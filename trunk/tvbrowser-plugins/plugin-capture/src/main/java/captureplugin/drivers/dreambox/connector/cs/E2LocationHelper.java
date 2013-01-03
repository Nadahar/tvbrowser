package captureplugin.drivers.dreambox.connector.cs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class E2LocationHelper {
  // Logger
  private static final Logger mLog = Logger.getLogger(E2LocationHelper.class.getName());
  // Class
  private static final Map<String, E2LocationHelper> singletonMap = new HashMap<String, E2LocationHelper>();
  // Member
  private List<String> mLocations = null;
  private final DreamboxConfig mConfig;
  private final Thread mThreadWaitFor;
  private Thread mThread;

  /**
   * Factory
   * 
   * @param config
   *          for dreambox
   * @param thread
   *          to wait for
   * 
   * @return locationThread
   * 
   */
  public static E2LocationHelper getInstance(DreamboxConfig config, Thread thread) {
    String id = config.getId();
    E2LocationHelper singleton = null;
    synchronized (singletonMap) {
      singleton = singletonMap.get(id);
      if (singleton == null) {
        singleton = new E2LocationHelper(config, thread);
        singletonMap.put(id, singleton);
      }
    }
    return singleton;
  }

  /**
   * Konstruktor
   * 
   * @param config
   * @param thread
   */
  private E2LocationHelper(DreamboxConfig config, Thread thread) {
    mLog.setLevel(Level.INFO);    
    this.mConfig = config;
    this.mThreadWaitFor = thread;
    this.mLocations = null;
    run();
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
   * get locations
   * 
   * @return locations
   */
  public synchronized List<String> getLocations() {
    if (!mThread.isAlive() && (mLocations == null)) {
      run();
    }
    try {
      mThread.join();
    } catch (InterruptedException e) {
      while ((mThread.getState() == Thread.State.RUNNABLE) && (mLocations == null)) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          mLog.log(Level.WARNING, "InterruptedException", e1);
        }
      }
    }
    return mLocations;
  }

  /**
   * read locations from dreambox
   */
  private void run() {
    mThread = new Thread() {

      @Override
      public void run() {

        if (mLocations == null) {
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
            URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/getlocations");
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
            E2ListItemHandler handler;
            if (data.indexOf("e2location") != -1) {
              handler = new E2ListItemHandler("e2location");
            } else if (data.indexOf("e2simplexmlitem") != -1) {
              handler = new E2ListItemHandler("e2simplexmlitem");
            } else {
              data = "<e2locations><e2location>/hdd/movie/</e2location></e2locations>";
              handler = new E2ListItemHandler("e2location");
            }
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(new StringReader(data)), handler);

            mLocations = handler.getList();

          } catch (ParserConfigurationException e) {
            mLog.log(Level.WARNING, "ParserConfigurationException", e);
          } catch (SAXException e) {
            mLog.warning(data);
            mLog.log(Level.WARNING, "SAXException", e);
          } catch (MalformedURLException e) {
            mLog.log(Level.WARNING, "MalformedURLException", e);
          } catch (SocketTimeoutException e) {
            mLog.log(Level.WARNING, "SocketTimeoutException", e);
          } catch (IOException e) {
            mLog.log(Level.WARNING, "IOException", e);
          } catch (IllegalArgumentException e) {
            mLog.log(Level.WARNING, "IllegalArgumentException", e);
          }

          mLog.info("[" + mConfig.getDreamboxAddress() + "] " + "GET getlocations - "
              + (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis()) + " ms");
        }
      }
    };
    mThread.start();
  }

}
