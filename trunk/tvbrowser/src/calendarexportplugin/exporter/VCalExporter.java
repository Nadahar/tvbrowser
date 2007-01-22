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
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import calendarexportplugin.CalendarExportPlugin;
import calendarexportplugin.utils.VCalFile;

import util.program.AbstractPluginProgramFormating;
import util.ui.ExtensionFileFilter;
import util.ui.Localizer;

import devplugin.Program;

/**
 * Exporter for vCal-Files
 *  
 * @author bodum
 */
public class VCalExporter extends AbstractExporter {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(VCalExporter.class);
  /** Property for Path for File */
  public static final String SAVE_PATH = "VCAL_SAVE_PATH";
  /** Path for File*/
  private String mSavePath;

  /*
   * (non-Javadoc)
   * @see calendarexportplugin.exporter.ExporterIf#getName()
   */
  public String getName() {
    return mLocalizer.msg("name","vCal File");
  }

  /*
   * (non-Javadoc)
   * @see calendarexportplugin.exporter.ExporterIf#exportPrograms(devplugin.Program[], java.util.Properties)
   */
  public boolean exportPrograms(Program[] programs, Properties settings, AbstractPluginProgramFormating formating) {
    mSavePath = settings.getProperty(SAVE_PATH);
    
    File file = chooseFile();
    
    if (file == null) {
      return false;
    }
    
    if (file.exists()) {
      int result = JOptionPane.showConfirmDialog(CalendarExportPlugin.getInstance().getBestParentFrame(), mLocalizer.msg("overwriteMessage",
          "The File \n{0}\nalready exists. Overwrite it?", file.getAbsolutePath()), mLocalizer.msg("overwriteTitle",
          "Overwrite?"), JOptionPane.YES_NO_OPTION);
      if (result != JOptionPane.YES_OPTION) {
        return false;
      }
    }

    mSavePath = file.getAbsolutePath();
    
    settings.setProperty(SAVE_PATH, mSavePath);
    
    new VCalFile().exportVCal(file, programs, settings, formating);

    return true;
  }

  /**
   * Shows a Filechooser for vCal Files.
   * 
   * @return selected File
   */
  private File chooseFile() {
    JFileChooser select = new JFileChooser();

    ExtensionFileFilter vCal = new ExtensionFileFilter("vcs", "vCal (*.vcs)");
    select.addChoosableFileFilter(vCal);

    if (mSavePath != null) {
      select.setSelectedFile(new File(mSavePath));
      select.setFileFilter(vCal);
    }

    if (select.showSaveDialog(CalendarExportPlugin.getInstance().getBestParentFrame()) == JFileChooser.APPROVE_OPTION) {

      String filename = select.getSelectedFile().getAbsolutePath();

      String ext= ".vcs";

      if (!filename.toLowerCase().endsWith(ext)) {

        if (filename.endsWith(".")) {
          filename = filename.substring(0, filename.length() - 1);
        }

        filename = filename + ext;
      }

      return new File(filename);
    }

    return null;
  }

}