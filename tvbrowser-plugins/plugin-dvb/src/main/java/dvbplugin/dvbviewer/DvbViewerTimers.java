/*
 * DvbViewerTimers.java
 * Copyright (C) 2006 Probum
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import dvbplugin.nanoxml.XMLElement;


/**
 * @author Probum
 * @author Pollaehne (pollaehne@users.sourceforge.net)
 */
public final class DvbViewerTimers {

  private static final String TIMERS_XML = "timers.xml";

  /** Translator */
  private static final Localizer localizer = Localizer.getLocalizerFor(DvbViewerTimers.class);

  private static final Logger logger = Logger.getLogger(DvbViewerTimers.class.getName());


  /**
   * Gets the entries of the XML-File, stores them into a List of ScheduledRecording objects
   * The expected format is:
   *
   * <pre>
   *  &lt;settings&gt;
   *      &lt;section name=&quot;VCR&quot;&gt;
   *          &lt;entry name=&quot;n&quot;&gt;
   * </pre>
   *
   * Where n is a positive integer and entry can be repeated as needed
   *
   * @param path the path where the timers.xml is located
   * @return List containing ScheduledRecording objects of the timers.xml
   *         entries
   */
  @SuppressWarnings("unchecked")
  public static List<ScheduledRecording> getEntries(String path) {
    ArrayList<ScheduledRecording> v = new ArrayList<ScheduledRecording>();
    BufferedReader buffer = null;
    File timersxml = new File(path, TIMERS_XML);
    try {

      if (!timersxml.exists() || !timersxml.isFile() || 0 == timersxml.length()) { return v; }

      // parse the file
      XMLElement settings = new XMLElement();
      buffer = new BufferedReader(new FileReader(timersxml));
      settings.parseFromReader(buffer);

      // check for the root element "settings"
      if ("settings".equals(settings.getName())) {
        Enumeration<XMLElement> sections = settings.enumerateChildren();
        while (sections.hasMoreElements()) {
          // check each child if it is an element of type "section" with an attribute name="VCR"
          XMLElement child = sections.nextElement();
          String name = child.getStringAttribute("name");
          if ("section".equals(child.getName()) && "VCR".equals(name)) {
            Enumeration<XMLElement> entries = child.enumerateChildren();
            while (entries.hasMoreElements()) {
              // check each child if it is an element of type "entry"
              XMLElement entry = entries.nextElement();
              if ("entry".equals(entry.getName())) {
                String content = entry.getContent();
                if (null != content && 0 < content.length()) {
                  // the entry contains data so add them
                  logger.log(Level.FINE, "Adding " + TIMERS_XML + " entry: {0}" , content);
                  v.add(new ScheduledRecording(content));
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      ErrorHandler.handle(localizer.msg("err_timers_reading",
                                        "Unable to read the scheduled DVBViewer recordings from '{0}'",
                                        timersxml.getAbsoluteFile()), e);
    } finally {
      if (null != buffer) {
        try {
          buffer.close();
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Could not close the file " + timersxml.getAbsolutePath(), e);
        }
      }
    }
    return v;
  }


  /**
   * Stores the entries located in the Vector to the timers.xml
   *
   * @param path the path where the timers.xml is located
   * @param v the Vector with the entries
   */
  public static void setEntries(String path, List<ScheduledRecording> v) {
    int size = v.size();
    BufferedWriter writer = null;
    File timersxml = new File(path, TIMERS_XML);
    try {
      writer = new BufferedWriter(new FileWriter(timersxml));

      XMLElement xml = new XMLElement();
      xml.setName("settings");

      // write the header
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      writer.newLine();

      if (0 < size) {
        // create the section
        XMLElement section = new XMLElement();
        section.setName("section");
        section.setAttribute("name", "VCR");
        xml.addChild(section);

        // write each single entry
        for (int i = 0; i < v.size(); i++) {
          XMLElement entry = new XMLElement();
          String content = v.get(i).getEntry();
          entry.setName("entry");
          entry.setIntAttribute("name", i);
          logger.log(Level.FINE, "Writing " + TIMERS_XML + " entry {0}: {1}",
                     new Object[] {String.valueOf(i) , content});
          entry.setContent(content);
          section.addChild(entry);
        }
      }

      // write the structure
      xml.write(writer);

      writer.flush();
    } catch (IOException e) {
      ErrorHandler.handle(localizer.msg("err_timers_reading",
                                        "Unable to write the scheduled DVBViewer recordings to '{0}'",
                                        timersxml.getAbsoluteFile()), e);
    } finally {
      if (null != writer) {
        try {
          writer.close();
        } catch (IOException e) {
          // at least we tried it
          logger.log(Level.SEVERE, "Could not close the file " + timersxml.getAbsolutePath(), e);
        }
      }
    }
  }
}
