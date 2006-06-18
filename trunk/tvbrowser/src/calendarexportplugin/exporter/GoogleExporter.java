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

import java.util.Properties;

import devplugin.Program;

/**
 * Exporter for Google Calendar API
 *  
 * @author bodum
 */
public class GoogleExporter extends AbstractExporter {

  /*
   * (non-Javadoc)
   * @see calendarexportplugin.exporter.ExporterIf#getName()
   */
  public String getName() {
    return "Google Calendar";
  }

  /*
   * (non-Javadoc)
   * @see calendarexportplugin.exporter.ExporterIf#exportPrograms(devplugin.Program[], java.util.Properties)
   */
  public boolean exportPrograms(Program[] programs, Properties settings) {
    System.out.println("GOOGLE!");
    return true;
  }

  /*
   * (non-Javadoc)
   * @see calendarexportplugin.exporter.AbstractExporter#hasSettingsDialog()
   */
  @Override
  public boolean hasSettingsDialog() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see calendarexportplugin.exporter.AbstractExporter#showSettingsDialog(java.util.Properties)
   */
  @Override
  public void showSettingsDialog(Properties settings) {
  }

}