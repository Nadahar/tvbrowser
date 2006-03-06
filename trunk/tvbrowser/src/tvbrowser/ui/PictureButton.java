/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.ui;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;


/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class PictureButton extends JButton implements FocusListener {

  private static Insets NULL_INSETS = new Insets(0, 0, 0, 0);
  private static Font TEXT_FONT = new Font("Dialog", Font.PLAIN, 10);

  private String mDescription;
  private JLabel mStatusBar;

  public PictureButton(String title, Icon icon) {
    super();
    
    addFocusListener(this);
    
    setIcon(icon);
    setText(title);
    //setToolTipText(title);
    setOpaque(false);
    setVerticalTextPosition(SwingConstants.BOTTOM);
    setHorizontalTextPosition(SwingConstants.CENTER);
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
  
  public PictureButton(String title, Icon icon, String description, JLabel statusBar) {
    this(title,icon);
    mDescription=description;
    mStatusBar=statusBar;
  }
  
  
  private void handleMouseEntered() {
    setBorderPainted(true);
    if (mStatusBar!=null) {
      mStatusBar.setText(mDescription);
    }
  }
  
  
  public void handleMouseExited() {
    setBorderPainted(false);
    if (mStatusBar!=null) {
      mStatusBar.setText("");
    }
  }

	
	public void focusGained(FocusEvent arg0) {
		setBorderPainted(true);
	}


	public void focusLost(FocusEvent arg0) {
    setBorderPainted(false);	
	}  

}