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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import tvbrowser.ui.finder.FinderPanel;


public class DateChooserPanel extends JPanel {
  
  /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(DateChooserPanel.class);

  
  private MainFrame mParent;
  private FinderPanel mFinderPanel;
  private JPanel mTimeBtnPanel;
  
  public DateChooserPanel(MainFrame parent, FinderPanel finderPanel) {
    setOpaque(false);
    mParent=parent;
    setLayout(new BorderLayout(0,7));
    setBorder(BorderFactory.createEmptyBorder(5,3,5,3));
    mFinderPanel = finderPanel;
    
    add(mFinderPanel,BorderLayout.CENTER);
    
  }
  
  
}