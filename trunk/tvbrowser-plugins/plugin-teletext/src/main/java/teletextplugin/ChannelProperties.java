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
package teletextplugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import devplugin.Channel;

/**
 * @author bananeweizen
 *
 */
public abstract class ChannelProperties {
  private String mFileName;
  protected Properties mProperties;

  protected ChannelProperties() {
    this("channels.properties");
  }

  protected ChannelProperties(final String fileName) {
    mFileName = fileName;
    if (!mFileName.toLowerCase().endsWith(".properties")) {
      mFileName += ".properties";
    }
  }

  private void initializeProperties() {
    // load the properties
    if (mProperties == null) {
      final InputStream stream = getClass().getResourceAsStream(mFileName);
      mProperties = new Properties();
      try {
        mProperties.load(stream);
      } catch (IOException e) {
        e.printStackTrace();
      }
      checkProperties();
    }
  }

  protected abstract void checkProperties();

  public String getProperty(final Channel channel) {
    try {
      initializeProperties();
      final String channelCountry = channel.getCountry();
      final String channelName = channel.getDefaultName();
      for (final Enumeration<?> keys = mProperties.propertyNames(); keys
          .hasMoreElements();) {
        final String key = (String) keys.nextElement();
        final int separatorIndex = key.indexOf(',');
        if (separatorIndex > 0) {
          final String[] countries = key.substring(0, separatorIndex).split(
              "\\W");
          final String name = "(?i)"
              + key.substring(separatorIndex + 1).replaceAll("\\*", ".*");
          for (String country : countries) {
            if (channelCountry.equalsIgnoreCase(country)
                && channelName.matches(name)) {
              String property = mProperties.getProperty(key);
               // is this a real property
              if (isValidProperty(property)) {
                return property;
              } else {
                // or only a redirect?
                property = mProperties.getProperty(property);
                if (isValidProperty(property)) {
                  return property;
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    // nothing found
    return null;
  }

  protected abstract boolean isValidProperty(final String property);
}
