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
package knotifyplugin;

import java.util.Properties;

import util.settings.PropertyBasedSettings;

/**
 * @author bananeweizen
 *
 */
public final class KNotifySettings extends PropertyBasedSettings {

  public KNotifySettings(final Properties properties) {
    super(properties);
  }

  public String getTitle() {
    return get("title", "{channel_name}, {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")} - {title}");
  }

  public String getDescription() {
    return get("description", "{splitAt(short_info,\"80\")}");
  }

  public void setTitle(final String title) {
    set("title", title);
  }

  public void setDescription(final String description) {
    set("description", description);
  }

}
