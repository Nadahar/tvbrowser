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

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import tvbrowser.core.PluginLoader;
import tvbrowser.core.PluginManager;
import tvbrowser.core.PluginStateListener;
import tvbrowser.core.Settings;
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
    
    PluginLoader.getInstance().addPluginStateListener(new PluginStateListener(){

			public void pluginActivated(Plugin p) {				
			  updateButtons();	
			}

			public void pluginDeactivated(Plugin p) {
        updateButtons();				
			}

			public void pluginLoaded(Plugin p) {
			}

			public void pluginUnloaded(Plugin p) {
			}
    });
    
  }
  
  private int getToolbarStyle() {
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
    JButton[] toolbarButtons = this.getToolbarButtons();
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
    JButton btn = new PictureButton(msg, new ImageIcon("imgs/Refresh24.gif"),MainFrame.mLocalizer.msg("menuinfo.update",""),mParent.getStatusBarLabel());
    btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				mParent.updateTvData();			
			}
    });
    return btn;
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
  
  
  private JButton[] getAllToolbarButtons() {
    ArrayList buttons = new ArrayList();
       
    buttons.add(createSettingsBtn());
    buttons.add(createUpdateBtn());    
        
    Plugin[] installedPlugins=PluginManager.getInstance().getInstalledPlugins();
    
    for (int i=0;i<installedPlugins.length;i++) {
      final Plugin plugin=installedPlugins[i];
      
      if (plugin.getButtonText()!=null) {
        String pluginClassName = plugin.getClass().getName();
        Icon icon = plugin.getButtonIcon();
        JButton btn = new PictureButton(plugin.getButtonText(), icon, plugin.getInfo().getDescription(), mParent.getStatusBarLabel());
        buttons.add(btn);    
        btn.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent event) {
            plugin.execute();
          }
        });
      }
    }
       
    JButton[] result = new JButton[buttons.size()];
    buttons.toArray(result);
    return result;
  }
  
  private JButton[] getToolbarButtons() {
    ArrayList buttons = new ArrayList();
    
    String[] buttonNames = Settings.propToolbarButtons.getStringArray();
    if (buttonNames == null) {
      return getAllToolbarButtons();
    }
    
    for (int i=0; i<buttonNames.length; i++) {
      if ("#update".equals(buttonNames[i])) {
        buttons.add(createUpdateBtn());
      }else if ("#settings".equals(buttonNames[i])) {
        buttons.add(createSettingsBtn());
      }else {
        final Plugin plugin = PluginLoader.getInstance().getActivePluginByClassName(buttonNames[i]);
        if (plugin!=null) {
          Icon icon = plugin.getButtonIcon();
          JButton btn = new PictureButton(plugin.getButtonText(), icon, plugin.getInfo().getDescription(), mParent.getStatusBarLabel());
          buttons.add(btn);    
          btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
              plugin.execute();
            }
          });
        }
      }
    }
    
        
    JButton[] result = new JButton[buttons.size()];
    buttons.toArray(result);
    return result;
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