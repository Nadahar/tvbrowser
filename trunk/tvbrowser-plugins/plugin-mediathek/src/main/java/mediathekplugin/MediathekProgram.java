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

import javax.swing.AbstractAction;
import javax.swing.Action;

import mediathekplugin.parser.IParser;
import util.browserlauncher.Launch;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.PluginTreeNode;

public final class MediathekProgram implements Comparable<MediathekProgram> {
  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(MediathekProgram.class);

  private String mTitle;
  private String mTitleLower;
  private String mUrl;
  private ArrayList<MediathekProgramItem> mItems;
  private static MediathekPlugin plugin = MediathekPlugin.getInstance();

  private PluginTreeNode mPluginTreeNode;

  private IParser mParser;

  public MediathekProgram(final IParser parser, final String title,
      final String url) {
    this.mParser = parser;
    this.mTitle = parser.fixTitle(title);
    this.mTitleLower = title.toLowerCase();
    this.mUrl = url;
    // initialize with null to be able to differentiate between (yet) unknown
    // and empty programs
    this.mItems = null;
  }

  public String getTitle() {
    return mTitle;
  }

  public String getUrl() {
    return mUrl;
  }

  public void addItem(final MediathekProgramItem item) {
    mItems.add(item);
  }

  public int getItemCount() {
    if (mItems == null) {
      return -1;
    }
    return mItems.size();
  }

  public ArrayList<MediathekProgramItem> getItems() {
    return mItems;
  }

  public void updatePluginTree(final boolean refreshUI) {
    if (mPluginTreeNode == null) {
      mPluginTreeNode = new MediathekProgramNode(this);
      plugin.getRootNode().add(mPluginTreeNode);
    }
    if (mItems != null && mItems.size() > 0) {
      for (MediathekProgramItem episode : getItems()) {
        mPluginTreeNode.add(new EpisodeNode(episode));
      }
      mPluginTreeNode.getMutableTreeNode().setShowLeafCountEnabled(true);
      if (refreshUI) {
        mPluginTreeNode.update();
      }
    }
  }

  protected void readEpisodes() {
    if ((getItemCount() == -1) && mParser.canReadEpisodes()) {
      UpdateThread.getInstance().addProgram(this);
    }
  }

  protected void parseEpisodes(final UpdateThread thread) {
    initializeItems();
    mParser.parseEpisodes(this);
  }

  protected ActionMenu actionMenuShowEpisodes() {
    final LaunchBrowserAction openURLAction = new LaunchBrowserAction(getUrl(),
        mLocalizer.msg("context.open", "Show Mediathek"));
    if (getItemCount() > 0) {
      final Action mainAction = new ContextMenuAction(mLocalizer.msg(
          "context.episodes", "Episodes in the Mediathek"), plugin
          .getContextMenuIcon());
      final ArrayList<Action> actionList = new ArrayList<Action>();
      for (final MediathekProgramItem item : getItems()) {
        actionList.add(new AbstractAction(item.getTitle()) {

          public void actionPerformed(final ActionEvent e) {
            Launch.openURL(item.getUrl());
          }
        });
      }
      actionList.add(ContextMenuSeparatorAction.getInstance());
      actionList.add(openURLAction);
      return new ActionMenu(mainAction, actionList
          .toArray(new Action[actionList.size()]));
    } else {
      return new ActionMenu(openURLAction);
    }
  }

  public int compareTo(final MediathekProgram other) {
    return mTitleLower.compareTo(other.mTitleLower);
  }

  protected void initializeItems() {
    if (mItems == null) {
      mItems = new ArrayList<MediathekProgramItem>();
    }
  }

  public boolean canReadEpisodes() {
    return mParser.canReadEpisodes();
  }

}