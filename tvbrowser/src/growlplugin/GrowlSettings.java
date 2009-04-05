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

/**
 * settings container for the Growl plugin
 * 
 * @author Bananeweizen
 * 
 */
public class GrowlSettings {
  private static final String KEY_DESCRIPTION = "description";
  private static final String KEY_TITLE = "title";

  private Properties mProperties;

  public GrowlSettings(final Properties properties) {
    if (properties == null) {
      mProperties = new Properties();
    }
    else {
      mProperties = properties;
    }
  }

  /**
   * get the title to use in the Growl notification
   * 
   * @return the title
   */
  public String getTitle() {
    return mProperties
        .getProperty(KEY_TITLE,
            "{leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")} {title}");
  }

  /**
   * get the description to use in the Growl notification
   * 
   * @return the description
   */
  public String getDescription() {
    return mProperties.getProperty(KEY_DESCRIPTION,
        "{channel_name}\n{short_info}");
  }

  protected Properties store() {
    return mProperties;
  }

  public void setTitle(final String title) {
    mProperties.setProperty(KEY_TITLE, title);
  }

  public void setDescription(final String description) {
    mProperties.setProperty(KEY_DESCRIPTION, description);
  }
}
