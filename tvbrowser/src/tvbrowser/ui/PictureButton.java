/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tvbrowser.ui;

import java.awt.Font;
import java.awt.Insets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import tvbrowser.core.Settings;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class PictureButton extends JButton {

  private static Insets NULL_INSETS = new Insets(0, 0, 0, 0);
  private static Font TEXT_FONT = new Font("Dialog", Font.PLAIN, 10);



  public PictureButton(String title, Icon icon) {
    super();
    
    setIcon(icon);
    setText(title);
    
    setOpaque(false);
    setVerticalTextPosition(AbstractButton.BOTTOM);
    setHorizontalTextPosition(AbstractButton.CENTER);
    setFont(TEXT_FONT);
    setMargin(NULL_INSETS);
    setFocusPainted(false);
    
    addMouseListener(new MouseAdapter () {
      public void mouseEntered(MouseEvent e) {
        handleMouseEntered();
      }
      public void mouseExited(MouseEvent e) {
        handleMouseExited();
      }
    });
    handleMouseExited();
  }
  
  
  
  public void setText(String text) {
    if (Settings.getButtonSettings() == Settings.ICON_ONLY) {
      super.setText(null);
      super.setToolTipText(text);
    } else {
      super.setText(text);
      super.setToolTipText(null);
    }
  }

  
  
  public void setIcon(Icon icon) {
    if (Settings.getButtonSettings() == Settings.TEXT_ONLY) {
      super.setIcon(null);
    } else {
      super.setIcon(icon);
    }
  }
  
  
  
  private void handleMouseEntered() {
    setBorderPainted(true);
  }
  
  
  public void handleMouseExited() {
    setBorderPainted(false);
  }  

}