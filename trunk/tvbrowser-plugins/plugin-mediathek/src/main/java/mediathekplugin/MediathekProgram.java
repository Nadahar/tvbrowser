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
package mediathekplugin;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;

import util.browserlauncher.Launch;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.PluginTreeNode;

public class MediathekProgram implements Comparable<MediathekProgram> {
  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(MediathekProgram.class);

  private String mTitle;
  private String mTitleLower;
  private String mUrl;
  private ArrayList<MediathekProgramItem> mItems;
  private Date mLastUpdate = null;
  private static MediathekPlugin plugin = MediathekPlugin.getInstance();

  private String rssFeedUrl = null;

  private PluginTreeNode mPluginTreeNode;

  public MediathekProgram(String title, String url) {
    this.mTitle = title;
    this.mTitleLower = title.toLowerCase();
    this.mUrl = url;
    this.mItems = new ArrayList<MediathekProgramItem>();
  }

  public String getTitle() {
    return mTitle;
  }

  public String getUrl() {
    return mUrl;
  }

  public void addItem(MediathekProgramItem item) {
    mItems.add(item);
  }

  public int getItemCount() {
    return mItems.size();
  }

  public ArrayList<MediathekProgramItem> getItems() {
    return mItems;
  }

  private void readRSS() {
    if ((mLastUpdate != null)
        && (new Date()).getTime() - mLastUpdate.getTime() < 10000) {
      return;
    }
    String rss = plugin.readUrl(rssFeedUrl);
    int count = 0;
    if (rss.indexOf("item") > 0) {
      Pattern pattern = Pattern.compile(
          "<title>([^<]+)</title>(.*?)<link>([^<]+)</link>", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(rss);
      matcher.region(rss.indexOf("item"), rss.length());
      while (matcher.find()) {
        String title = plugin.convertHTML(matcher.group(1));
        String link = matcher.group(3);
        addItem(new MediathekProgramItem(title, link));
        count++;
      }
    }
    logInfo("Read " + count + " episodes for " + getTitle());
    mLastUpdate = new Date();
    updatePluginTree(true);
  }

  protected void updatePluginTree(boolean refreshUI) {
    if (mPluginTreeNode == null) {
      mPluginTreeNode = new MediathekProgramNode(getTitle());
      plugin.getRootNode().add(mPluginTreeNode);
    }
    if (mItems.size() > 0) {
      for (MediathekProgramItem episode : getItems()) {
        PluginTreeNode episodeNode = new EpisodeNode(episode.getTitle());
        mPluginTreeNode.add(episodeNode);
      }
      mPluginTreeNode.getMutableTreeNode().setShowLeafCountEnabled(true);
      if (refreshUI) {
        mPluginTreeNode.update();
      }
    }
  }

  private void logInfo(String string) {
    MediathekPlugin.getInstance().getLogger().info(string);
  }

  private void logWarning(String string) {
    MediathekPlugin.getInstance().getLogger().warning(string);
  }

  protected void readEpisodes() {
    if (rssFeedUrl == null) {
      int num = Integer.parseInt(mUrl.substring(mUrl.indexOf("/") + 1));
      rssFeedUrl = "http://www.zdf.de/ZDFMediathek/content/"
          + Integer.toString(num) + "?view=rss";
    }
    if (rssFeedUrl != null) {
      readRSS();
    }
  }

  protected ActionMenu actionMenuShowEpisodes() {
    Action mainAction = new ContextMenuAction(mLocalizer.msg(
        "contextMenuEpisodes", "Episodes in the Mediathek"), plugin
        .getContextMenuIcon());
    ArrayList<Action> actionList = new ArrayList<Action>();
    for (final MediathekProgramItem item : getItems()) {
      actionList.add(new AbstractAction(item.getTitle()) {

        public void actionPerformed(ActionEvent e) {
          Launch.openURL(item.getUrl());
        }
      });
    }
    Action[] subActions = new Action[actionList.size()];
    actionList.toArray(subActions);
    return new ActionMenu(mainAction, subActions);
  }

  public int compareTo(MediathekProgram other) {
    return mTitleLower.compareTo(other.mTitleLower);
  }
}