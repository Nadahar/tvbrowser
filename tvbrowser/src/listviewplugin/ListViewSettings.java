/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package listviewplugin;

import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.ui.PictureSettingsPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * Configuration Dialog for the ListView Plugin
 * 
 * @author bodum
 */
public class ListViewSettings implements SettingsTab {
  /** Translator */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ListViewSettings.class);
 
  /** The Settings */
  private Properties mSettings;
  /** Checkbox for showing at startup*/
  private JCheckBox mShowAtStart;
  /** Picture settings */
  private PictureSettingsPanel mPictureSettings;
  /**
   * Create the SettingsTab
   * @param settings Settings
   */
  public ListViewSettings(Properties settings) {
    mSettings = settings;
  }
  
  /**
   * Create the Panel
   */
  public JPanel createSettingsPanel() {    
    JPanel panel = new JPanel();
    panel.setLayout(new FormLayout("5dlu,default:grow", "5dlu,default,10dlu,fill:default:grow"));
    
    CellConstraints cc = new CellConstraints();
    
    mShowAtStart = new JCheckBox(mLocalizer.msg("showAtStart", "Show at startup"));
    mShowAtStart.setSelected(mSettings.getProperty("showAtStartup", "false").equals("true"));    

    mPictureSettings = new PictureSettingsPanel(ListViewPlugin.getInstance().getProgramPanelSettings(),true,false);
    
    JScrollPane pictures = new JScrollPane(mPictureSettings);
    pictures.setBorder(null);
    pictures.setViewportBorder(null);
    
    panel.add(mShowAtStart, cc.xy(2,2));
    panel.add(pictures, cc.xyw(1,4,2));
        
    return panel;
  }

  /**
   * Save the Settings
   */
  public void saveSettings() {
    mSettings.setProperty("showAtStartup", String.valueOf(mShowAtStart.isSelected()));
    
    mSettings.setProperty("pictureType", String.valueOf(mPictureSettings.getPictureShowingType()));
    mSettings.setProperty("pictureTimeRangeStart", String.valueOf(mPictureSettings.getPictureTimeRangeStart()));
    mSettings.setProperty("pictureTimeRangeEnd", String.valueOf(mPictureSettings.getPictureTimeRangeEnd()));
    mSettings.setProperty("pictureShowsDescription", String.valueOf(mPictureSettings.getPictureIsShowingDescription()));
    mSettings.setProperty("pictureDuration", String.valueOf(mPictureSettings.getPictureDurationTime()));
    
    if(PictureSettingsPanel.typeContainsType(mPictureSettings.getPictureShowingType(),PictureSettingsPanel.SHOW_FOR_PLUGINS)) {
      StringBuffer temp = new StringBuffer();
      
      String[] plugins = mPictureSettings.getClientPluginIds();
      
      for(int i = 0; i < plugins.length; i++)
        temp.append(plugins[i]).append(";;");
      
      if(temp.toString().endsWith(";;"))
        temp.delete(temp.length()-2,temp.length());
      
      mSettings.setProperty("picturePlugins", temp.toString());
    }
  }

  /**
   * Icon for the SettingsTab
   */
  public Icon getIcon() {
    return ListViewPlugin.getInstance().createImageIcon("actions", "view-list", 16);
  }

  /**
   * Get the Title
   */
  public String getTitle() {
    return mLocalizer.msg("settingsTabName", "ListView Plugin");
  }

}