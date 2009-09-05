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
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import util.ui.CaretPositionCorrector;

/**
 * A spinner that gives the possibility to choose date/time
 * 
 */
public class TimeDateSpinner extends JSpinner {

  /**
   * Create the spinner
   * 
   * @param date Date
   */
  public TimeDateSpinner(Date date) {
    super(new SpinnerDateModel());
    getModel().setValue(date);
    if(getEditor() instanceof JSpinner.DateEditor) {
      CaretPositionCorrector.createCorrector(
          ((JSpinner.DateEditor)getEditor()).getTextField(),
          new char[] {'.','/','-',':',' '}, ':');
    }
  }
  
  /**
   * Set the Background Color of the Spinner
   * 
   * @param bg Color of Background
   */
  public void setSpinnerBackground(Color bg) {
      JComponent editor = getEditor();
      if (editor instanceof JSpinner.DefaultEditor) {
        JSpinner.DefaultEditor defEditor = (JSpinner.DefaultEditor) editor;
        JFormattedTextField tf = defEditor.getTextField();
        if (tf != null) {
          tf.setBackground(bg);
        }
      }
  }

  /**
   * Get the selected Date/Time
   * 
   * @return Date
   */
  public Date getDate() {
    return ((SpinnerDateModel)getModel()).getDate();
  }

}
