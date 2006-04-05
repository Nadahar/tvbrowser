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

import javax.swing.Icon;
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

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
  .getLocalizerFor(TrayProgramsChannelsSettingsTab.class);

  private OrderChooser mChannelOCh;
  
  public JPanel createSettingsPanel() {
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "fill:default:grow",
        "pref,5dlu,fill:default:grow"));
    builder.setDefaultDialogBorder();
    CellConstraints cc = new CellConstraints();
    
    mChannelOCh = new OrderChooser(
        Settings.propNowRunningProgramsInTrayChannels.getChannelArray(false),
        Settings.propSubscribedChannels.getChannelArray(false), true);

    final JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg(
        "programShowing.runningChannels",
        "Which channels should be used for these displays?"), cc.xy(1, 1));
    builder.add(mChannelOCh, cc.xy(1, 3));
    
    boolean enabled =  Settings.propTrayIsEnabled.getBoolean() && 
      (Settings.propShowNowRunningProgramsInTray.getBoolean() || 
        Settings.propShowTimeProgramsInTray.getBoolean());
    
    c.getComponent(0).setEnabled(enabled);
    mChannelOCh.setEnabled(enabled);
    
    return builder.getPanel();
  }

  public void saveSettings() {
    Object[] order = mChannelOCh.getOrder();
    Channel[] ch = new Channel[order.length];

    for (int i = 0; i < ch.length; i++)
      ch[i] = (Channel) order[i];

    if (order != null)
      Settings.propNowRunningProgramsInTrayChannels.setChannelArray(ch);
    
    Settings.propShowProgramsInTrayWasConfigured.setBoolean(true);
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("programShowing.channels","Channels");
  }

}
