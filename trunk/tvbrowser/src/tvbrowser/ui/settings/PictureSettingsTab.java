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
 */
package tvbrowser.ui.settings;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.reminderplugin.ReminderList;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.MarkerChooserDlg;
import util.ui.PictureSettingsPanel;
import util.ui.UiUtilities;

import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.SettingsTab;

/**
 * The settings tab for the program panel picture settings.
 * 
 * @author René Mach
 * @since 2.2.2
 */
public class PictureSettingsTab implements SettingsTab {
  private static Localizer mLocalizer = Localizer.getLocalizerFor(PictureSettingsTab.class);
  
  /** Picture settings */
  private PictureSettingsPanel mPictureSettings;
  private JPanel mSubPanel;
  private JRadioButton mShowPicturesForPlugins;
  private JLabel mPluginLabel;
  private Marker[] mClientPlugins;
  
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    mSubPanel = new JPanel(new FormLayout("12dlu,pref:grow,5dlu,pref","pref,2dlu,pref"));
    
    mShowPicturesForPlugins = new JRadioButton(mLocalizer.msg("showPicturesForPlugins","Show for programs that are marked by plugins:"), Settings.propPictureType.getInt() == PictureSettingsPanel.SHOW_FOR_PLUGINS);        
    mPluginLabel = new JLabel();
    mPluginLabel.setEnabled(Settings.propPictureType.getInt() == PictureSettingsPanel.SHOW_FOR_PLUGINS);
    
    final JButton choose = new JButton(mLocalizer.msg("selectPlugins","Choose Plugins"));
    choose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
        MarkerChooserDlg chooser = null;
        if(w instanceof JDialog)
          chooser = new MarkerChooserDlg((JDialog)w,mClientPlugins, null);
        else
          chooser = new MarkerChooserDlg((JFrame)w,mClientPlugins, null);
        
        chooser.setLocationRelativeTo(w);
        chooser.setVisible(true);
        
        mClientPlugins = chooser.getMarker();
        
        handlePluginSelection();
      }
    });
    choose.setEnabled(Settings.propPictureType.getInt() == PictureSettingsPanel.SHOW_FOR_PLUGINS);
    
    mShowPicturesForPlugins.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mPluginLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        choose.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    
    String[] clientPluginIdArr = Settings.propPicturePluginIds.getStringArray();    
    
    ArrayList clientPlugins = new ArrayList();
    
    for(int i = 0; i < clientPluginIdArr.length; i++) {
      PluginAccess plugin = Plugin.getPluginManager().getActivatedPluginForId(clientPluginIdArr[i]);
      if(plugin != null)
        clientPlugins.add(plugin);
      else if(ReminderList.MARKER.getId().compareTo(clientPluginIdArr[i]) == 0)
        clientPlugins.add(ReminderList.MARKER);
      else if(FavoritesPlugin.MARKER.getId().compareTo(clientPluginIdArr[i]) == 0)
        clientPlugins.add(FavoritesPlugin.MARKER);
    }
    
    mClientPlugins = (Marker[])clientPlugins.toArray(new Marker[clientPlugins.size()]);
    
    handlePluginSelection();
    
    mSubPanel.add(mShowPicturesForPlugins, cc.xyw(1,1,4));
    mSubPanel.add(mPluginLabel, cc.xy(2,3));
    mSubPanel.add(choose, cc.xy(4,3));
    
    mPictureSettings = new PictureSettingsPanel(Settings.propPictureType.getInt(),Settings.propPictureStartTime.getInt(),Settings.propPictureEndTime.getInt(),Settings.propIsPictureShowingDescription.getBoolean(), true, true, mSubPanel, mShowPicturesForPlugins);
    
    return mPictureSettings;
  }
  
  private void handlePluginSelection() {
    if(mClientPlugins.length > 0) {
      mPluginLabel.setText(mClientPlugins[0].toString());
      mPluginLabel.setEnabled(true);
    }
    else {
      mPluginLabel.setText(mLocalizer.msg("noPlugins","No Plugins choosen"));
      mPluginLabel.setEnabled(false);
    }
    
    for (int i = 1; i < (mClientPlugins.length > 4 ? 3 : mClientPlugins.length); i++) {
      mPluginLabel.setText(mPluginLabel.getText() + ", " + mClientPlugins[i]);
    }
    
    if(mClientPlugins.length > 4)
      mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + mLocalizer.msg("otherPlugins","others...") + ")");
  }

  
  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return PictureSettingsPanel.mLocalizer.msg("pictures","Pictures");
  }

  public void saveSettings() {
    if(mPictureSettings != null) {
      Settings.propPictureType.setInt(mShowPicturesForPlugins.isSelected() ? PictureSettingsPanel.SHOW_FOR_PLUGINS : mPictureSettings.getPictureShowingType());
      Settings.propPictureStartTime.setInt(mPictureSettings.getPictureTimeRangeStart());
      Settings.propPictureEndTime.setInt(mPictureSettings.getPictureTimeRangeEnd());
      Settings.propIsPictureShowingDescription.setBoolean(mPictureSettings.getPictureIsShowingDescription());
      
      if(mShowPicturesForPlugins.isSelected()) {
        String[] clientPluginIdArr = new String[mClientPlugins.length];
      
        for (int i = 0; i < mClientPlugins.length; i++)
          clientPluginIdArr[i] = mClientPlugins[i].getId();

        Settings.propPicturePluginIds.setStringArray(clientPluginIdArr);
      }
    }
  }

}
