/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
package tvbrowser.ui.mainframe.searchfield;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

/**
 * The TextField for the Search in the Toolbar 
 * @author bodum
 */
public class SearchTextField extends JTextField implements FocusListener{
  /** Color */
  private Color mTextColor, mNoTextColor;
  /** Is Text available ?*/
  private boolean mHasText = false;
  
  /**
   * Create the Search-Field
   * @param len Specified amount of Columns
   */
  public SearchTextField(int len) {
    super(len);
    addFocusListener(this);
    
    int r = (getForeground().getRed()   + getBackground().getRed())   >> 1;
    int g = (getForeground().getGreen() + getBackground().getGreen()) >> 1;
    int b = (getForeground().getBlue()  + getBackground().getBlue())  >> 1;
    
    mNoTextColor = new Color(r,g,b);
    mTextColor = getForeground();
    
    setText("Suchen...");
    setForeground(mNoTextColor);
  }
  
  /*
   * (non-Javadoc)
   * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
   */
  public void focusGained(FocusEvent e) {
    if (!mHasText) {
      setText("");
      setForeground(mTextColor);
    }
  }

  /*
   * (non-Javadoc)
   * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
   */
  public void focusLost(FocusEvent e) {
    if (getText().length() == 0) {
      mHasText = false;
      setText("Kein Text !");
      setForeground(mNoTextColor);
    } else {
      mHasText = true;
    }
      
    updateUI();
  }
  
}
