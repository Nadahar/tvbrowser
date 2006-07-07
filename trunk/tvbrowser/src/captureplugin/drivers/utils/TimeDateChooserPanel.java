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
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 * A Panel that gives the possibility to choose date/time
 * 
 */
public class TimeDateChooserPanel extends JPanel {

  /** The Spinner-Model */
  private SpinnerDateModel mDateModel;

  private JSpinner mSpinner;
  private Point mClickLocation;
  private int mCaretPosition;
  private CaretListener mCaretListener;

  /**
   * Create the Panel
   * 
   * @param date Date
   */
  public TimeDateChooserPanel(Date date) {
    mDateModel = new SpinnerDateModel();
    mDateModel.setValue(date);
    mClickLocation = null;
    mCaretPosition = -1;

    mSpinner = new JSpinner(mDateModel);
    
    if(mSpinner.getEditor() instanceof JSpinner.DateEditor) {
      JFormattedTextField field = ((JSpinner.DateEditor)mSpinner.getEditor()).getTextField();
      
      createCaretListener(field);
      addMouseListenerToField(field);
      addFocusListenerToField(field);
    }
    
    add(mSpinner);
  }
  
  /** Create caret listener for making sure of editing the right value.*/
  private void createCaretListener(final JFormattedTextField field) {
    mCaretListener = new CaretListener() {
      public void caretUpdate(final CaretEvent e) {
        if(mCaretPosition != -1 && field.getSelectedText() == null) {
          mCaretPosition = field.getCaretPosition();
        
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              String text = field.getText();
            
              if(mCaretPosition >= text.length() || text.charAt(mCaretPosition) == ':' ||
                  text.charAt(mCaretPosition) == '.' || text.charAt(mCaretPosition) == '/' ||
                  text.charAt(mCaretPosition) == ' ') {
                field.setCaretPosition(--mCaretPosition);
              }
            }
          });
        }
      }
    };
  }
  
  /** Add mouse listener to the field to track clicks and get the location of it */
  private void addMouseListenerToField(final JFormattedTextField field) {
    field.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        mClickLocation = e.getPoint();
      }
    });
  }
  
  /**  Add focus listener to set the location of the caret to the right place */
  private void addFocusListenerToField(final JFormattedTextField field) {
    field.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        if(mCaretPosition == -1)
          mCaretPosition = field.getText().indexOf(":") + 1;
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {              
            if(mClickLocation != null)
              mCaretPosition = field.getUI().viewToModel(field, mClickLocation);
            
            mClickLocation = null;
            
            field.setCaretPosition(mCaretPosition);
            field.addCaretListener(mCaretListener);
          }
        });
      }

      public void focusLost(FocusEvent e) {
        mCaretPosition = field.getCaretPosition();
        field.removeCaretListener(mCaretListener);
      }
    });      
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
