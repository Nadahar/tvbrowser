/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
package captureplugin.utils;

import java.io.File;

import javax.swing.JPanel;

import captureplugin.tabs.DevicePanel;

/**
 * This Class handles the im- and eexport of Device-Files
 * 
 * @author Bodo Tasche
 */
public class DeviceImportAndExport {

  /** Error-String */
  private String mError = "";

  /** Exception */
  private Exception mException = new Exception();

  /**
   * Gets the Error-Description
   * 
   * @return Error-Description
   */
  public String getError() {
    return mError;
  }

  /**
   * Gets the Exception
   * 
   * @return Exception
   */
  public Exception getException() {
    return mException;
  }

  /**
   * Import a Device
   * 
   * @param panel Panel is needed for JDialogs
   * @param selectedFile File to Import
   * @return true if successfully
   */
  public boolean importDevice(JPanel panel, File selectedFile) {
    mError = "";
    mException = new Exception();
    
    if (!selectedFile.exists()) {
      mError = "Selected File doesn't exist";
      return false;
    }

    if (selectedFile.isDirectory()) {
      mError = "Please select a File, not a Directory";
      return false;
    }

    return true;
  }

  /**
   * Export a Device
   * 
   * @param panel Panel is needed for JDialogs
   * @param file File to Export
   * @return true if successfully
   */
  public boolean exportDevice(DevicePanel panel, File file) {
    mError = "";
    mException = new Exception();

    if (file.isDirectory()) {
      mError = "Please select a File, not a Directory";
      return false;
    }

    return true;
  }

}