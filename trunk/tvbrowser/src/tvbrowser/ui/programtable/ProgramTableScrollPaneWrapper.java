/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.programtable;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;

import devplugin.PluginCenterPanel;
import devplugin.SettingsItem;

/**
 * A wrapper class for the TV-Browser program table scroll pane,
 * used for the new center panel function since version 3.2.
 * 
 * @author René Mach
 * @since 3.2
 */
public class ProgramTableScrollPaneWrapper extends PluginCenterPanel {
  public static final int INFO_EMPTY_FILTER_RESULT = 0;
  public static final int INFO_NO_CHANNELS_SUBSCRIBED = 1;
  public static final int INFO_EMPTY_CHANNEL_GROUP = 2;
  public static final int INFO_NO_DATA = 3;
  
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramTableScrollPaneWrapper.class);
  private ProgramTableScrollPane mProgramTableScrollPane;
  private JPanel mMainPanel;
  
  private InfoPanel mInfoPanel;
  
  public ProgramTableScrollPaneWrapper(ProgramTableScrollPane scrollPane) {
    mInfoPanel = null;
    mProgramTableScrollPane = scrollPane;
    mMainPanel = new JPanel(new BorderLayout());
    mMainPanel.setOpaque(false);
    mMainPanel.add(mProgramTableScrollPane, BorderLayout.CENTER);
  }
  
  @Override
  public String getName() {
    return mLocalizer.msg("name", "Program table");
  }

  @Override
  public JPanel getPanel() {
    return mMainPanel;
  }

  public void showInfoPanel(int type, String name) {
    mMainPanel.removeAll();
    
    mInfoPanel = new InfoPanel(type, name);
    
    mMainPanel.add(mInfoPanel, BorderLayout.CENTER);
    mMainPanel.repaint();
  }
  
  public void removeInfoPanel(int type) {
    if(mInfoPanel != null && mInfoPanel.isType(type)) {
      mMainPanel.removeAll();
      mInfoPanel = null;
      mMainPanel.add(mProgramTableScrollPane, BorderLayout.CENTER);
      mMainPanel.repaint();
    }
  }
  
  public boolean hasInfoPanel(int type) {
    return (mInfoPanel != null && mInfoPanel.isType(type));
  }
  
  private static final class InfoPanel extends JPanel {
    private int mType;
    
    public InfoPanel(final int type, String name) {
      mType = type;
      
      setLayout(new FormLayout("5dlu:grow,default,5dlu:grow","fill:5dlu:grow,default,5dlu,default,fill:5dlu:grow"));
      
      String infoText = null;
      String buttonText = null;
      
      switch (type) {
        case INFO_EMPTY_FILTER_RESULT: 
          infoText = mLocalizer.msg("infoEmptyFilter", "The selected filter '{0}' doesn't accepts any programs.", name);
          buttonText = mLocalizer.msg("buttonEmptyFilter", "Deactivate filter to show all programs");
          break;
        case INFO_NO_CHANNELS_SUBSCRIBED: 
          infoText = mLocalizer.msg("infoNoChannels", "No channels are subscribed.");
          buttonText = mLocalizer.msg("buttonNoChannels", "Select channels now");
          break;
        case INFO_EMPTY_CHANNEL_GROUP: 
          infoText = mLocalizer.msg("infoEmptyChannelGroup", "The used channel group '{0}' seems to be empty.", name);
          buttonText = mLocalizer.msg("buttonEmptyChanenlGroup", "Deactivate channel group");
          break;
        case INFO_NO_DATA: 
          infoText = mLocalizer.msg("infoNoDate", "No data is available.");
          buttonText = mLocalizer.msg("buttonLoadData", "Update data");
          break;
      }
      
      JButton action = new JButton(buttonText);
      action.addActionListener(e -> {
        switch(type) {
          case INFO_EMPTY_FILTER_RESULT: MainFrame.getInstance().setProgramFilter(FilterManagerImpl.getInstance().getAllFilter());break;
          case INFO_NO_CHANNELS_SUBSCRIBED: PluginManagerImpl.getInstance().showSettings(SettingsItem.CHANNELS);break;
          case INFO_EMPTY_CHANNEL_GROUP: MainFrame.getInstance().setChannelFilter(null);break;
          case INFO_NO_DATA: MainFrame.getInstance().updateTvData();break;
        }
      });
      
      add(new JLabel(infoText), CC.xy(2, 2));
      add(action, CC.xy(2, 4));
    }
    
    boolean isType(int type) {
      return mType == type;
    }
  }
}
