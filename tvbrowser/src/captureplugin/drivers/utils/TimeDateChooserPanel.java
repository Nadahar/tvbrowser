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
package captureplugin.drivers.utils;

import java.awt.Color;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import util.ui.CaretPositionCorrector;

/**
 * A Panel that gives the possibility to choose date/time
 * 
 */
public class TimeDateChooserPanel extends JPanel {

  /** The Spinner-Model */
  private SpinnerDateModel mDateModel;

  private JSpinner mSpinner;
  
  /**
   * Create the Panel
   * 
   * @param date Date
   */
  public TimeDateChooserPanel(Date date) {
    mDateModel = new SpinnerDateModel();
    mDateModel.setValue(date);

    mSpinner = new JSpinner(mDateModel);
         
    
    if(mSpinner.getEditor() instanceof JSpinner.DateEditor) {
      CaretPositionCorrector.createCorrector(
          ((JSpinner.DateEditor)mSpinner.getEditor()).getTextField(),
          new char[] {'.','/','-',':',' '}, ':');
    }
    
    add(mSpinner);
  }
  
  /**
   * Set the Background Color of the Spinner
   * 
   * @param bg Color of Background
   */
  public void setSpinnerBackground(Color bg) {
    if (mSpinner != null) {
      JComponent editor = mSpinner.getEditor();
      if (editor instanceof JSpinner.DefaultEditor) {
        JSpinner.DefaultEditor defEditor = (JSpinner.DefaultEditor) editor;
        JFormattedTextField tf = defEditor.getTextField();
        if (tf != null) {
          tf.setBackground(bg);
        }
      }
    }
  }

  /**
   * Get the selected Date/Time
   * 
   * @return Date
   */
  public Date getDate() {
    return mDateModel.getDate();
  }

}
