/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.extras.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import tvbrowser.core.Settings;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;


/**
 * ConfigurationHandler is used to load and store configurations.
 */
public class ConfigurationHandler {

  private String mFilePrefix;

  public ConfigurationHandler(String filePrefix) {
    mFilePrefix = filePrefix;
  }


  public void loadData(DataDeserializer deserializer) throws IOException {
    String userDirectoryName = Settings.getUserSettingsDirName();
     File userDirectory = new File(userDirectoryName);
     File datFile = new File(userDirectory, "java."+mFilePrefix + ".dat");

     if (datFile.exists()) {
       ObjectInputStream in = null;
       try {
         in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(datFile), 0x4000));
         deserializer.read(in);
       }
       catch (ClassNotFoundException e) {
         throw new IOException("Could not read file "+datFile.getAbsolutePath());
       }
       finally {
         if (in != null) {
           try { in.close(); } catch (IOException exc) {
             // ignore
           }
         }
       }
     }

  }

  public void storeData(final DataSerializer serializer) throws IOException {
    String userDirectoryName = Settings.getUserSettingsDirName();
    File userDirectory = new File(userDirectoryName);

    File tmpDatFile = new File(userDirectory, mFilePrefix + ".dat.temp");
    File datFile = new File(userDirectory, "java." + mFilePrefix + ".dat");

    StreamUtilities.objectOutputStream(tmpDatFile,
        new ObjectOutputStreamProcessor() {
          public void process(ObjectOutputStream out) throws IOException {
            serializer.write(out);
            out.close();
          }
        });

    // Saving succeeded -> Delete the old file and rename the temp file
    datFile.delete();
    tmpDatFile.renameTo(datFile);
  }

  public Properties loadSettings() throws IOException {
    String userDirectoryName = Settings.getUserSettingsDirName();
    File propFile = new File(userDirectoryName, "java." + mFilePrefix + ".prop");
    BufferedInputStream in = null;
    try {
      if (propFile.exists()) {
        Properties prop = new Properties();
        in = new BufferedInputStream(new FileInputStream(propFile), 0x4000);
        prop.load(in);
        in.close();
        return prop;
      } else {
        return new Properties();
      }
    }
    catch (IOException thr) {
      throw new IOException("Could not read settings from "+propFile.getAbsolutePath());
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (IOException exc) {
          // ignore
        }
      }
    }
  }

  public void storeSettings(Properties settings) throws IOException {
    // save settings in a temp file
    String userDirectoryName = Settings.getUserSettingsDirName();
    FileOutputStream fOut = null;
    File tmpPropFile = new File(userDirectoryName, mFilePrefix + ".prop.temp");
    try {
      if (settings != null) {
        fOut = new FileOutputStream(tmpPropFile);
        settings.store(fOut, "Settings");
        fOut.close();
      }

      // Saving succeeded -> Delete the old file and rename the temp file
      File propFile = new File(userDirectoryName, "java." + mFilePrefix + ".prop");
      propFile.delete();
      tmpPropFile.renameTo(propFile);
    }
    catch (Throwable thr) {
      throw new IOException("Could not store settings to " + tmpPropFile.getAbsolutePath());
    }
    finally {
      if (fOut != null) {
        try { fOut.close(); } catch (IOException exc) {
          // ignore
        }
      }
    }
  }

}
