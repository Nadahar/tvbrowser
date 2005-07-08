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
  /** Height of Panel, if an Icon is > 15, it get adjusted to it's needs */
  private int mColumnHeight = 15;  
  
  public ChannelPanel(int columnWidth, Channel[] channelArr) {
    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    setOpaque(true);
    setBackground(new java.awt.Color(208, 199, 241));
    
    setShownChannels(channelArr);
    setColumnWidth(columnWidth);
  }

  public static void fontChanged() {
    ChannelLabel.fontChanged();
  }
  
  public void setShownChannels(Channel[] channelArr) {
    removeAll();
    mLabelArr = new ChannelLabel[channelArr.length];
    
    for (int i = 0; i < mLabelArr.length; i++) {
      mLabelArr[i]=new ChannelLabel(channelArr[i]);  
      add(mLabelArr[i]);
   
      if ((mLabelArr[i] != null) && (mLabelArr[i].getIcon() != null) && 
              (mLabelArr[i].getIcon().getIconHeight() > mColumnHeight)) {
          mColumnHeight = mLabelArr[i].getIcon().getIconHeight();
      }
   }
    
    setColumnWidth(mColumnWidth);
    updateUI();
  }

  
  
  public void setColumnWidth(int columnWidth) {
    mColumnWidth = columnWidth;
    
    for (int i = 0; i < mLabelArr.length; i++) {
      mLabelArr[i].setPreferredSize(new Dimension(mColumnWidth, mColumnHeight));
    }
  }  

  
  static class ChannelLabel extends util.ui.ChannelLabel {
   
    private static Cursor linkCursor=new Cursor(Cursor.HAND_CURSOR);
    private static Font channelNameFont;
  
    public static void fontChanged() {
      boolean useDefaults = Settings.propUseDefaultFonts.getBoolean();
      if (useDefaults) {
        channelNameFont = Settings.propChannelNameFont.getDefault();
      } else {
        channelNameFont = Settings.propChannelNameFont.getFont();
      }
    }
    
    public ChannelLabel(final Channel ch) {
      super();
      setIcon(ch.getIcon());
          /*Settings.propEnableChannelIcons.getBoolean()) {
        // Set Icon if it's available
        if (Settings.propShowChannelIconsInProgramTable.getBoolean()*/
      if ( !(Settings.propEnableChannelIcons.getBoolean() && Settings.propShowChannelIconsInProgramTable.getBoolean()) || Settings.propShowChannelNames.getBoolean()) {
        // Set the channel name as text
        String channelName = ch.getName();
        if (channelName == null) {
          channelName = mLocalizer.msg("unknown", "Unknown");
        }
        setText(channelName);
      }

      // Check whether the font was set
      if (channelNameFont == null) {
        fontChanged();
      }

      // Avoid that a null-font is set
      // (Happens when the font from the config is null)
      if (channelNameFont != null) {
        setFont(channelNameFont);
      }
      
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

    public void setIcon(Icon icon) {
      if (Settings.propEnableChannelIcons.getBoolean()) {
        // Set Icon if it's available
        if (Settings.propShowChannelIconsInProgramTable.getBoolean()) {
          super.setIcon(icon);
        }
      }
    }

  } // inner class ChannelLabel

}
