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

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public class SyndEntryWithParent {
  private SyndEntry mEntry;
  private SyndFeed mFeed;

  public SyndEntryWithParent(final SyndEntry entry, final SyndFeed feed) {
    mEntry = entry;
    mFeed = feed;
  }

  public SyndFeed getFeed() {
    return mFeed;
  }

  public SyndEntry getEntry() {
    return mEntry;
  }
}
