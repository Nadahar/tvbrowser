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

import java.io.File;

import util.program.AbstractPluginProgramFormating;
import util.ui.Localizer;
import calendarexportplugin.CalendarExportSettings;
import calendarexportplugin.utils.ICalFile;
import devplugin.Program;

/**
 * Exporter for iCal-Files
 * 
 * @author bodum
 */
public class ICalExporter extends CalExporter {

  public ICalExporter() {
    super("ics", "iCal (*.ics)");
  }

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ICalExporter.class);
  /** Property for Path for File */
  private static final String SAVE_PATH = "ICAL_SAVE_PATH";
  
  public String getName() {
    return mLocalizer.msg("name","iCal File");
  }

  protected String getSavePath(CalendarExportSettings settings) {
    return settings.getExporterProperty(SAVE_PATH);
  }

  protected void setSavePath(CalendarExportSettings settings, String path) {
    settings.setExporterProperty(SAVE_PATH, path);
  }

  @Override
  protected void export(File file, Program[] programs, CalendarExportSettings settings,
      AbstractPluginProgramFormating formating) {
    new ICalFile().export(file, programs, settings, formating);
  }

}