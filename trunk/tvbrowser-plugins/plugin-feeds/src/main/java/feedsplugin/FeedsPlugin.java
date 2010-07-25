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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public final class FeedsPlugin extends Plugin {
  private static final boolean IS_STABLE = false;

  private static final Version mVersion = new Version(2, 70, 0, IS_STABLE);

  private PluginInfo mPluginInfo;

  private ArrayList<SyndEntry> mEntries = new ArrayList<SyndEntry>();

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FeedsPlugin.class);

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Feeds");
      final String desc = mLocalizer.msg("description", "Associates entries from feeds with programs.");
      mPluginInfo = new PluginInfo(FeedsPlugin.class, name, desc, "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  @Override
  public void handleTvDataAdded(final MutableChannelDayProgram newProg) {
    final Iterator<Program> iterator = newProg.getPrograms();
    if (iterator != null) {
      while (iterator.hasNext()) {
        final MutableProgram program = (MutableProgram) iterator.next();
      }
    }
  }

  @Override
  public void handleTvBrowserStartFinished() {
    String url = "http://www.klack.de/xml/klackTopRSS.xml";
    readRSS(url);
  }

  private void readRSS(final String rssUrl) {
    mEntries = new ArrayList<SyndEntry>();
    final FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
    final FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
    feedFetcher.setUserAgent("TV-Browser Feeds Plugin " + FeedsPlugin.getVersion().toString());
    try {
      int count = 0;
      final SyndFeed feed = feedFetcher.retrieveFeed(new URL(rssUrl));
      final Iterator<?> iterator = feed.getEntries().iterator();
      while (iterator.hasNext()) {
        final SyndEntry entry = (SyndEntry) iterator.next();
        mEntries.add(entry);
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (FeedException e) {
      e.printStackTrace();
    } catch (FetcherException e) {
      e.printStackTrace();
    }
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    // find matches
    ArrayList<SyndEntry> matches = new ArrayList<SyndEntry>();
    for (SyndEntry entry : mEntries) {
      String title = entry.getTitle();
      Channel entryChannel = null;
      for (Channel channel : Plugin.getPluginManager().getSubscribedChannels()) {
        if (title.toLowerCase().contains(channel.getName().toLowerCase())) {
          entryChannel = channel;
        }
      }
      if (title.toLowerCase().contains(program.getTitle().toLowerCase())) {
        matches.add(entry);
      }
    }
    if (matches.isEmpty()) {
      return null;
    }
    // sort entries
    Collections.sort(matches, new Comparator<SyndEntry>() {
      public int compare(SyndEntry o1, SyndEntry o2) {
        return o1.getTitle().compareToIgnoreCase(o2.getTitle());
      }
    });
    ArrayList<AbstractAction> actions = new ArrayList<AbstractAction>();
    for (final SyndEntry entry : matches) {
      AbstractAction action = new AbstractAction(entry.getTitle()) {
        public void actionPerformed(final ActionEvent e) {
          JOptionPane.showMessageDialog(getParentFrame(), entry.getDescription());
        }
      };
      actions.add(action);
    }
    final ContextMenuAction baseAction = new ContextMenuAction(mLocalizer.msg("name", "Feeds"), createImageIcon("apps",
        "feeds", 16));

    return new ActionMenu(baseAction, actions.toArray(new AbstractAction[actions.size()]));
  }
}
