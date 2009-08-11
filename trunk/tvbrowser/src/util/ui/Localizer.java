/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package util.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import util.io.IOUtilities;

/**
 * Does the localization of texts.
 * <p>
 * Each class that uses String that reach the user interface has its own
 * Localizer. You can get a localized String by using one of the msg methods.
 * <p>
 * The msg methods have all the same pattern:<br>
 * <code>String msg(key, defaultValue, [args]);</code>
 * <ul>
 * <li>The <CODE>key</CODE> is a String that identifies the message. Each class
 *     has its own namespace.</li>
 * <li>The defaultValue is the value you would expect on an English system. (But
 *     note: Even on an English system you may get a different String!)</li>
 * <li>The optional <CODE>args</CODE> are arguments that will be parsed into the
 *     message. See {@link java.text.MessageFormat} for details.
 * </li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class Localizer {
  private static Localizer mLocalizer = null;
  
  public final static String I18N_OK = "i18n_ok";
  public final static String I18N_CANCEL = "i18n_cancel";  
  public final static String I18N_CLOSE = "i18n_close";
  public final static String I18N_DELETE = "i18n_delete";
  public final static String I18N_EDIT = "i18n_edit";
  public final static String I18N_PROGRAM = "i18n_program";
  public final static String I18N_PROGRAMS = "i18n_programs";
  public final static String I18N_CHANNEL = "i18n_channel";
  public final static String I18N_CHANNELS = "i18n_channels";
  public final static String I18N_HELP = "i18n_help";
  public final static String I18N_FILE = "i18n_file";
  public final static String I18N_ADD = "i18n_add";
  public final static String I18N_SETTINGS = "i18n_settings";
  public final static String I18N_UP = "i18n_up";
  public final static String I18N_DOWN = "i18n_down";
  public final static String I18N_LEFT = "i18n_left";
  public final static String I18N_RIGHT = "i18n_right";
  public final static String I18N_BACK = "i18n_back";
  public final static String I18N_NEXT = "i18n_next";
  public final static String I18N_PICTURES = "i18n_pictures";
  public final static String I18N_OPTIONS = "i18n_options";
  public final static String I18N_SELECT = "i18n_select";
  public final static String I18N_ERROR = "i18n_error";
  public final static String I18N_DEFAULT = "i18n_default";
  public final static String I18N_STANDARD = "i18n_standard";
  public final static String I18N_YESTERDAY = "i18n_yesterday";
  public final static String I18N_TODAY = "i18n_today";
  public final static String I18N_TOMORROW = "i18n_tomorrow";
  public final static String I18N_INFO = "i18n_info";
  
  /** The logger for this class. */  
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(Localizer.class.getName());
  
  private static HashMap<String, String> standardLocalizations;

  /**
   * To avoid the creation of to many objects, this array is used for calls using
   * one argument.
   */  
  private static final Object[] ONE_ARG_ARR = new Object[1];
  
  /**
   * To avoid the creation of to many objects, this array is used for calls using
   * two arguments.
   */  
  private static final Object[] TWO_ARGS_ARR = new Object[2];
  
  /**
   * To avoid the creation of to many objects, this array is used for calls using
   * three arguments.
   */  
  private static final Object[] THREE_ARGS_ARR = new Object[3];
  
  /** Contains for a Class (key) a Localizer (value). */
  private static HashMap<Class, Localizer> mLocalizerCache = new HashMap<Class, Localizer>();
    
  /** The base name of the ResourceBundle used by this Localizer. */  
  private String mBaseName;
  
  /**
   * map of localized strings of this localizer (merged translated and default strings)
   */
  private HashMap<String, String> mResource;
  
  /**
   * Because one ResourceBundle is used by all the Localizers of classes from the
   * same package, a prefix is added to the key to distinguish the keys from different
   * classes.
   */  
  private String mKeyPrefix;
  
  private ClassLoader mClassLoader;
  
  /**
   * ellipsis suffix for use in menus
   */
  private static final String ELLIPSIS = "...";

  /**
   * Creates a new instance of Localizer.
   *
   * @param clazz The Class to create the Localizer for.
   */
  protected Localizer(Class clazz) {
    initializeForClass(clazz);
  }

  protected void initializeForClass(Class clazz) {
    String className = clazz.getName();
    int lastDot = className.lastIndexOf('.');
    String packageName;
    if (lastDot == -1) {
      // This class has no package
      packageName = "";
    } else {
      packageName = className.substring(0, lastDot);
    }

    mKeyPrefix = className.substring(packageName.length() + 1) + ".";

    lastDot = packageName.lastIndexOf('.');
    if (lastDot == -1) {
      mBaseName = packageName + "." + packageName;
    } else {
      mBaseName = packageName + packageName.substring(lastDot);
    }
    
    mClassLoader = new LocalizerClassloader(clazz.getClassLoader());
    
    loadResourceBundle();
  }
  
  private void loadResourceBundle() {  
    try {
      // load the resource bundle including all parents
      ResourceBundle bundle = ResourceBundle.getBundle(mBaseName, Locale.getDefault(), mClassLoader);
      if (bundle != null) {
        // now merge the bundle and all parents into one hash map to save memory
        mResource = new HashMap<String, String>();
        for (Enumeration<String> enumKeys = bundle.getKeys(); enumKeys.hasMoreElements();) {
          String key = enumKeys.nextElement();
          if (key.startsWith(mKeyPrefix)) {
            mResource.put(key, bundle.getString(key));
          }
        }
      }
    }
    catch (MissingResourceException exc) {
      mLog.warning("ResourceBundle not found: '" + mBaseName + "'");
    }
  }

  protected static Localizer getCachedLocalizerFor(final Class clazz) {
    return mLocalizerCache.get(clazz);
  }
  
  /**
   * Gets the Localizer for the specified Class.
   *
   * @param clazz The Class to get the localizer for.
   * @return the Localizer for the specified Class.
   */  
  public static Localizer getLocalizerFor(Class clazz) {
    Localizer localizer = getCachedLocalizerFor(clazz);
    
    if (localizer == null) {
      localizer = new Localizer(clazz);
      addLocalizerToCache(clazz, localizer);
    }
    
    return localizer;
  }

  protected static void addLocalizerToCache(Class clazz, final Localizer localizer) {
    mLocalizerCache.put(clazz, localizer);
  }


  /**
   * Clears the localizer cache.
   */
  public static void emptyLocalizerCache() {
    mLocalizerCache.clear();
  }

  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message (English)
   * @param arg1 The argument that should replace <CODE>{0}</CODE>.
   * @return a localized message.
   */  
  public String msg(String key, String defaultMsg, Object arg1) {
    synchronized (ONE_ARG_ARR) {
      ONE_ARG_ARR[0] = arg1;
      
      return msg(key, defaultMsg, ONE_ARG_ARR);
    }
  }

  
  
  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message. (English)
   * @param arg1 The argument that should replace <CODE>{0}</CODE>.
   * @param arg2 The argument that should replace <CODE>{1}</CODE>.
   * @return a localized message.
   */  
  public String msg(String key, String defaultMsg, Object arg1, Object arg2) {
    synchronized (TWO_ARGS_ARR) {
      TWO_ARGS_ARR[0] = arg1;
      TWO_ARGS_ARR[1] = arg2;
      
      return msg(key, defaultMsg, TWO_ARGS_ARR);
    }
  }

  
  
  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message. (English)
   * @param arg1 The argument that should replace <CODE>{0}</CODE>.
   * @param arg2 The argument that should replace <CODE>{1}</CODE>.
   * @param arg3 The argument that should replace <CODE>{2}</CODE>.
   * @return a localized message.
   */  
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
  
  
  
  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message. (English)
   * @param args The arguments that should replace the appropriate place holder.
   *        See {@link java.text.MessageFormat} for details.
   * @return a localized message.
   */  
  public String msg(String key, String defaultMsg, Object[] args) {
    String msg = msg(key, defaultMsg);
    checkMessage(key, msg);
    
    // Workaround: The MessageFormat uses the ' char for quoting strings.
    //             so the "{0}" in "AB '{0}' CD" will not be replaced.
    //             In order to avoid this we quote every ' with '', so
    //             everything will be replaced as expected.
    msg = IOUtilities.replace(msg, "'", "''");
    
    return MessageFormat.format(msg, args);    
  }
  
  
  
  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message (English)
   * @return a localized message.
   */  
  public String msg(String key, String defaultMsg) {
    return msg(key,defaultMsg,true);
  }
  
  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message (English)
   * @param warn If warnings should be logged if key is not found.
   * @return a localized message.
   * @since 2.5.1
   */
  public String msg(String key, String defaultMsg, boolean warn) {
    key = mKeyPrefix + key;
    
    String msg = null;
    if (mResource != null) {
      try {
        msg = mResource.get(key);
        checkMessage(key, msg);
      }
      catch (MissingResourceException exc) {
        //Empty
      }
    }
    
    if (msg == null) {
      if (mResource != null && warn) {
        // Workaround: There is a bug in the logging mechanism of Java.
        //             When someone tries to log an exception which uses
        //             localization then the logging stucks in a deadlock,
        //             when the exception message was not found in the resource
        //             bundle. The reason of this is, that the following log
        //             waits until the error log has finished and the error log
        //             waits until this method returns, but this method does not
        //             return because the following log waits. -- A classical
        //             dead lock.
        // Solution:   We do the following log in another thread, so this method
        //             can return and the error log gets its message and can
        //             unlock the logging.
        final String fkey = key;
        Thread logThread = new Thread("Log missing resource") {
          public void run() {
            mLog.warning("Key '" + fkey + "' not found in resource bundle '" + mBaseName + "'");
          }
        };
        logThread.start();
      }
      return "[" + key + "#" + defaultMsg + "]";
    } else {
      return msg;
    }
  }
  
  /**
   * Scans all Language-Directories for different Versions of tvbrowser/tvbrowser.properties.
   * 
   * This is faster than analyzing all Files
   *  
   * @return all available Locales
   * @since 2.3
   */
  public Locale[] getAllAvailableLocales() {
	// always have English locale available
    ArrayList<Locale> langArray = new ArrayList<Locale>();
    langArray.add(Locale.ENGLISH);

    try {
      File jar = new File("tvbrowser.jar");

      if (!jar.exists()) {
        URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
        jar = new File(url.getFile());
      }

      // First Step: look into tvbrowser.jar
      JarFile file = new JarFile(jar);
      
      Enumeration<JarEntry> entries = file.entries();
      
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        if (name.startsWith("tvbrowser/tvbrowser_") && (name.lastIndexOf(".properties") > 0)) {
          name = name.substring(20, name.lastIndexOf(".properties"));
          langArray.add(getLocaleForString(name));
        }
      }
      
      addLocaleFiles(new File(Settings.getUserSettingsDirName() + "/lang/tvbrowser"), langArray);
      addLocaleFiles(new File("lang/tvbrowser"), langArray);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    Locale[] locales = langArray.toArray(new Locale[langArray.size()]);
    
    Arrays.sort(locales, new Comparator<Locale>() {
      public int compare(Locale o1, Locale o2) {
        return o1.getDisplayName().compareTo(o2.getDisplayName());
      }
    });
    
    return locales;
  }

  /**
   * Adds all found Locales of tvbrowser/tvbrowser.properties to the langArray
   * 
   * @param dir search this directory
   * @param langArray add found locales to this ArrayList
   */
  private void addLocaleFiles(File dir, ArrayList<Locale> langArray) {
    if (dir.exists() && dir.isDirectory()) {
      String[] files = dir.list();
      if (files != null) {
        for (String string : files) {
          if (string.startsWith("tvbrowser_") && string.endsWith(".properties")) {
            Locale loc = getLocaleForString(string.substring(10, string.lastIndexOf(".properties")));
            if (!langArray.contains(loc)) {
              langArray.add(loc);
            }
          }
        }
      }
    }
  }

  /**
   * Get the Locale for a specific String.
   * The String is in this format: "lang_country_variant"
   * 
   * @param string String with Locale
   * @return Locale
   */
  private Locale getLocaleForString(String string) {
    String[] split = string.split("_");

    if (split.length >= 3) {
      return new Locale(split[0], split[1], split[2]);
    } else if (split.length == 2) {
      return new Locale(split[0], split[1]);
    } else {
      return new Locale(split[0]);
    }
  }
  
  /**
   * get a standard localization
   * @param key one of the constant values defined in the Localizer class
   * @return localized message for key
   */
  public static String getLocalization(final String key) {
    if(mLocalizer == null) {
      mLocalizer = Localizer.getLocalizerFor(Localizer.class);
    }
    
    String value = mLocalizer.msg(key,null);
    
    if(value == null) {
      if(key.equals(I18N_OK)) {
        value = "OK";
      } else if(key.equals(I18N_CANCEL)) {
        value = "Cancel";
      } else if(key.equals(I18N_CLOSE)) {
        value = "Close";
      } else if(key.equals(I18N_DELETE)) {
        value = "Delete";
      } else if(key.equals(I18N_EDIT)) {
        value = "Edit";
      } else if(key.equals(I18N_PROGRAM)) {
        value = "Program";
      } else if(key.equals(I18N_PROGRAMS)) {
        value = "Programs";
      } else if(key.equals(I18N_CHANNEL)) {
        value = "Channel";
      } else if(key.equals(I18N_CHANNELS)) {
        value = "Channels";
      } else if(key.equals(I18N_HELP)) {
        value = "Help";
      } else if(key.equals(I18N_FILE)) {
        value = "File";
      } else if(key.equals(I18N_ADD)) {
        value = "Add";
      } else if(key.equals(I18N_SETTINGS)) {
        value = "Settings";
      } else if(key.equals(I18N_UP)) {
        value = "Up";
      } else if(key.equals(I18N_DOWN)) {
        value = "Down";
      } else if(key.equals(I18N_LEFT)) {
        value = "Left";
      } else if(key.equals(I18N_RIGHT)) {
        value = "Right";
      } else if(key.equals(I18N_BACK)) {
        value = "Back";
      } else if(key.equals(I18N_NEXT)) {
        value = "Next";
      } else if(key.equals(I18N_PICTURES)) {
        value = "Pictures";
      } else if(key.equals(I18N_OPTIONS)) {
        value = "Options";
      } else if(key.equals(I18N_SELECT)) {
        value = "Select";
      } else if(key.equals(I18N_ERROR)) {
        value = "Error";
      } else if(key.equals(I18N_DEFAULT)) {
        value = "Default";
      }
    }
    
    return value;
  }
  
  /**
   * get a standard localization with ellipsis as suffix
   * @param key one of the constant values defined in the Localizer class
   * @return localized message for key
   */
  public static String getEllipsisLocalization(final String key) {
    return ellipsisSuffix(getLocalization(key));
  }
  
  private void checkMessage(String key, String localizedMessage) {
    if (TVBrowser.isStable()) {
      return;
    }
    if (mKeyPrefix.equals("Localizer.")) {
      return;
    }
    if (standardLocalizations == null) {
      standardLocalizations = new HashMap<String, String>(20);
      HashMap<String, String> standardResource = Localizer.getLocalizerFor(Localizer.class).mResource;
      for (Entry<String, String> entry : standardResource.entrySet()) {
        String standardKey = entry.getKey();
        if (standardKey.startsWith("Localizer.")) {
          standardLocalizations.put(entry.getValue(), standardKey);
        }
      }
    }
    if (standardLocalizations.containsKey(localizedMessage)) {
      String standardKey = standardLocalizations.get(localizedMessage);
      mLog.warning("Localization of message '" + key + "' should be replaced by Localizer.getLocalization(" + standardKey.substring(0,10)+standardKey.substring(10).toUpperCase() +")");
    }
  }

  /**
   * get a localized message with an ellipsis as suffix
   * @param key localization key
   * @param defaultMessage default (English) message
   * @return localized message
   * @since 3.0
   */
  public String ellipsisMsg(final String key, final String defaultMessage) {
    return ellipsisSuffix(msg(key, defaultMessage));
  }

  private static String ellipsisSuffix(final String msg) {
    if (msg.endsWith(ELLIPSIS)) {
      return msg;
    }
    return msg + ELLIPSIS;
  }

  /**
   * Gets a localized message ending with ellipsis suffix
   *
   * @param key The key of the message.
   * @param defaultMsg The default message (English)
   * @param arg1 The argument that should replace <CODE>{0}</CODE>.
   * @return a localized message.
   */  
  public String ellipsisMsg(final String key, final String defaultMsg, final Object arg1) {
    return ellipsisSuffix(msg(key, defaultMsg, arg1));
  }
  
  /**
   * check if a given message key exists
   * @param key
   * @return
   * @since 3.0
   */
  public boolean hasMessage(final String key) {
  	return mResource.containsKey(key);
  }
}
