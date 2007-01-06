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

import javax.swing.Icon;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import util.ui.Localizer;
import util.ui.PictureSettingsPanel;

/**
 * The settings tab for the program panel picture settings.
 * 
 * @author René Mach
 * @since 2.2.2
 */
public class PictureSettingsTab extends AbstractSettingsTab {  
  /** Picture settings */
  private PictureSettingsPanel mPictureSettings;
  
  public JPanel createSettingsPanel() {
    mPictureSettings = new PictureSettingsPanel(Settings.propPictureType.getInt(),Settings.propPictureStartTime.getInt(),Settings.propPictureEndTime.getInt(),Settings.propIsPictureShowingDescription.getBoolean(), true, true, Settings.propPictureDuration.getInt(), Settings.propPicturePluginIds.getStringArray(),null);
    
    return mPictureSettings;
  }
  
  public Icon getIcon() {
    return getPictureIcon();
  }

  public String getTitle() {
    return Localizer.getLocalization(Localizer.I18N_PICTURES);
  }

  public void saveSettings() {
    if(mPictureSettings != null) {
      Settings.propPictureType.setInt(mPictureSettings.getPictureShowingType());
      Settings.propPictureStartTime.setInt(mPictureSettings.getPictureTimeRangeStart());
      Settings.propPictureEndTime.setInt(mPictureSettings.getPictureTimeRangeEnd());
      Settings.propPictureDuration.setInt(mPictureSettings.getPictureDurationTime());
      Settings.propIsPictureShowingDescription.setBoolean(mPictureSettings.getPictureIsShowingDescription());
      
      if(PictureSettingsPanel.typeContainsType(mPictureSettings.getPictureShowingType(),PictureSettingsPanel.SHOW_FOR_PLUGINS))
        Settings.propPicturePluginIds.setStringArray(mPictureSettings.getClientPluginIds());
    }
  }
}
