/*
 * DvbViewerSetup.java
 * Copyright (C) 2006 UP
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
 *     $Date: $
 *   $Author: $
 * $Revision: $
 */

package dvbplugin.dvbviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import dvbplugin.nanoxml.XMLElement;


/**
 * @author Pollaehne (pollaehne@users.sourceforge.net)
 */
public final class DvbViewerSetup {

  /** name of the key for the default value of A/V disabling */
  public static final String DEF_REC_AVDISABLED = "DefRecAVDisabled";
  /** name of the key for the default value of action after recording */
  public static final String DEF_AFTER_RECORD = "DefAfterRecord";
  /** name of the key for the default value of action before recording */
  public static final String DEF_REC_ACTION = "DefRecAction";
  /** name of the key for the default value of time before recording */
  public static final String EPGAFTER = "EPGAfter";
  /** name of the key for the default value of time after recording */
  public static final String EPGBEFORE = "EPGBefore";
  /** name of the key for the using the dvbviewer task scheduler */
  public static final String USESCHEDULER = "UseScheduler";
  /** name of the DVBViewer's configuration file */
  private static final String SETUP_XML = "setup.xml";

  /** Translator */
  private static final Localizer localizer = Localizer.getLocalizerFor(DvbViewerSetup.class);

  private static Logger logger = Logger.getLogger(DvbViewerSetup.class.getName());


  /**
   * Tries to find entries in the setup.xml whose name is a key in the <code>items</code> Map
   * and stores the corresponding value into the Map
   *
   * The expected format of setup.xml is:
   *
   * <pre>
   *  &lt;settings&gt;
   *      &lt;section name=&quot;General&quot;&gt;
   *          &lt;entry name=&quot;<b>X</b>&quot;&gt;
   * </pre>
   * Where X is one of the keys in the <code>items</code> map
   *
   * @param path the path where the timers.xml is located
   * @return Map containing the name and value as String of the above entries from setup.xml
   */
  public static void getEntries(String path, Map<String, String> items) {
    BufferedReader buffer = null;
    File settingsxml = new File(path, SETUP_XML);
    try {

      if (!settingsxml.exists() || !settingsxml.isFile() || 0 == settingsxml.length()) { return; }

      // parse the file
      XMLElement settings = new XMLElement();
      buffer = new BufferedReader(new FileReader(settingsxml));
      settings.parseFromReader(buffer);

      // check for the root element "settings"
      if ("settings".equals(settings.getName())) {
        for (Enumeration<XMLElement> sections = settings.enumerateChildren(); sections.hasMoreElements();) {

          // check each child if it is an element of type "section" with an attribute name="General"
          XMLElement child = sections.nextElement();
          String name = child.getStringAttribute("name");
          if ("section".equals(child.getName()) && "General".equals(name)) {

            for (Enumeration<XMLElement> entries = child.enumerateChildren(); entries.hasMoreElements();) {

              // check each child if it is an element of type "entry"
              XMLElement entry = entries.nextElement();
              if ("entry".equals(entry.getName())) {

                // check if it is one of the interesting ones
                name = entry.getStringAttribute("name");
                for (Entry<String, String> mapEntry : items.entrySet()) {
                  String key = mapEntry.getKey();
                  if (key.equals(name)) {
                    String content = entry.getContent();
                    if (null != content && 0 < content.length()) {
                      logger.log(Level.FINE, "Found " + key + " with value {0}", content);
                      mapEntry.setValue(content);
                      break;
                    }
                  }
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      ErrorHandler.handle(localizer.msg("err_setup_reading",
                                        "Unable to read DVBViewer configuration from '{0}'",
                                        settingsxml.getAbsoluteFile()), e);
    } finally {
      if (null != buffer) {
        try {
          buffer.close();
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Could not close the file " + settingsxml.getAbsolutePath(), e);
        }
      }
    }
  }


  /**
   * Stores the items in the Map into setup.xml if they have changed compared to the
   * value in the setup.xml
   *
   * Attention: The Map <code>items</code> will be empty on return
   *
   * @param path the path where the setup.xml is located
   * @param items the Map with the items to be written
   */
  public static void setEntries(String path, Map<String, String> items) {
    int size = items.size();

    if (0 == size) {return;}

    File setupxml = new File(path, SETUP_XML);
    BufferedReader reader = null;
    BufferedWriter writer = null;
    try {
      boolean allProcessed = false;
      boolean isChanged = false;
      reader = new BufferedReader(new FileReader(setupxml));
      XMLElement settings = new XMLElement();

      settings.parseFromReader(reader);
      reader.close();
      reader = null;

      // check for the root element "settings"
      if ("settings".equals(settings.getName())) {
        Enumeration<XMLElement> sections = settings.enumerateChildren();
        while (!allProcessed && sections.hasMoreElements()) {
          // check each child if it is an element of type "section" with an attribute name="General"
          XMLElement child = sections.nextElement();
          String name = child.getStringAttribute("name");
          if ("section".equals(child.getName()) && "General".equals(name)) {
            Enumeration<XMLElement> entries = child.enumerateChildren();
            while (!allProcessed && entries.hasMoreElements()) {
              // check each child if it is an element of type "entry"
              XMLElement entry = entries.nextElement();
              if ("entry".equals(entry.getName())) {
                // check if it is one of the interesting ones
                name = entry.getStringAttribute("name");
                Iterator<String> keys = items.keySet().iterator();
                while (keys.hasNext()) {
                  String key = keys.next();
                  if (key.equals(name)) {
                    String content = entry.getContent();
                    String value = items.get(key);
                    if (value.equals(content)) {
                      logger.log(Level.FINE, "Value of key " + key + " remains unchanged");
                    } else {
                      logger.log(Level.FINE, "Setting " + key + " to value " + value);
                      entry.setContent(value);
                      isChanged = true;
                    }

                    // item processed remove it
                    items.remove(key);
                    if (0 == items.size()) {
                      allProcessed = true;
                    }

                    break;
                  }
                }
              }
            }
          }
        }
      }

      if (!isChanged) {return;}

      writer = new BufferedWriter(new FileWriter(setupxml));

      // write the header
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      writer.newLine();

      // write the structure
      settings.write(writer);

      writer.flush();
    } catch (IOException e) {
      ErrorHandler.handle(localizer.msg("err_setup_reading",
                                        "Unable to write DVBViewer configuration to '{0}'",
                                        setupxml.getAbsoluteFile()), e);
    } finally {
      if (null != reader) {
        try {
          reader.close();
        } catch (IOException e) {
          // at least we tried it
          logger.log(Level.SEVERE, "Could not close the file " + setupxml.getAbsolutePath() + " after reading.", e);
        }
      }
      if (null != writer) {
        try {
          writer.close();
        } catch (IOException e) {
          // at least we tried it
          logger.log(Level.SEVERE, "Could not close the file " + setupxml.getAbsolutePath() + " after writing.", e);
        }
      }
    }
  }
}
