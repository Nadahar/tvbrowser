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
package feedsplugin;

import java.util.ArrayList;
import java.util.Properties;

import util.settings.PropertyBasedSettings;

class FeedsPluginSettings extends PropertyBasedSettings {

  private static final String KEY_COUNT_FEEDS = "countFeeds";
  private static final String KEY_FEED_URL = "feedUrl";
  private static final String KEY_FEED_TITLE = "feedTitle";

  public FeedsPluginSettings(final Properties properties) {
    super(properties);
  }

  public ArrayList<String> getFeeds() {
    int count = get(KEY_COUNT_FEEDS, 0);
    ArrayList<String> list = new ArrayList<String>();
    for (int i = 0; i < count; i++) {
      list.add(get(KEY_FEED_URL + String.valueOf(i), ""));
    }
    return list;
  }

  public void setFeeds(final ArrayList<String> feeds) {
    set(KEY_COUNT_FEEDS, feeds.size());
    for (int i = 0; i < feeds.size(); i++) {
      set(KEY_FEED_URL + String.valueOf(i), feeds.get(i));
    }
  }

  public ArrayList<String> getCachedFeedTitles() {
    ArrayList<String> urls = getFeeds();
    int count = urls.size();
    ArrayList<String> list = new ArrayList<String>();
    for (int i = 0; i < count; i++) {
      list.add(get(KEY_FEED_TITLE + String.valueOf(i), urls.get(i)));
    }
    return list;
  }

  public void setCachedFeedTitles(final ArrayList<String> titles) {
    for (int i = 0; i < titles.size(); i++) {
      set(KEY_FEED_TITLE + String.valueOf(i), titles.get(i));
    }
  }

}
