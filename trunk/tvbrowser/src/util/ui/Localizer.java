/*
 * Localizer.java
 *
 * Created on 27. April 2003, 15:05
 */

package util.ui;

import java.util.*;
import java.text.MessageFormat;

import util.io.IOUtilities;

/**
 *
 * @author  Til
 */
public class Localizer {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(Localizer.class.getName());

  private static final Object[] ONE_ARG_ARR = new Object[1];
  private static final Object[] TWO_ARGS_ARR = new Object[2];
  private static final Object[] THREE_ARGS_ARR = new Object[3];
  
  /**
   * Contains for a Class (key) a Localizer (value).
   */
  private static HashMap mLocalizerCache = new HashMap();
    
  private String mBaseName;
  private ResourceBundle mBundle;
  private String mKeyPrefix;
  
  

  /**
   * Creates a new instance of Localizer.
   */
  private Localizer(Class clazz) {
    String packageName = clazz.getPackage().getName();
    mKeyPrefix = clazz.getName().substring(packageName.length() + 1) + ".";

    int lastDot = packageName.lastIndexOf('.');
    if (lastDot == -1) {
      mBaseName = packageName + "." + packageName;
    } else {
      mBaseName = packageName + packageName.substring(lastDot);
    }
    
    Locale locale = Locale.getDefault();
   
    try {
      mBundle = ResourceBundle.getBundle(mBaseName, locale, clazz.getClassLoader());
    }
    catch (MissingResourceException exc) {
      mLog.warning("ResourceBundle not found: '" + mBaseName + "'");
    }
  }
  
  
  
  public static Localizer getLocalizerFor(Class clazz) {
    Localizer localizer = (Localizer) mLocalizerCache.get(clazz);
    
    if (localizer == null) {
      localizer = new Localizer(clazz);
      mLocalizerCache.put(clazz, localizer);
    }
    
    return localizer;
  }

  
  
  public String msg(String key, String defaultMsg, Object arg1) {
    synchronized (ONE_ARG_ARR) {
      ONE_ARG_ARR[0] = arg1;
      
      return msg(key, defaultMsg, ONE_ARG_ARR);
    }
  }

  
  
  public String msg(String key, String defaultMsg, Object arg1, Object arg2) {
    synchronized (TWO_ARGS_ARR) {
      TWO_ARGS_ARR[0] = arg1;
      TWO_ARGS_ARR[1] = arg2;
      
      return msg(key, defaultMsg, TWO_ARGS_ARR);
    }
  }

  
  
  public String msg(String key, String defaultMsg, Object arg1, Object arg2,
    Object arg3)
  {
    synchronized (THREE_ARGS_ARR) {
      THREE_ARGS_ARR[0] = arg1;
      THREE_ARGS_ARR[1] = arg2;
      THREE_ARGS_ARR[2] = arg3;
      
      return msg(key, defaultMsg, THREE_ARGS_ARR);
    }
  }
  
  
  
  public String msg(String key, String defaultMsg, Object[] args) {
    String msg = msg(key, defaultMsg);
    
    // Workaround: The MessageFormat uses the ' char for quoting strings.
    //             so the "{0}" in "AB '{0}' CD" will not be replaced.
    //             In order to avoid this we quote every ' with '', so
    //             everthing will be replaced as expected.
    msg = IOUtilities.replace(msg, "'", "''");
    
    return MessageFormat.format(msg, args);    
  }
  
  
  
  public String msg(String key, String defaultMsg) {
    key = mKeyPrefix + key;
    
    String msg = null;
    if (mBundle != null) {
      try {
        msg = mBundle.getString(key);
      }
      catch (MissingResourceException exc) {}
    }
    
    if (msg == null) {
      if (mBundle != null) {
        mLog.warning("Key '" + key + "' not found in resource bundle '" + mBaseName + "'");
      }
      return "[" + key + "#" + defaultMsg + "]";
    } else {
      return msg;
    }
  }
  
}
