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
package growlplugin;

import java.util.Properties;

import util.settings.PropertyBasedSettings;

/**
 * settings container for the Growl plugin
 * 
 * @author Bananeweizen
 * 
 */
public final class GrowlSettings extends PropertyBasedSettings {
  private static final String KEY_DESCRIPTION = "description";
  private static final String KEY_TITLE = "title";

  public GrowlSettings(final Properties properties) {
    super(properties);
  }

  /**
   * get the title to use in the Growl notification
   * 
   * @return the title
   */
  public String getTitle() {
    return get(KEY_TITLE,
            "{leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")} {title}");
  }

  /**
   * get the description to use in the Growl notification
   * 
   * @return the description
   */
  public String getDescription() {
    return get(KEY_DESCRIPTION,"{channel_name}\n{short_info}");
  }

  public void setTitle(final String title) {
    set(KEY_TITLE, title);
  }

  public void setDescription(final String description) {
    set(KEY_DESCRIPTION, description);
  }
}
