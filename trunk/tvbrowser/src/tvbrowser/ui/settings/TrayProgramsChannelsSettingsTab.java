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
package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import util.ui.OrderChooser;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.SettingsTab;

/**
 * Channel settings for the program showing in tray.
 * 
 * @author René Mach
 *
 */
public class TrayProgramsChannelsSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  
  private JCheckBox mUseUserChannels;
  private OrderChooser mChannelOCh;
  
  public JPanel createSettingsPanel() {
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu,fill:default:grow,5dlu",
        "pref,5dlu,pref,10dlu,fill:default:grow"));
    builder.setDefaultDialogBorder();
    CellConstraints cc = new CellConstraints();
    
    mUseUserChannels = new JCheckBox(mLocalizer.msg("userChannels","Use user defined channels"),Settings.propTrayUseSpecialChannels.getBoolean());
    mUseUserChannels.setToolTipText(mLocalizer.msg("userChannelsToolTip","<html>If you select this you can choose the channels that will be used for<br><b>Programs at...</b> and <b>Now/Soon running programs</b>.<br>If this isn't selected the first 10 channels in default order will be used.</html>"));
    
    mChannelOCh = new OrderChooser(
        Settings.propTraySpecialChannels.getChannelArray(false),
        Settings.propSubscribedChannels.getChannelArray(false), true);

    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg(
        "channelsSeparator",
        "Which channels should be used for these displays?"), cc.xyw(1, 1, 3));
    builder.add(mUseUserChannels, cc.xy(2,3));
    builder.add(mChannelOCh, cc.xy(2, 5));
    
    boolean enabled =  Settings.propTrayIsEnabled.getBoolean() && 
      (Settings.propTrayNowProgramsEnabled.getBoolean() || 
       Settings.propTraySoonProgramsEnabled.getBoolean() ||
        Settings.propTrayOnTimeProgramsEnabled.getBoolean());
    
    c.getComponent(0).setEnabled(enabled);
    mUseUserChannels.setEnabled(enabled);
    mChannelOCh.setEnabled(enabled && mUseUserChannels.isSelected());
    
    mUseUserChannels.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mChannelOCh.setEnabled(mUseUserChannels.isSelected());
      }
    });
    
    return builder.getPanel();
  }

  public void saveSettings() {
    if (mUseUserChannels != null)
      Settings.propTrayUseSpecialChannels.setBoolean(mUseUserChannels.isSelected());
    
    Object[] order = mChannelOCh.getOrder();
    Channel[] ch = new Channel[order.length];

    if(!mUseUserChannels.isSelected()) {
      order = Settings.propSubscribedChannels.getChannelArray(false);
      ch = new Channel[order.length > 10 ? 10 : order.length];
    }

    for (int i = 0; i < ch.length; i++)
      ch[i] = (Channel) order[i];
    
    if (order != null)
      Settings.propTraySpecialChannels.setChannelArray(ch);
    
    
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("channels","Channels");
  }

}
