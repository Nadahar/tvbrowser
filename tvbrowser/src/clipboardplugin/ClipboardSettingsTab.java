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
package clipboardplugin;

import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import util.paramhandler.ParamInputField;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import devplugin.SettingsTab;

/**
 * This is the Settings-Tab for the ClipboardPlugin
 * 
 * @author bodum
 */
public class ClipboardSettingsTab implements SettingsTab {
  /** Translator */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ClipboardSettingsTab.class);
  
  /** Settings to use */
	private Properties mSettings;
  /** Text-Area for the Parameters */
	private ParamInputField mParamText;
	
  /**
   * Creates the SettingsTab
   * @param setttings Settings to use
   */
	public ClipboardSettingsTab(Properties setttings) {
		mSettings = setttings;
	}

  /**
   * Creates the SettingsPanel
   * @return Settings-Panel
   */
	public JPanel createSettingsPanel() {
    mParamText = new ParamInputField(mSettings.getProperty("ParamToUse", ClipboardPlugin.DEFAULT_PARAM));
    mParamText.setBorder(BorderFactory.createTitledBorder(
        mLocalizer.msg("createText", "Text to create for each Program") + ":"));
    
		return mParamText;
	}

  /**
   * Save the Settings
   */
	public void saveSettings() {
		mSettings.setProperty("ParamToUse", mParamText.getText());
	}

  /**
   * Get the Tab-Icon
   * @return Icon
   */
	public Icon getIcon() {
		return new ImageIcon(ImageUtilities
        .createImageFromJar("clipboardplugin/clipboard.png",
            ClipboardSettingsTab.class));
	}

  /**
   * Get the Title for this Tab
   * @return Tab-Title
   */
	public String getTitle() {
		return mLocalizer.msg("title","Clipboard Settings");
	}
}