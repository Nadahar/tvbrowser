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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.searchplugin;

import javax.swing.Icon;
import javax.swing.JPanel;

import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.PictureSettingsPanel;

import devplugin.SettingsTab;

/**
 * The picture settings for the search plugin and the search field.
 * 
 * @author René Mach
 */
public class SearchPictureSettingsTab implements SettingsTab {
  private PictureSettingsPanel mPicturePanel;
  
  public JPanel createSettingsPanel() {
    mPicturePanel = new PictureSettingsPanel(SearchPlugin.getInstance().getProgramPanelSettings(),true,true);
    
    return mPicturePanel;
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return Localizer.getLocalization(Localizer.I18N_PICTURES);
  }

  public void saveSettings() {
    SearchPlugin.getInstance().setProgramPanelSettings(new ProgramPanelSettings(mPicturePanel.getPictureShowingType(),mPicturePanel.getPictureTimeRangeStart(),mPicturePanel.getPictureTimeRangeEnd(),false,mPicturePanel.getPictureIsShowingDescription(),mPicturePanel.getPictureDurationTime(),mPicturePanel.getClientPluginIds()));
  }
}
