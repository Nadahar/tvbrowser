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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;


import tvbrowser.core.PluginManager;
import tvbrowser.core.Settings;
import tvbrowser.ui.filter.FilterChooser;
import tvbrowser.ui.PictureButton;
import devplugin.Channel;

public class HorizontalToolBar extends JPanel implements ActionListener {
  
  /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(HorizontalToolBar.class);

  
  private MainFrame mParent;
  private JButton mSettingsBtn, mUpdateBtn;
  private FilterChooser mFilterChooser;
  private JComboBox mChannelChooser;
  private DefaultComboBoxModel mChannelChooserModel;
  private JPanel mBtnPanel;
  
  public HorizontalToolBar(MainFrame parent, FilterChooser filterChooser) {
    setOpaque(false);
    mParent=parent;
    mFilterChooser=filterChooser;
    setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
    setLayout(new BorderLayout());
    
    mBtnPanel=createButtonPanel();
    

    JPanel comboboxPanel=new JPanel(new GridLayout(0,1));
    mChannelChooserModel=new DefaultComboBoxModel();
    mChannelChooser=new JComboBox(mChannelChooserModel);
    updateChannelChooser();
    mChannelChooser.addActionListener(this);
    comboboxPanel.add(mChannelChooser);
    comboboxPanel.add(mFilterChooser);
    
    add(mBtnPanel,BorderLayout.WEST);
    add(comboboxPanel,BorderLayout.EAST);
  }
  
  public void updateButtons() {
    remove(mBtnPanel);
    mBtnPanel=createButtonPanel();
    add(mBtnPanel,BorderLayout.WEST);
    updateUI();
    
  }
  
  public JPanel createButtonPanel() {
    
    JPanel panel=new JPanel();
    panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
    panel.setOpaque(false);  
    
    JPanel defaultBtnPanel=new JPanel(new GridLayout(1,0,10,0));
    defaultBtnPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,20));
    defaultBtnPanel.setOpaque(false);
    int width=0;
    if (Settings.isUpdateBtnVisible()) {
      mUpdateBtn=createUpdateBtn();
      defaultBtnPanel.add(mUpdateBtn);
      width+=100;
    }
    if (Settings.isPreferencesBtnVisible()) {
      mSettingsBtn=createSettingsBtn();    
      defaultBtnPanel.add(mSettingsBtn);
      width+=100;
    }
    Dimension dim=defaultBtnPanel.getPreferredSize();
    dim.width=width;
    defaultBtnPanel.setPreferredSize(dim);
    
    JPanel pluginsPanel=createPluginPanel();
    
    panel.add(defaultBtnPanel);
   
    panel.add(pluginsPanel);
        
    return panel;
    
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
    btn.addActionListener(this);
    return btn;
  }
  private JButton createSettingsBtn() {
      String msg = tvbrowser.TVBrowser.mLocalizer.msg("button.settings", "Settings");
      JButton settingsBtn = new PictureButton(msg, new ImageIcon("imgs/Preferences24.gif"),MainFrame.mLocalizer.msg("menuinfo.settings",""),mParent.getStatusBarLabel());
      settingsBtn.addActionListener(this);
      return settingsBtn;
  }
  
  private JPanel createPluginPanel() {
    JPanel result=new JPanel(new GridLayout(1,0,10,0));
    result.setOpaque(false);
    String[] buttonPluginArr = Settings.getButtonPlugins();
    for (int i = 0; i < buttonPluginArr.length; i++) {
      final devplugin.Plugin plugin = PluginManager.getPlugin(buttonPluginArr[i]);
      if ((plugin != null) && PluginManager.isInstalled(plugin) && plugin.getButtonText()!=null) {
        Icon icon = plugin.getButtonIcon();
        JButton btn = new PictureButton(plugin.getButtonText(), icon, plugin.getInfo().getDescription(), mParent.getStatusBarLabel());
        result.add(btn);
        btn.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent event) {
            plugin.execute();
          }
        });
      }
    }
    return result;
  }
  
  private JPanel createComboBoxPanel() {
    JPanel result=new JPanel(new GridLayout(0,1));
    JComboBox mChannelCB=new JComboBox(new String[]{"ARD","ZDF","Sat.1","RTL"});
    JComboBox mFilterCB=new JComboBox(new String[]{"show all","any plugin"});
    result.add(mChannelCB);
    result.add(mFilterCB);
    return result;
  }
  
  public JButton getUpdateBtn() {
    return mUpdateBtn;
  }
  
    
  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src==mUpdateBtn) {
      mParent.updateTvData();
    }
    else if (src==mSettingsBtn) {
      mParent.showSettingsDialog();      
    }
    else if (src==mChannelChooser) {
      if (mChannelChooser.getSelectedIndex()>0) {
        mParent.showChannel((Channel)mChannelChooser.getSelectedItem());
        mChannelChooser.setSelectedIndex(0);
      }
    }
  }
  
}