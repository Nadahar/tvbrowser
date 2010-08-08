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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.apache.commons.lang.StringUtils;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import util.ui.UiUtilities;

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
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public final class FeedsPlugin extends Plugin {
  private static final boolean IS_STABLE = false;

  private static final Version mVersion = new Version(2, 70, 0, IS_STABLE);

  private static Icon mIcon;

  private PluginInfo mPluginInfo;

  private ArrayList<SyndFeed> mFeeds = new ArrayList<SyndFeed>();

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FeedsPlugin.class);

  private static final Logger mLog = Logger.getLogger(FeedsPlugin.class.getName());

  private PluginTreeNode mRootNode;

  private FeedsPluginSettings mSettings;

  private static FeedsPlugin mInstance;

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

  public FeedsPlugin() {
    mInstance = this;
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
    updateFeeds();
  }

  private void updateFeeds() {
    mFeeds = new ArrayList<SyndFeed>();
    Hashtable<SyndFeed, PluginTreeNode> nodes = new Hashtable<SyndFeed, PluginTreeNode>();
    ArrayList<String> feedUrls = mSettings.getFeeds();
    if (!feedUrls.isEmpty()) {
      final FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
      final FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
      feedFetcher.setUserAgent("TV-Browser Feeds Plugin " + FeedsPlugin.getVersion().toString());
      for (String feedUrl : feedUrls) {
        try {
          final SyndFeed feed = feedFetcher.retrieveFeed(new URL(feedUrl));
          mFeeds.add(feed);
          mLog.info("Loaded " + feed.getEntries().size() + " feed entries from " + feedUrl);
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
    }
    if (!mFeeds.isEmpty()) {
      Collections.sort(mFeeds, new Comparator<SyndFeed>() {
        public int compare(SyndFeed o1, SyndFeed o2) {
          return o1.getTitle().compareToIgnoreCase(o2.getTitle());
        }

      });
      getRootNode().clear();
      for (SyndFeed feed : mFeeds) {
        PluginTreeNode node = mRootNode.addNode(feed.getTitle());
        nodes.put(feed, node);
      }

      final Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();
      Date date = Date.getCurrentDate();
      final int maxDays = 7;
      for (int days = 0; days < maxDays; days++) {
        for (Channel channel : channels) {
          for (Iterator<Program> iter = devplugin.Plugin.getPluginManager().getChannelDayProgram(date, channel); iter
              .hasNext();) {
            final Program prog = iter.next();
            for (SyndFeed feed : mFeeds) {
              if (!getMatchingEntries(prog, feed).isEmpty()) {
                nodes.get(feed).addProgramWithoutCheck(prog);
              }
            }
          }
        }
        date = date.addDays(1);
      }
      mRootNode.update();
    }
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    // find matches
    ArrayList<SyndEntry> matches = getMatchingEntries(program);
    if (matches.isEmpty()) {
      return null;
    }
    AbstractAction action = new AbstractAction(mLocalizer.msg("name", "Feeds"), getPluginIcon()) {
      public void actionPerformed(final ActionEvent e) {
        FeedsDialog dialog = new FeedsDialog(getParentFrame(), program);
        dialog.pack();
        UiUtilities.setSize(dialog, 600, 400);
        UiUtilities.centerAndShow(dialog);
      }
    };
    return new ActionMenu(action);
  }

  private ArrayList<SyndEntry> getMatchingEntries(final Program program, final SyndFeed feed) {
    ArrayList<SyndEntry> matches = new ArrayList<SyndEntry>();
    final Iterator<?> iterator = feed.getEntries().iterator();
    while (iterator.hasNext()) {
      final SyndEntry entry = (SyndEntry) iterator.next();
      String title = entry.getTitle();
      if (StringUtils.containsIgnoreCase(title, program.getTitle())) {
        matches.add(entry);
      }
    }
    return matches;
  }

  ArrayList<SyndEntry> getMatchingEntries(final Program program) {
    ArrayList<SyndEntry> result = new ArrayList<SyndEntry>();
    for (SyndFeed feed : mFeeds) {
      result.addAll(getMatchingEntries(program, feed));
    }
    return result;
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    if (mRootNode == null) {
      mRootNode = new PluginTreeNode(this);
      mRootNode.getMutableTreeNode().setIcon(getPluginIcon());
    }
    return mRootNode;
  }

  static Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = getInstance().createImageIcon("apps", "feeds", 16);
    }
    return mIcon;
  }

  static FeedsPlugin getInstance() {
    return mInstance;
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new FeedsSettingsTab(mSettings);
  }

  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  @Override
  public void loadSettings(Properties properties) {
    mSettings = new FeedsPluginSettings(properties);
  }
}