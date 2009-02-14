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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import util.ui.Localizer;
import captureplugin.CapturePluginData;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverFactory;
import captureplugin.tabs.DevicePanel;

/**
 * This Class handles the import and export of Device-Files
 * 
 * @author Bodo Tasche
 */
public class DeviceImportAndExport {

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(DeviceImportAndExport.class);
  
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
   * @param data
   *          CapturePlugin-Data
   * @param panel
   *          Panel is needed for JDialogs
   * @param file
   *          File to Import
   * @return Imported Device, null if an error occurred
   */
  public DeviceIf importDevice(CapturePluginData data, JPanel panel, File file) {
    mError = "";
    mException = new Exception();
    
    if (!file.exists()) {
      mError = mLocalizer.msg("FileNotExists","Selected File doesn't exist");
      return null;
    }

    try {
      ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file), 0x4000));
      
      in.readInt(); // version not yet used
      
      in.readObject(); // Warning in File
      
      String classname = (String) in.readObject();
      String devname = (String)in.readObject();
      
      DeviceIf dev = DriverFactory.getInstance().createDevice(classname, devname);
      
      if (dev == null) {
          mError = mLocalizer.msg("ProblemsCreating","Problems while creating the Device");
          return null;
      }

      dev.readData(in,true);
      
      return dev;
    } catch (Exception e) {
      mError = mLocalizer.msg("ProblemsReading","Problems while reading the File");
      mException = e;
      return null;
    }
  }

  /**
   * Export a Device
   * 
   * @param panel Panel is needed for JDialogs
   * @param device the Device to Export
   * @param file File to Export
   * @return true if successfully
   */
  public boolean exportDevice(DevicePanel panel, final DeviceIf device,
      File file) {
    mError = "";
    mException = new Exception();

    if (file.exists()) {
      if (JOptionPane.showConfirmDialog(panel, 
          mLocalizer.msg("ReplaceFile","Do you want to replace the existing File?"), 
          mLocalizer.msg("Replace","Replace?"), 
          JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
        return true;
      }
    }
    
    try {
      StreamUtilities.objectOutputStream(file,
          new ObjectOutputStreamProcessor() {
            public void process(ObjectOutputStream out) throws IOException {
              out.writeInt(1);
              out.writeObject("\n\nDon't touch this ;)\n\n");

              out.writeObject(device.getDriver().getClass().getName());
              out.writeObject(device.getName());

              device.writeData(out);

              out.close();
            }
          });
    } catch (Exception e) {
      mError = mLocalizer.msg("ProblemsWriting","Problems while writing the File");
      mException = e;
      return false;
    }
    
    return true;
  }

}