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

import tvbrowser.core.Settings;

import java.awt.*;
import java.awt.event.*;

import devplugin.Channel;


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
    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    setOpaque(true);
    setBackground(new java.awt.Color(208, 199, 241));
    
    setShownChannels(channelArr);
    setColumnWidth(columnWidth);
  }

  public static void fontChanged() {
    ChannelLabel.fontChaned();
  }
  
  public void setShownChannels(Channel[] channelArr) {
    removeAll();
    mLabelArr = new ChannelLabel[channelArr.length];
    
    for (int i = 0; i < mLabelArr.length; i++) {
      String channelName = null;
      if (channelArr[i] != null) {
        channelName = channelArr[i].getName();
      }
      if (channelName == null) {
        channelName = mLocalizer.msg("unknown", "Unknown");
      }
      
      mLabelArr[i]=new ChannelLabel(channelArr[i]);  
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

class ChannelLabel extends JLabel {
   
    private static Cursor linkCursor=new Cursor(Cursor.HAND_CURSOR);
    private static Font channelNameFont;
  
    public static void fontChaned() {
      boolean useDefaults = Settings.propUseDefaultFonts.getBoolean();
      if (useDefaults) {
        channelNameFont = Settings.propChannelNameFont.getDefault();
      } else {
        channelNameFont = Settings.propChannelNameFont.getFont();
      }
    }
  
    public ChannelLabel(final Channel ch) {
      super(ch.getName());

      if (channelNameFont == null) {
        fontChaned();
      }

      setFont(channelNameFont);
      setOpaque(false);
      setHorizontalAlignment(SwingConstants.CENTER);
            
      setCursor(linkCursor);
      addMouseListener(new MouseAdapter(){
        public void  mouseClicked(MouseEvent e) { 
          util.ui.BrowserLauncher.openURL(ch.getWebpage());
        }
        
        public void mouseEntered(MouseEvent e) {
          e.getComponent().setForeground(Color.blue);
        }
        
        public void mouseExited(MouseEvent e) {
          e.getComponent().setForeground(Color.black);
        }        
      });
    } 
  }

