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
  
  private JScrollPane scrollPane=null;
  
  private JLabel [] labels;
  
  
  public ChannelPanel() {
    int n=ChannelList.getNumberOfSubscribedChannels();
   setLayout(new GridLayout(1,0,0,0));
    
  
    setOpaque(true);
    setBackground(new java.awt.Color(208,199,241));
    
    labels=new JLabel[n];
    Iterator iter = ChannelList.getChannels();
    int colWidth=Settings.getColumnWidth();
    
    while (iter.hasNext()) {
      Channel ch = (Channel) iter.next();
      if (ChannelList.isSubscribedChannel(ch)) {
        int pos=ChannelList.getPos(ch);
        labels[pos]=new JLabel(ch.getName());
        labels[pos].setOpaque(false);
        labels[pos].setHorizontalAlignment(JLabel.CENTER);
        labels[pos].setPreferredSize(new Dimension(colWidth,15));
      }
      
    }
    for (int i=0;i<n;i++) {
      if (labels[i]==null) {
        labels[i] = new JLabel(mLocalizer.msg("unknown", "Unknown"));
      }
      add(labels[i]);
    }
  }
  
  
  public void scroll(int x) {
    scrollPane.getViewport().setViewPosition(new Point(x,0));
  }
   
}