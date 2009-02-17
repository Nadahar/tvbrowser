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

import java.util.Enumeration;

public class TeleTextChannelProperties extends ChannelProperties {
  private static java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(TeleTextPlugin.class.getName());

  @Override
  protected void checkProperties() {
    for (final Enumeration<?> keys = mProperties.propertyNames(); keys
        .hasMoreElements();) {
      final String key = (String) keys.nextElement();
      String url = mProperties.getProperty(key);
      if (url != null && url.length() > 0) {
        // is this a mapping only?
        if (!url.startsWith("http")) {
          url = mProperties.getProperty(url);
          if (url != null) {
            mProperties.put(key, url);
          } else {
            mLog.warning("Bad teletext mapping for " + key);
          }
        }
        if (url == null || url.length() == 0 || !url.startsWith("http")) {
          mLog.warning("Bad teletext URL " + url);
        }
      } else {
        mLog.warning("Bad teletext key " + key + "=" + url);
      }
    }
  }

  protected boolean isValidProperty(final String property) {
    return property != null && property.length() > 0
        && property.startsWith("http");
  }

}
