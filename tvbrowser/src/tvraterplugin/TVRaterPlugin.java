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
 */

package tvraterplugin;

import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.util.Properties;

import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * This Plugin gives the User the possibility to rate a Movie
 *
 * @author Bodo Tasche
 */
public class TVRaterPlugin extends devplugin.Plugin {
	private Properties _settings;
	private Point _location = null;

	private static final Localizer mLocalizer = Localizer.getLocalizerFor(TVRaterPlugin.class);

	public static TVRaterDB tvraterDB = new TVRaterDB();

	public String getContextMenuItemText() {
		return mLocalizer.msg("contextMenuText", "View rating");
	}

	public PluginInfo getInfo() {
		String name = mLocalizer.msg("pluginName", "TV Rater");
		String desc = mLocalizer.msg("description", "Gives the User the possibility to rate a Show/Movie and get ratings from other Users");
		String author = "Bodo Tasche";
		return new PluginInfo(name, desc, author, new Version(1, 0));
	}


	public void execute(Program program) {
		TVRateDialog dlg = new TVRateDialog(parent, program);
		dlg.pack();
		dlg.addComponentListener(new java.awt.event.ComponentAdapter() {

			public void componentMoved(ComponentEvent e) {
				e.getComponent().getLocation(_location);
			}
		});

		if (_location != null) {
			dlg.setLocation(_location);
			dlg.show();
		} else {
			UiUtilities.centerAndShow(dlg);
			_location = dlg.getLocation();
		}
	}

	public Properties storeSettings() {
	  return _settings;
	}
  
	public void loadSettings(Properties settings) {
	  if (settings == null ) {
		settings = new Properties();
	  }
    
	  this._settings = settings;
	}

	public SettingsTab getSettingsTab() {
	  return new TVRaterSettingsTab(_settings);
	}
	
	public String getMarkIconName() {
		return "tvraterplugin/tvrater.gif";
	}

	public String getButtonText() {
		return null;
	}

	public String getButtonIconName() {
		return null;
	}

}