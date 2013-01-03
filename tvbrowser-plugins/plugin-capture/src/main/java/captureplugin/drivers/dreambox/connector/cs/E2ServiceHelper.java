package captureplugin.drivers.dreambox.connector.cs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author fishhead
 * 
 */
public class E2ServiceHelper {
  // Logger
  private static final Logger mLog = Logger.getLogger(E2ServiceHelper.class
      .getName());
  // Class
  private static Map<String, String> cServiceReferenceSD = null;
  private static Map<String, String> cServiceReferenceHD = null;
  private static Map<String, String> cServiceNameSD = null;
  private static Map<String, String> cServiceNameHD = null;

  /**
   * init
   */
  public static synchronized void init() {
    mLog.setLevel(Level.INFO);    
    if (cServiceReferenceSD == null) {
      cServiceReferenceSD = new HashMap<String, String>();
      cServiceReferenceHD = new HashMap<String, String>();
      cServiceNameSD = new HashMap<String, String>();
      cServiceNameHD = new HashMap<String, String>();
      Properties properties = new Properties();
      FileInputStream stream;
      try {
        String local = System.getProperty("user.home") + File.separatorChar
            + "SD_HD.properties";
        stream = new FileInputStream(local);
        properties.load(stream);
        stream.close();
      } catch (FileNotFoundException e) {
        mLog.warning(e.getLocalizedMessage());
      } catch (IOException e) {
        mLog.warning(e.getLocalizedMessage());
      }
      int i = 1;
      String refSD, refHD, nameSD, nameHD;
      while (true) {
        refSD = properties.getProperty("SD." + i + ".Reference");
        refHD = properties.getProperty("HD." + i + ".Reference");
        nameSD = properties.getProperty("SD." + i + ".Name");
        nameHD = properties.getProperty("HD." + i + ".Name");
        if ((refSD == null) || (refHD == null) || (nameSD == null)
            || (nameHD == null)) {
          break;
        }
        cServiceReferenceSD.put(refSD, refHD);
        cServiceReferenceHD.put(refHD, refSD);
        cServiceNameSD.put(nameSD, nameHD);
        cServiceNameHD.put(nameHD, nameSD);
        i++;
      }
    }
    mLog.info("INIT E2ServiceHelper");
  }

  /**
   * Handelt es sich um einen HD-Sender
   * 
   * @param serviceRefHD
   * @return ja/nein
   */
  public static boolean isHdService(String serviceRefHD) {
    return cServiceReferenceHD.containsKey(serviceRefHD);
  }

  /**
   * Gibt es einen zugehoerigen HD-Sender
   * 
   * @param serviceRefSD
   * @return ja/nein
   */
  public static boolean hasHdService(String serviceRefSD) {
    return cServiceReferenceSD.containsKey(serviceRefSD);
  }

  /**
   * Get Service Reference
   * 
   * @param serviceRef
   * @param isHdService
   * 
   * @return reference
   */
  public static String getServiceRef(String serviceRef, boolean isHdService) {

    String sRef = serviceRef;
    if (isHdService) {
      if (cServiceReferenceSD.containsKey(serviceRef)) {
        sRef = cServiceReferenceSD.get(serviceRef);
      }
    } else {
      if (cServiceReferenceHD.containsKey(serviceRef)) {
        sRef = cServiceReferenceHD.get(serviceRef);
      }
    }
    return sRef;
  }

  /**
   * Get Service Name
   * 
   * @param serviceName
   * @param useHdService
   * 
   * @return name
   */
  public static String getServiceName(String serviceName, boolean useHdService) {
    String sName = serviceName;
    if (useHdService) {
      // HD
      if (cServiceNameSD.containsKey(serviceName)) {
        sName = cServiceNameSD.get(serviceName);
      }
    } else {
      // SD
      if (cServiceNameHD.containsKey(serviceName)) {
        sName = cServiceNameHD.get(serviceName);
      }
    }
    return sName;
  }

  /**
   * Umsetzung SD nach HD vorhanden
   * 
   * @return ja/nein
   */
  public static boolean exits() {
    return cServiceReferenceSD.size() > 0;
  }

  // Statischer Initializer
  static {
    init();
  }

}
