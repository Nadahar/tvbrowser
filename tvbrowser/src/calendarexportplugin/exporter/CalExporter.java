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
 * VCS information:
 *     $Date: 2005-12-26 21:46:18 +0100 (Mo, 26 Dez 2005) $
 *   $Author: troggan $
 * $Revision: 1764 $
 */
package calendarexportplugin.exporter;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import util.program.AbstractPluginProgramFormating;
import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import calendarexportplugin.CalendarExportPlugin;
import calendarexportplugin.CalendarExportSettings;
import calendarexportplugin.utils.CalendarToolbox;
import devplugin.Program;

public abstract class CalExporter extends AbstractExporter {

  /** Translator */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(CalExporter.class);

  /** Path for File */
  private String mSavePath;

  private String mExtension;
  private String mExtensionFilter;

  public CalExporter(String extension, String extensionFilter) {
    mExtension = extension;
    mExtensionFilter = extensionFilter;
  }

  public boolean exportPrograms(Program[] programs, CalendarExportSettings settings,
      AbstractPluginProgramFormating formatting) {
    mSavePath = getSavePath(settings);

    File file = chooseFile(programs);

    if (file == null) {
      return false;
    }

    if (file.exists()) {
      int result = JOptionPane.showConfirmDialog(CalendarExportPlugin
          .getInstance().getBestParentFrame(), mLocalizer.msg(
          "overwriteMessage", "The File \n{0}\nalready exists. Overwrite it?",
          file.getAbsolutePath()), mLocalizer.msg("overwriteTitle",
          "Overwrite?"), JOptionPane.YES_NO_OPTION);
      if (result != JOptionPane.YES_OPTION) {
        return false;
      }
    }

    mSavePath = file.getAbsolutePath();

    setSavePath(settings, mSavePath);
    export(file, programs, settings, formatting);

    return true;
  }

  /**
   * Shows a file chooser for calendar Files.
   *
   * @return selected File
   * @param programs
   *          programs that are exported
   */
  private File chooseFile(Program[] programs) {
    JFileChooser select = new JFileChooser();

    ExtensionFileFilter vCal = new ExtensionFileFilter(mExtension,
        mExtensionFilter);
    select.addChoosableFileFilter(vCal);
    String ext = "." + mExtension;

    if (mSavePath != null) {
      select.setSelectedFile(new File(mSavePath));
      select.setFileFilter(vCal);
    }

    // check if all programs have same title. if so, use as filename
    String fileName = programs[0].getTitle();
    for (int i = 1; i < programs.length; i++) {
      if (!programs[i].getTitle().equals(fileName)) {
        fileName = "";
      }
    }

    fileName = CalendarToolbox.cleanFilename(fileName);

    if (StringUtils.isNotEmpty(fileName)) {
      if (mSavePath == null) {
        mSavePath = "";
      }
      select.setSelectedFile(new File((new File(mSavePath).getParent())
          + File.separator + fileName + ext));
    }

    if (select.showSaveDialog(CalendarExportPlugin.getInstance()
        .getBestParentFrame()) == JFileChooser.APPROVE_OPTION) {

      String filename = select.getSelectedFile().getAbsolutePath();

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

  protected abstract String getSavePath(CalendarExportSettings settings);
  protected abstract void setSavePath(CalendarExportSettings settings, String path);
  protected abstract void export(File file, Program[] programs,
      CalendarExportSettings settings, AbstractPluginProgramFormating formatting);

}
