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
package tvbrowser.extras.favoritesplugin;

import javax.swing.Icon;
import javax.swing.JPanel;

import tvbrowser.ui.settings.AbstractSettingsTab;
import util.ui.Localizer;
import util.ui.PictureSettingsPanel;

/**
 * The settings tab for the program panel in the program list.
 * 
 * @author René Mach
 * @since 2.2.2
 */
public class FavoritesPicturesSettingsTab extends AbstractSettingsTab {
  /** Picture settings */
  private PictureSettingsPanel mPictureSettings;
  
  public JPanel createSettingsPanel() {
    mPictureSettings = new PictureSettingsPanel(FavoritesPlugin.getInstance().getProgramPanelSettings(), true, true);
    
    return mPictureSettings;
  }

  public Icon getIcon() {
    return getPictureIcon();
  }

  public String getTitle() {
    return Localizer.getLocalization(Localizer.I18N_PICTURES);
  }

  public void saveSettings() {
    FavoritesPlugin.getInstance().getSettings().setProperty("pictureType", String.valueOf(mPictureSettings.getPictureShowingType()));
    FavoritesPlugin.getInstance().getSettings().setProperty("pictureTimeRangeStart", String.valueOf(mPictureSettings.getPictureTimeRangeStart()));
    FavoritesPlugin.getInstance().getSettings().setProperty("pictureTimeRangeEnd", String.valueOf(mPictureSettings.getPictureTimeRangeEnd()));
    FavoritesPlugin.getInstance().getSettings().setProperty("pictureShowsDescription", String.valueOf(mPictureSettings.getPictureIsShowingDescription()));
    FavoritesPlugin.getInstance().getSettings().setProperty("pictureDuration", String.valueOf(mPictureSettings.getPictureDurationTime()));
    
    if(PictureSettingsPanel.typeContainsType(mPictureSettings.getPictureShowingType(),PictureSettingsPanel.SHOW_FOR_PLUGINS)) {
      StringBuffer temp = new StringBuffer();
      
      String[] plugins = mPictureSettings.getClientPluginIds();
      
      for(int i = 0; i < plugins.length; i++)
        temp.append(plugins[i]).append(";;");
      
      if(temp.toString().endsWith(";;"))
        temp.delete(temp.length()-2,temp.length());
      
      FavoritesPlugin.getInstance().getSettings().setProperty("picturePlugins", temp.toString());
    }
    
    new Thread() {
      public void run() {
        FavoritesPlugin.getInstance().store();
      }
    }.start();
  }
}
