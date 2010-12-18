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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.apache.commons.lang.StringUtils;

import util.ui.UiUtilities;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
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
  private static final String[][] TITLE_DELIMITERS = new String[][] { { "\"", "\"" }, { "«", "»" }, { "»", "«" },
      { "„", "“" }, {"&#8222;", "&#8220;"} };

  private static final int MIN_MATCH_LENGTH = 3;

  private static final boolean IS_STABLE = false;

  private static final Version mVersion = new Version(2, 70, 1, IS_STABLE);

  private static Icon mIcon;

  private PluginInfo mPluginInfo;

  private ArrayList<SyndFeed> mFeeds = new ArrayList<SyndFeed>();

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FeedsPlugin.class);

  private static final Logger mLog = Logger.getLogger(FeedsPlugin.class.getName());

  private PluginTreeNode mRootNode;

  private FeedsPluginSettings mSettings;

  private boolean mStartFinished;

  private HashMap<String, ArrayList<SyndEntryWithParent>> mKeywords = new HashMap<String, ArrayList<SyndEntryWithParent>>();

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
  public void handleTvBrowserStartFinished() {
    mStartFinished = true;
    updateFeeds();
  }

  @Override
  public void handleTvDataUpdateFinished() {
    if (mStartFinished) {
      updateFeeds();
    }
  }

  private void updateFeeds() {
    synchronized (mFeeds) {
      mFeeds = new ArrayList<SyndFeed>();
    }
    Hashtable<SyndFeed, PluginTreeNode> nodes = new Hashtable<SyndFeed, PluginTreeNode>();
    ArrayList<String> feedUrls = mSettings.getFeeds();
    if (!feedUrls.isEmpty()) {
      final FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
      final FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
      feedFetcher.setUserAgent("TV-Browser Feeds Plugin " + FeedsPlugin.getVersion().toString());
      for (String feedUrl : feedUrls) {
        try {
          final SyndFeed feed = feedFetcher.retrieveFeed(new URL(feedUrl));
          synchronized (mFeeds) {
            mFeeds.add(feed);
          }
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
    mKeywords = new HashMap<String, ArrayList<SyndEntryWithParent>>();
    if (!mFeeds.isEmpty()) {
      for (SyndFeed feed : mFeeds) {
        addFeedKeywords(feed);
      }
      getRootNode().clear();
      ArrayList<String> titles = new ArrayList<String>(mFeeds.size());
      final Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();
      Date date = Date.getCurrentDate();
      final int maxDays = 7;
      synchronized (mFeeds) {
        Collections.sort(mFeeds, new Comparator<SyndFeed>() {
          public int compare(SyndFeed o1, SyndFeed o2) {
            return o1.getTitle().compareToIgnoreCase(o2.getTitle());
          }
        });
        for (SyndFeed feed : mFeeds) {
          PluginTreeNode node = mRootNode.addNode(feed.getTitle());
          List<SyndEntryImpl> entries = feed.getEntries();
          titles.add(feed.getTitle());
          AbstractAction[] subActions = new AbstractAction[entries.size()];
          for (int i = 0; i < subActions.length; i++) {
            final SyndEntryImpl entry = entries.get(i);
            subActions[i] = new AbstractAction(entry.getTitle()) {
              public void actionPerformed(ActionEvent e) {
                showFeedsDialog(new FeedsDialog(getParentFrame(), entry));
              }
            };
          }
          ActionMenu menu = new ActionMenu(new ContextMenuAction(mLocalizer.msg("readEntry", "Read entry")), subActions);
          node.addActionMenu(menu);
          nodes.put(feed, node);
        }
        mSettings.setCachedFeedTitles(titles);
        for (int days = 0; days < maxDays; days++) {
          for (Channel channel : channels) {
            for (Iterator<Program> iter = devplugin.Plugin.getPluginManager().getChannelDayProgram(date, channel); iter
                .hasNext();) {
              final Program prog = iter.next();
                ArrayList<SyndEntryWithParent> matchingEntries = getMatchingEntries(prog.getTitle());
                if (!matchingEntries.isEmpty()) {
                  HashSet<SyndFeed> feeds = new HashSet<SyndFeed>();
                  for (SyndEntryWithParent entry : matchingEntries) {
                    feeds.add(entry.getFeed());
                  }
                  for (SyndFeed syndFeed : feeds) {
                    nodes.get(syndFeed).addProgramWithoutCheck(prog);
                    prog.validateMarking();
                  }
                }
            }
          }
          date = date.addDays(1);
        }
      }
      mRootNode.update();
    }
  }

  private void addFeedKeywords(final SyndFeed feed) {
    final Iterator<?> iterator = feed.getEntries().iterator();
    while (iterator.hasNext()) {
      final SyndEntry entry = (SyndEntry) iterator.next();
      String feedTitle = entry.getTitle();
      // index title or parts
      for (String[] delimiter : TITLE_DELIMITERS) {
        String titlePart = StringUtils.substringBetween(feedTitle, delimiter[0], delimiter[1]);
        if (titlePart != null && !titlePart.isEmpty()) {
          feedTitle = titlePart;
          break;
        }
      }
      addFeedKey(feedTitle, entry, feed);
      // index description parts
      String desc = entry.getDescription().getValue();
      if (desc != null) {
        for (String[] delimiter : TITLE_DELIMITERS) {
          if (desc.contains(delimiter[0])) {
            String[] descParts = StringUtils.substringsBetween(desc, delimiter[0], delimiter[1]);
            if (descParts != null) {
              for (String descPart : descParts) {
                if (!descPart.isEmpty()) {
                  addFeedKey(descPart, entry, feed);
                }
              }
              break;
            }
          }
        }
      }
    }
  }

  private void addFeedKey(final String keyWord, final SyndEntry feedEntry, final SyndFeed feed) {
    ArrayList<SyndEntryWithParent> list = mKeywords.get(keyWord);
    if (list == null) {
      list = new ArrayList<SyndEntryWithParent>(1);
      mKeywords.put(keyWord, list);
    }
    list.add(new SyndEntryWithParent(feedEntry, feed));
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    return getContextMenuAction(getMatchingEntries(program.getTitle()));
  }

  private ActionMenu getContextMenuAction(final ArrayList<SyndEntryWithParent> matches) {
    if (matches.isEmpty()) {
      return null;
    }
    final ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>(matches.size());
    for (SyndEntryWithParent match : matches) {
      entries.add(match.getEntry());
    }
    AbstractAction action = new AbstractAction(mLocalizer.msg("contextMenu", "Feeds {0}", matches.size()),
        getPluginIcon()) {
      public void actionPerformed(final ActionEvent e) {
        showFeedsDialog(new FeedsDialog(getParentFrame(), entries));
      }
    };
    return new ActionMenu(action);
  }

  @Override
  public ActionMenu getContextMenuActions(Channel channel) {
    return getContextMenuAction(getMatchingEntries(channel.getName()));
  }

  protected void showFeedsDialog(final FeedsDialog dialog) {
    dialog.pack();
    UiUtilities.setSize(dialog, 600, 400);
    UiUtilities.centerAndShow(dialog);
  }

  private ArrayList<SyndEntryWithParent> getMatchingEntries(final String searchString) {
    ArrayList<SyndEntryWithParent> matches = new ArrayList<SyndEntryWithParent>();
    String quotedString = quoteTitle(searchString);
    for (Entry<String, ArrayList<SyndEntryWithParent>> entry : mKeywords.entrySet()) {
      if (matchesTitle(entry.getKey(), searchString, quotedString )) {
        matches.addAll(entry.getValue());
      }
    }
    return matches;
  }

  private boolean hasMatchingEntries(final String searchString) {
    String quotedString = quoteTitle(searchString);
    for (Entry<String, ArrayList<SyndEntryWithParent>> entry : mKeywords.entrySet()) {
      if (matchesTitle(entry.getKey(), searchString, quotedString)) {
        return true;
      }
    }
    return false;
  }

  private boolean matchesTitle(final String feedTitle, final String programTitle, String quotedTitle) {
    if (programTitle.isEmpty()) {
      return false;
    }
    if (feedTitle.equalsIgnoreCase(programTitle)) {
      return true;
    }
    if (StringUtils.containsIgnoreCase(feedTitle, programTitle)) {
      if (quotedTitle == null) {
        quotedTitle = quoteTitle(programTitle);
      }
      if (feedTitle.matches(quotedTitle)) {
        return true;
      }
    }
    if (programTitle.contains(" - ")) {
      String[] parts = StringUtils.splitByWholeSeparator(programTitle, " - ");
      if (parts[0].length() >= MIN_MATCH_LENGTH) {
        return matchesTitle(feedTitle, parts[0], null);
      }
    } else if (programTitle.endsWith(")")) {
      int index = programTitle.lastIndexOf("(");
      if (index > 0) {
        // try without the suffix in brackets, which might be a part number or
        // the like
        return matchesTitle(feedTitle, programTitle.substring(0, index).trim(), null);
      }
    } else if (!Character.isLetterOrDigit(programTitle.charAt(programTitle.length() - 1))) {
      String shortProgramTitle = removePunctuation(programTitle);
      String shortFeedTitle = removePunctuation(feedTitle);
      return matchesTitle(shortFeedTitle, shortProgramTitle, null);
    }
    return false;
  }

  private String quoteTitle(final String programTitle) {
    return ".*\\b" + Pattern.quote(programTitle) + "\\b.*";
  }

  private String removePunctuation(String title) {
    while (title.length() > 0 && !Character.isLetterOrDigit(title.charAt(title.length() - 1))) {
      title = title.substring(0, title.length() - 1);
    }
    return title;
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

  @Override
  public Icon[] getProgramTableIcons(final Program program) {
    if (program == getPluginManager().getExampleProgram()) {
      return new Icon[] { getPluginIcon() };
    }
    if (hasMatchingEntries(program.getTitle())) {
      return new Icon[] { getPluginIcon() };
    }
    return null;
  }

  @Override
  public String getProgramTableIconText() {
    return mLocalizer.msg("name", "Feeds");
  }

  @Override
  public ActionMenu getButtonAction() {
    ArrayList<String> feedTitles = mSettings.getCachedFeedTitles();
    if (feedTitles.isEmpty()) {
      return null;
    }
    ContextMenuAction mainAction = new ContextMenuAction(mLocalizer.msg("name", "Feeds"), getPluginIcon());
    ArrayList<AbstractAction> list = new ArrayList<AbstractAction>(feedTitles.size());
    for (int i = 0; i < feedTitles.size(); i++) {
      final String title = feedTitles.get(i);
      final int feedIndex = i;
      list.add(new AbstractAction(title) {

        @Override
        public void actionPerformed(ActionEvent e) {
          SyndFeed selectedFeed = mFeeds.get(feedIndex);
          if (selectedFeed != null) {
            List entries = selectedFeed.getEntries();
            showFeedsDialog(new FeedsDialog(getParentFrame(), entries));
          }
        }
      });
    }
    Collections.sort(list, new Comparator<AbstractAction>() {

      @Override
      public int compare(AbstractAction o1, AbstractAction o2) {
        return ((String) o1.getValue(AbstractAction.NAME)).compareTo((String) o2.getValue(AbstractAction.NAME));
      }
    });
    return new ActionMenu(mainAction, list.toArray(new AbstractAction[list.size()]));
  }
}