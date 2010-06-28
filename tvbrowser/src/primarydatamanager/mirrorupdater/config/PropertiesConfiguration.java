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
package primarydatamanager.mirrorupdater.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import primarydatamanager.mirrorupdater.UpdateException;
import primarydatamanager.mirrorupdater.data.DataSource;
import primarydatamanager.mirrorupdater.data.DataTarget;
import primarydatamanager.mirrorupdater.data.FileDataSource;
import primarydatamanager.mirrorupdater.data.FileDataTarget;
import primarydatamanager.mirrorupdater.data.FtpDataTarget;
import primarydatamanager.mirrorupdater.data.HttpDataSource;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class PropertiesConfiguration implements Configuration {

  private static final Logger mLog = Logger.getLogger(PropertiesConfiguration.class.getName());
    
  private static final String PRIMARY_SERVER_URL = "http://www.tvbrowser.org";
  
  private DataSource mDataSource;
  private DataTarget mDataTarget;
  private String[] mChannelgroups;

  public PropertiesConfiguration(String propertiesFileName)
    throws UpdateException
  {
    // Load the properties file
    Properties prop = new Properties();
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(propertiesFileName);
      prop.load(stream);
    }
    catch (IOException exc) {
      throw new UpdateException("Loading properties file failed: "
        + propertiesFileName, exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
    
    // Init the data source
    try {
      String type = getProperty(prop, "dataSource.type", "http");
      if (type.equals("http")) {
        String url = getProperty(prop, "dataSource.url", getPrimaryServerUrl());
        mDataSource = new HttpDataSource(url);
      } else if (type.equals("file")) {
        String dir = getProperty(prop, "dataSource.dir");
        mDataSource = new FileDataSource(new File(dir));
      } else {
        throw new UpdateException("dataSource.type must be either 'http' or 'file'");
      }
    }
    catch (Exception exc) {
      throw new UpdateException("Error initializing data source from "
        + "properties file: " + propertiesFileName, exc);
    }
    
    // Init the data target
    try {
      String type = getProperty(prop, "dataTarget.type", "ftp");
      if (type.equals("ftp")) {
        String url = getProperty(prop, "dataTarget.url");
        String path = getProperty(prop, "dataTarget.path", "tvdata");
        int port = getIntProperty(prop, "dataTarget.port", 21);
        String user = getProperty(prop, "dataTarget.user");
        String password = getProperty(prop, "dataTarget.password");
        mDataTarget = new FtpDataTarget(url, path, port, user, password);
      } else if (type.equals("file")) {
        String dir = getProperty(prop, "dataTarget.dir");
        mDataTarget = new FileDataTarget(new File(dir));
      } else {
        throw new UpdateException("dataTarget.type must be either 'ftp' or 'file'");
      }
    }
    catch (Exception exc) {
      throw new UpdateException("Error initializing data target from "
        + "properties file: " + propertiesFileName, exc);
    }
    
     // Get the channel groups
     
     String s = prop.getProperty("groups");
     if (s==null) {
       mChannelgroups=null;
     }
     else {
       mChannelgroups=s.split(":");
     }
    
  }



  private String getProperty(Properties prop, String key, String defaultValue) {
    String value = prop.getProperty(key);
    if (value == null) {
      mLog.fine("Property '" + key + "' not set. Using " + defaultValue);
      return defaultValue;
    } else {
      return value;
    }
  }



  private String getProperty(Properties prop, String key)
    throws UpdateException
  {
    String value = prop.getProperty(key);
    if (value == null) {
      throw new UpdateException("Property '" + key + "' not set");
    } else {
      return value;
    }
  }



  private int getIntProperty(Properties prop, String key, int defaultValue)
    throws UpdateException
  {
    String value = prop.getProperty(key);
    if (value == null) {
      mLog.fine("Property '" + key + "' not set. Using " + defaultValue);
      return defaultValue;
    } else {
      value = value.trim();
      try {
        return Integer.parseInt(value);
      }
      catch (Exception exc) {
        throw new UpdateException("Property '" + key + "' must be a number: '"
          + value + "'", exc);
      }
    }
  }



  public DataSource getDataSource() {
    return mDataSource;
  }



  public DataTarget getDataTarget() {
    return mDataTarget;
  }



  public String getPrimaryServerUrl() {
    return PRIMARY_SERVER_URL;
  }

  public String[] getChannelgroups() {
    return mChannelgroups;
  }

}
