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

package tvbrowser.ui.programtable;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import devplugin.Channel;
import tvbrowser.core.*;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ChannelPanel extends JPanel {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ChannelPanel.class);
  
  private int mColumnWidth;
  private JLabel[] mLabelArr;
  
  
  public ChannelPanel(int columnWidth, Channel[] channelArr) {
    setLayout(new GridLayout(1,0,0,0));
    setOpaque(true);
    setBackground(new java.awt.Color(208, 199, 241));
    
    setShownChannels(channelArr);
    setColumnWidth(columnWidth);
  }

  
  
  public void setShownChannels(Channel[] channelArr) {
    removeAll();
    
    mLabelArr = new JLabel[channelArr.length];
    
    for (int i = 0; i < mLabelArr.length; i++) {
      String channelName = null;
      if (channelArr[i] != null) {
        channelName = channelArr[i].getName();
      }
      if (channelName == null) {
        channelName = mLocalizer.msg("unknown", "Unknown");
      }
      
      mLabelArr[i] = new JLabel(channelName);
      mLabelArr[i].setOpaque(false);
      mLabelArr[i].setHorizontalAlignment(JLabel.CENTER);
      add(mLabelArr[i]);
    }
    
    setColumnWidth(mColumnWidth);
  }

  
  
  public void setColumnWidth(int columnWidth) {
    mColumnWidth = columnWidth;
    
    for (int i = 0; i < mLabelArr.length; i++) {
      mLabelArr[i].setPreferredSize(new Dimension(mColumnWidth, 15));
    }
  }
  
}