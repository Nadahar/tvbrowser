/*
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
 *     $Date: 2005-12-26 21:46:18 +0100 (Mo, 26 Dez 2005) $
 *   $Author: troggan $
 * $Revision: 1764 $
 */
package calendarexportplugin.exporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import util.misc.OperatingSystem;

/**
 * Factory for the ExporterIf
 *
 * @author bodum
 */
public class ExporterFactory {
  /** List of all exporters */
  private ArrayList<ExporterIf> mExporterList;
  /** List of active exporters*/
  private ArrayList<ExporterIf> mActiveExporter;

  /**
   * Create the Factory
   */
  public ExporterFactory() {
    mExporterList = new ArrayList<ExporterIf>();
    if (OperatingSystem.isMacOs()) {
      mExporterList.add(new AppleiCalExporter());
    }
    mExporterList.add(new AppleiCalExporter());
    mExporterList.add(new GoogleExporter());
    mExporterList.add(new ICalExporter());
    mExporterList.add(new VCalExporter());

    if (OperatingSystem.isOther() || OperatingSystem.isLinux()) {
      mExporterList.add(new KOrganizerExporter());
    } else if (OperatingSystem.isWindows() && !OperatingSystem.isWindows64()) {
      mExporterList.add(new OutlookExporter());
    }

    sortList(mExporterList);

    mActiveExporter = new ArrayList<ExporterIf>(mExporterList);
  }

  private void sortList(ArrayList<ExporterIf> list) {
    Collections.sort(list, new Comparator<ExporterIf>() {
      public int compare(ExporterIf o1, ExporterIf o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });
  }

  /**
   * Set list of active exporters
   * @param exporters List of active exporters
   */
  public void setActiveExporters(ExporterIf[] exporters) {
    mActiveExporter = new ArrayList<ExporterIf>(Arrays.asList(exporters));
    sortList(mActiveExporter);
  }

  /**
   * @return List of active exporters
   */
  public ExporterIf[] getActiveExporters() {
    return mActiveExporter.toArray(new ExporterIf[mActiveExporter.size()]);
  }

  /**
   * @return All exporters
   */
  public ExporterIf[] getAllExporters() {
    return mExporterList.toArray(new ExporterIf[mExporterList.size()]);
  }

  /**
   * @return String representation of all active exporters
   */
  public String getListOfActiveExporters() {
    if (mActiveExporter.size() == 0) {
      return "";
    }

    StringBuilder classes = new StringBuilder();

    for (ExporterIf exporter : mActiveExporter) {
      classes.append(exporter.getClass().getName());
      classes.append(':');
    }

    return classes.substring(0, classes.length() - 1);
  }

  /**
   * @param property set list of active exporters from a string
   */
  public void setListOfActiveExporters(String property) {
    if (property == null) {
      // Set the Defaults
      if (OperatingSystem.isWindows()) {
        property = "calendarexportplugin.exporter.GoogleExporter:calendarexportplugin.exporter.ICalExporter:calendarexportplugin.exporter.VCalExporter:calendarexportplugin.exporter.OutlookExporter";
      } else if (OperatingSystem.isMacOs()) {
        property = "calendarexportplugin.exporter.AppleiCalExporter:calendarexportplugin.exporter.GoogleExporter:calendarexportplugin.exporter.ICalExporter:calendarexportplugin.exporter.VCalExporter";
      } else {
        property = "calendarexportplugin.exporter.KOrganizerExporter:calendarexportplugin.exporter.GoogleExporter:calendarexportplugin.exporter.ICalExporter:calendarexportplugin.exporter.VCalExporter";
      }
    }

    mActiveExporter = new ArrayList<ExporterIf>();

    String[] classes = property.split("\\:");
    for (String classname : classes) {
      for (ExporterIf exporter : mExporterList) {
        if (exporter.getClass().getName().equals(classname)) {
          mActiveExporter.add(exporter);
        }
      }
    }
    sortList(mActiveExporter);
  }

}