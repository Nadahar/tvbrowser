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

import java.util.*;
import java.text.MessageFormat;

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
 * <li>The defaultValue is the value you would expect on an english system. (But
 *     note: Even on an english system you may get a different String!)</li>
 * <li>The optional <CODE>args</CODE> are arguments that will be parsed into the
 *     message. See {@link java.text.MessageFormat} for details.
 * </li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class Localizer {

  /** The logger for this class. */  
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(Localizer.class.getName());

  /**
   * To avoid the creation of to much object, this array is used for calls using
   * one argument.
   */  
  private static final Object[] ONE_ARG_ARR = new Object[1];
  
  /**
   * To avoid the creation of to much object, this array is used for calls using
   * two arguments.
   */  
  private static final Object[] TWO_ARGS_ARR = new Object[2];
  
  /**
   * To avoid the creation of to much object, this array is used for calls using
   * three arguments.
   */  
  private static final Object[] THREE_ARGS_ARR = new Object[3];
  
  /** Contains for a Class (key) a Localizer (value). */
  private static HashMap mLocalizerCache = new HashMap();
    
  /** The base name of the ResourceBundle used by this Localizer. */  
  private String mBaseName;
  
  /** The ResourceBundle used by this Localizer. */  
  private ResourceBundle mBundle;
  
  /**
   * Because one ResourceBundle is used by all the Localizers of classes from the
   * same package, a prefix is added to the key to distinguish the keys from different
   * classes.
   */  
  private String mKeyPrefix;
  
  

  /**
   * Creates a new instance of Localizer.
   *
   * @param clazz The Class to create the Localizer for.
   */
  private Localizer(Class clazz) {
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
    
    Locale locale = Locale.getDefault();
   
    try {
      mBundle = ResourceBundle.getBundle(mBaseName, locale, clazz.getClassLoader());
    }
    catch (MissingResourceException exc) {
      mLog.warning("ResourceBundle not found: '" + mBaseName + "'");
    }
  }
  
  
  
  /**
   * Gets the Localizer for the specified Class.
   *
   * @param clazz The Class to get the localizer for.
   * @return the Localizer for the specified Class.
   */  
  public static Localizer getLocalizerFor(Class clazz) {
    Localizer localizer = (Localizer) mLocalizerCache.get(clazz);
    
    if (localizer == null) {
      localizer = new Localizer(clazz);
      mLocalizerCache.put(clazz, localizer);
    }
    
    return localizer;
  }

  
  
  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message. (english)
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
   * @param defaultMsg The default message. (english)
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
   * @param defaultMsg The default message. (english)
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
   * @param defaultMsg The default message. (english)
   * @param args The arguments that should replace the appropriate place holder.
   *        See {@link java.text.MessageFormat} for details.
   * @return a localized message.
   */  
  public String msg(String key, String defaultMsg, Object[] args) {
    String msg = msg(key, defaultMsg);
    
    // Workaround: The MessageFormat uses the ' char for quoting strings.
    //             so the "{0}" in "AB '{0}' CD" will not be replaced.
    //             In order to avoid this we quote every ' with '', so
    //             everthing will be replaced as expected.
    msg = IOUtilities.replace(msg, "'", "''");
    
    return MessageFormat.format(msg, args);    
  }
  
  
  
  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message. (english)
   * @return a localized message.
   */  
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
