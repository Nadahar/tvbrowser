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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	private Point _locationRaterDialog = null;

	private Point _locationOverviewDialog = null;
	private Dimension _dimensionOverviewDialog = null;

	private static final Localizer mLocalizer = Localizer.getLocalizerFor(TVRaterPlugin.class);

	private Database tvraterDB = new Database();

	public String getContextMenuItemText() {
		return mLocalizer.msg("contextMenuText", "View rating");
	}

	public PluginInfo getInfo() {
		String name = mLocalizer.msg("pluginName", "TV Rater");
		String desc = mLocalizer.msg("description", "Gives the User the possibility to rate a Show/Movie and get ratings from other Users");
		String author = "Bodo Tasche";
		return new PluginInfo(name, desc, author, new Version(0, 1));
	}

	/**
	 * This method is invoked by the host-application if the user has choosen your
	 * plugin from the menu.
	 */
	public void execute() {
		DialogOverview dlg = new DialogOverview(getParentFrame(), tvraterDB);
		dlg.pack();
		dlg.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				_dimensionOverviewDialog = e.getComponent().getSize();
			}
			
			public void componentMoved(ComponentEvent e) {
				e.getComponent().getLocation(_locationOverviewDialog);
			}
		});

		if ((_locationOverviewDialog != null) && (_dimensionOverviewDialog != null)) {
			dlg.setLocation(_locationOverviewDialog);
			dlg.setSize(_dimensionOverviewDialog);
			dlg.show();
		} else {
			dlg.setSize(300, 250);
			UiUtilities.centerAndShow(dlg);
			_locationOverviewDialog = dlg.getLocation();
			_dimensionOverviewDialog = dlg.getSize();
		}
		
	}

	public void execute(Program program) {
		DialogRating dlg = new DialogRating(getParentFrame(), program, tvraterDB);
		dlg.pack();
		dlg.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				e.getComponent().getLocation(_locationRaterDialog);
			}
		});

		if (_locationRaterDialog != null) {
			dlg.setLocation(_locationRaterDialog);
			dlg.show();
		} else {
			UiUtilities.centerAndShow(dlg);
			_locationRaterDialog = dlg.getLocation();
		}
	}

	public Properties storeSettings() {
		return _settings;
	}

	public void loadSettings(Properties settings) {
		if (settings == null) {
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
		return "TVRater";
	}

	public String getButtonIconName() {
		return "tvraterplugin/tvrater.gif";
	}

	/**
	 * Called by the host-application during start-up. 
	 *
	 * @see #writeData(ObjectOutputStream)
	 */
	public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
		tvraterDB.readData(in);
	}

	/**
	 * Counterpart to loadData. Called when the application shuts down.
	 *
	 * @see #readData(ObjectInputStream)
	 */
	public void writeData(ObjectOutputStream out) throws IOException {
		tvraterDB.writeData(out);
	}

}