/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package util.settings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class PropertyManager {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(PropertyManager.class.getName());
  
  private Properties mProperties;
  private HashMap<String, Property> mPropertyHash;
  private HashSet<String> mChangedKeySet;
  
  
  
  public PropertyManager() {
    mProperties = new Properties();
    mPropertyHash = new HashMap<String, Property>();
    mChangedKeySet = new HashSet<String>();
    
    
  }


  public void writeToFile(File settingsFile) throws IOException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(settingsFile);
      mProperties.store(out, null);
    }
    finally {
      if (out != null) {
        out.close();
      }
    }
  }


  public void readFromFile(File settingsFile) throws IOException {
    BufferedInputStream in = null;
    try {
      in = new BufferedInputStream(new FileInputStream(settingsFile), 0x4000);
      mProperties.load(in);
      
      clearCaches();
      removeUnknownEntries();
    }
    finally {
      if (in != null) {
        in.close();
      }
    }
  }
  
  
  void addProperty(Property prop) {
    mPropertyHash.put(prop.getKey(), prop);
  }
  
  
  void setProperty(String key, String value) {
    String oldVal = getProperty(key);
    boolean equalsOld = (oldVal == null) ? (oldVal == value) : oldVal.equals(value);
    
    if (! equalsOld) {
      if (value == null) {
        mProperties.remove(key);
      } else {
        mProperties.setProperty(key, value);
      }

      mChangedKeySet.add(key);
    }
  }
  
  
  String getProperty(String key) {
    return mProperties.getProperty(key);
  }


  public boolean hasChanged(Property prop) {
    return mChangedKeySet.contains(prop.getKey());
  }
  
  
  public boolean hasChanged(Property[] propArr) {
    for (int i = 0; i < propArr.length; i++) {
      if (mChangedKeySet.contains(propArr[i].getKey())) {
        return true;
      }
    }

    return false;
  }
  
  
  public void clearChanges() {
    mChangedKeySet.clear();
  }
  
  
  private void clearCaches() {
    Iterator iter = mPropertyHash.values().iterator();
    while (iter.hasNext()) {
      Property prop = (Property) iter.next();
      prop.clearCache();
    }
  }


  private void removeUnknownEntries() {
    Iterator iter = mProperties.keySet().iterator();
    while (iter.hasNext()) {
      String key = (String) iter.next();

      // Check whether this key is known      
      boolean isKnown = (mPropertyHash.get(key) != null);
      
      if (! isKnown) {
        mLog.info("Removing unknown setting: " + key + " ("
          + mProperties.get(key) + ")");
        iter.remove();
      }
    }
  }

}
