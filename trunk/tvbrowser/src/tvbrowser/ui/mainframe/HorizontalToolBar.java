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
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateAdapter;
import tvbrowser.ui.PictureButton;
import tvbrowser.ui.filter.FilterChooser;
import util.ui.Toolbar;
import devplugin.Channel;
import devplugin.Plugin;

public class HorizontalToolBar extends JPanel implements ActionListener {
  
  /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(HorizontalToolBar.class);

  
  private MainFrame mParent;
  private JButton mSettingsBtn, mUpdateBtn;
  private FilterChooser mFilterChooser;
  private JComboBox mChannelChooser;
  private DefaultComboBoxModel mChannelChooserModel;
  private Toolbar mToolbar;
  
  public HorizontalToolBar(MainFrame parent, FilterChooser filterChooser) {
    setOpaque(false);
    mParent=parent;
    mFilterChooser=filterChooser;
    setBorder(BorderFactory.createEmptyBorder(5,1,0,1));
    setLayout(new BorderLayout());
    
    //mToolbar=createButtonPanel();
    mToolbar = new Toolbar();
    mToolbar.setOpaque(false);
    mToolbar.setButtons(getToolbarButtons(), getToolbarStyle());

    JPanel comboboxPanel=new JPanel(new GridLayout(0,1));
    mChannelChooserModel=new DefaultComboBoxModel();
    mChannelChooser=new JComboBox(mChannelChooserModel);
    mChannelChooser.setMaximumRowCount(25);
    mChannelChooser.setRenderer(new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            
              JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                                                 index, isSelected, cellHasFocus);

              if (value instanceof Channel) {
                label.setText(((Channel)value).getName());
              }
              else {
                label.setText(value.toString());
              }

              return label;
              }

    
        });
    updateChannelChooser();
    mChannelChooser.addActionListener(this);
    comboboxPanel.add(mChannelChooser);
    comboboxPanel.add(mFilterChooser);
    

    add(mToolbar,BorderLayout.CENTER);
    add(comboboxPanel,BorderLayout.EAST);
    
    PluginProxyManager.getInstance().addPluginStateListener(
      new PluginStateAdapter() {
        public void pluginActivated(Plugin p) {
          updateButtons();
        }

        public void pluginDeactivated(Plugin p) {
          updateButtons();
        }
      }); 
  }
  
  public int getToolbarStyle() {
    int style = 0;
    if (Settings.propToolbarButtonStyle.getString().equals("icon")) {
      style = Toolbar.ICON;      
    }
    if (Settings.propToolbarButtonStyle.getString().equals("text")) {
      style = Toolbar.TEXT;       
    }
    if (Settings.propToolbarButtonStyle.getString().equals("text&icon")) {
      style = Toolbar.ICON|Toolbar.TEXT;        
    }
    
    return style;
  }
  
  public void updateButtons() {    
    JButton[] toolbarButtons = getToolbarButtons();
    mToolbar.setButtons(toolbarButtons, getToolbarStyle());    
    mToolbar.updateToolbar();
  }
  
  
 
  
  public void updateChannelChooser() {    
    mChannelChooserModel.removeAllElements();
    mChannelChooserModel.addElement(mLocalizer.msg("GoToChannel","Go to channel"));
    Channel[] channelList=tvbrowser.core.ChannelList.getSubscribedChannels();
    for (int i=0;i<channelList.length;i++) {
      mChannelChooserModel.addElement(channelList[i]);
    }
  }
  
  private JButton createUpdateBtn() {
    
    String msg = tvbrowser.TVBrowser.mLocalizer.msg("button.update", "Update");
    mUpdateBtn = new PictureButton(msg, new ImageIcon("imgs/Refresh24.gif"),MainFrame.mLocalizer.msg("menuinfo.update",""),mParent.getStatusBarLabel());
    mUpdateBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				mParent.updateTvData();			
			}
    });
    return mUpdateBtn;
  }
  private JButton createSettingsBtn() {
      String msg = tvbrowser.TVBrowser.mLocalizer.msg("button.settings", "Settings");
      JButton settingsBtn = new PictureButton(msg, new ImageIcon("imgs/Preferences24.gif"),MainFrame.mLocalizer.msg("menuinfo.settings",""),mParent.getStatusBarLabel());
      settingsBtn.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {				
          mParent.showSettingsDialog();
				}});
      return settingsBtn;
  }
  
  
  private JButton[] getToolbarButtons() {
    ArrayList buttons = new ArrayList();
    
    String[] buttonNames = Settings.propToolbarButtons.getStringArray();
    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    if (buttonNames == null) {
      // Show all buttons
      buttons.add(createSettingsBtn());
      buttons.add(createUpdateBtn());    

      PluginProxy[] pluginArr = pluginMng.getActivatedPlugins();
      for (int i = 0; i < pluginArr.length; i++) {
        JButton button = createPluginButton(pluginArr[i]);
        if (button != null) {
          buttons.add(button);
        }
      }
    } else {
      // Show only the selected buttons
      for (int i = 0; i < buttonNames.length; i++) {
        if ("#update".equals(buttonNames[i])) {
          buttons.add(createUpdateBtn());
        } else if ("#settings".equals(buttonNames[i])) {
          buttons.add(createSettingsBtn());
        } else {
          // Get the plugin
          String pluginId = buttonNames[i];
          PluginProxy plugin = pluginMng.getPluginForId(pluginId);
          if (plugin == null) {
            // The plugin was not found
            // -> Check whether the old class name is used
            pluginId = "java." + pluginId;
            plugin = pluginMng.getPluginForId(pluginId);
            
            if (plugin != null) {
              // There are still the old class names in use -> Save it as ID
              buttonNames[i] = pluginId;
              Settings.propToolbarButtons.setStringArray(buttonNames);
            }
          }
          
          // Add the button
          JButton button = createPluginButton(plugin);
          if (button != null) {
            buttons.add(button);
          }
        }
      }
    }
    
    // Create an array from the list    
    JButton[] result = new JButton[buttons.size()];
    buttons.toArray(result);
    return result;
  }


  private JButton createPluginButton(PluginProxy plugin) {
    if ((plugin != null) && plugin.isActivated()) {
      final Action action = plugin.getButtonAction();

      String text = (String) action.getValue(Action.NAME);
      String desc = (String) action.getValue(Action.SHORT_DESCRIPTION);
      Icon icon = (Icon) action.getValue(Plugin.BIG_ICON);
      
      JButton button = new PictureButton(text, icon, desc, mParent.getStatusBarLabel());
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          action.actionPerformed(evt);
        }
      });
      return button;
    } else {
      return null;
    }
  }
  
  
  public JButton getUpdateBtn() {
    return mUpdateBtn;
  }
  
    
  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src==mChannelChooser) {
      if (mChannelChooser.getSelectedIndex()>0) {
        mParent.showChannel((Channel)mChannelChooser.getSelectedItem());
        mChannelChooser.setSelectedIndex(0);
      }
    }
  }
  
}