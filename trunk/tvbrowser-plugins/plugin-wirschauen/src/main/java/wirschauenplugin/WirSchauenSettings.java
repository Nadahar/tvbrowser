/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package wirschauenplugin;

import java.util.Properties;

public class WirSchauenSettings extends PropertyBasedSettings {

	/**
	 * this is the key used to store the show markings option in a property
	 * (see storeSettings and loadSettings).
	 */
	private static final String OPTION_KEY_SHOW_MARKINGS = "showMarkings";

	public WirSchauenSettings(final Properties properties) {
		super(properties);
	}

	/**
	 * whether the programs with OMDB text shall be marked
	 * @return
	 */
	public boolean getMarkPrograms() {
		return get(OPTION_KEY_SHOW_MARKINGS, false);
	}

	public void setMarkPrograms(final boolean showMarkings) {
    boolean updateMarkings = showMarkings != getMarkPrograms();
		set(OPTION_KEY_SHOW_MARKINGS, showMarkings);
		// if the setting changed, we need to update the graphics
		if (updateMarkings) {
      WirSchauenPlugin.getInstance().updateMarkings(showMarkings);
		}
	}

}
