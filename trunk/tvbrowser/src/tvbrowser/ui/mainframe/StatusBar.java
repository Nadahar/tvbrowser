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
 
package tvbrowser.ui.mainframe;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {
  
  private JProgressBar mProgressBar;
  private JLabel mInfoLabel;  
  
 public StatusBar() {
   setOpaque(false);
   setLayout(new BorderLayout(10,0));
   setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
   mInfoLabel=new JLabel();
   mInfoLabel.setBorder(BorderFactory.createLoweredBevelBorder());
   mProgressBar = new JProgressBar();
	 mProgressBar.setPreferredSize(new Dimension(200,10));
	 mProgressBar.setOpaque(false);
   mProgressBar.setBorder(BorderFactory.createLoweredBevelBorder());
        
   add(mProgressBar,BorderLayout.EAST);
   add(mInfoLabel,BorderLayout.CENTER);
   this.setPreferredSize(new Dimension(0,20));
 }
 
 public JProgressBar getProgressBar() {
   return mProgressBar;
 }
 
 public JLabel getLabel() {
   return mInfoLabel;
 }
  
}

