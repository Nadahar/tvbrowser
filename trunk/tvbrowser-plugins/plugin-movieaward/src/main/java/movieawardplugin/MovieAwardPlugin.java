/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *     $Date: 2007-10-02 10:19:08 +0200 (Di, 02 Okt 2007) $
 *   $Author: Bananeweizen $
 * $Revision: 3966 $
 */
package movieawardplugin;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import util.misc.SoftReferenceCache;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.Version;

public class MovieAwardPlugin extends Plugin {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(MovieAwardPlugin.class);
  private static Logger mLog = Logger.getLogger(MovieAwardPlugin.class.getName());

  private static final Version mVersion = new Version(0, 2);
  private PluginInfo mPluginInfo;
  private ArrayList<MovieAward> mMovieAwards;
  private Icon mIcon;
  /** Small Cache to speed up comparison of programs */
  private SoftReferenceCache<Program, Boolean> mAwardCache = new SoftReferenceCache<Program, Boolean>();
  /**
   * root node of the plugin tree
   */
  private PluginTreeNode mRootNode;
  /**
   * sub node containing all award movies by date
   */
  private PluginTreeNode mDateNode;
  /**
   * sub node containing all award movies by award
   */
  private PluginTreeNode mAwardNode;
  /**
   * hash to remember which awards are currently shown in plugin tree
   */
  private HashMap<MovieAward, PluginTreeNode> mAwardNodes = new HashMap<MovieAward, PluginTreeNode>();
  /**
   * remember start finished state of main program
   */
  private boolean mStartFinished;

  public MovieAwardPlugin() {
  }

  @Override
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      String name = mLocalizer.msg("pluginName", "Movie Awards");
      String desc = mLocalizer.msg("description", "Shows movie awards");
      String author = "TV-Browser Team";

      mPluginInfo = new PluginInfo(MovieAwardPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  public static Version getVersion() {
    return mVersion;
  }

  private void initDatabase() {
    // might be called multiple times
    if (mMovieAwards == null) {
      mMovieAwards = new ArrayList<MovieAward>();
      mMovieAwards.add(MovieDataFactory.loadMovieDataFromStream(getClass().getResourceAsStream("data/oscars.xml")));
      mMovieAwards.add(MovieDataFactory.loadMovieDataFromStream(getClass().getResourceAsStream("data/europeanmovieawards.xml")));
      mMovieAwards.add(MovieDataFactory.loadMovieDataFromStream(getClass().getResourceAsStream("data/cannes.xml")));
      mMovieAwards.add(MovieDataFactory.loadMovieDataFromStream(getClass().getResourceAsStream("data/grimme.xml")));
    }

    mLog.info("loaded movie award. " + mMovieAwards.size());
  }

  @Override
  public void onActivation() {
    initDatabase();
  }

  @Override
  public Icon[] getProgramTableIcons(Program program) {
    if (hasAwards(program)) {
      return new Icon[] {getPluginIcon()};
    }

    return null;
  }

  public String getProgramTableIconText() {
    return mLocalizer.msg("pluginName", "Movie Awards");
  }

  public ActionMenu getContextMenuActions(final Program program) {
    if (getPluginManager().getExampleProgram().equals(program) || hasAwards(program)) {
      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              showAwardDialog(program);
            }
          });
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("contextMenuShowAwards", "Show Awards"));
      action.putValue(Action.SMALL_ICON, getPluginIcon());
      return new ActionMenu(action);
    }
    return null;
  }

  private void showAwardDialog(Program program) {
    final Window window = UiUtilities.getLastModalChildOf(getParentFrame());

    final MovieAwardDialog dialog;

    if (window instanceof JDialog) {
      dialog = new MovieAwardDialog((JDialog)window, mMovieAwards, program);
    } else {
      dialog = new MovieAwardDialog((JFrame)window, mMovieAwards, program);
    }
    UiUtilities.registerForClosing(dialog);
    UiUtilities.centerAndShow(dialog);
  }

  private boolean hasAwards(final Program program) {
    if (mAwardCache.containsKey(program)) {
      return mAwardCache.get(program);
    }

    for (MovieAward award : mMovieAwards) {
      if (award.containsAwardFor(program)) {
        mAwardCache.put(program, true);
        addToPluginTree(program, award);
        return true;
      }
    }

    mAwardCache.put(program, false);
    return false;
  }

  public Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("movieaward.png"));
    }
    return mIcon;
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    if (mRootNode == null) {
      mRootNode = new PluginTreeNode(this, false);
      mRootNode.getMutableTreeNode().setShowLeafCountEnabled(false);
      if (mStartFinished) {
        // update the tree as the plugin view has been switched on for the first
        // time after start
        updateRootNode();
      }
    }
    return mRootNode;
  }

  private void addToPluginTree(Program program, MovieAward award) {
    mDateNode.addProgram(program);
    PluginTreeNode node = mAwardNodes.get(award);
    if (node == null) {
      node = new PluginTreeNode(award.getName());
      node.setGroupingByDateEnabled(false);
      mAwardNodes.put(award, node);
      mAwardNode.add(node);
    }
    node.addProgram(program);
    mRootNode.update();
  }

  @Override
  public void handleTvBrowserStartFinished() {
    mStartFinished = true;
    // update tree, but only if it is shown at all
    if (mRootNode != null) {
      updateRootNode();
    }
  }

  private void updateRootNode() {
    if (!mStartFinished) {
      return;
    }
    mDateNode = new PluginTreeNode(mLocalizer.msg("dateNode", "Datum"));
    mAwardNode = new PluginTreeNode(mLocalizer.msg("awardNode", "Award"));
    mAwardNode.setGroupingByDateEnabled(false);
    mRootNode.add(mAwardNode);
    mRootNode.add(mDateNode);
    
    // search all awards
    Channel[] channels = devplugin.Plugin.getPluginManager()
        .getSubscribedChannels();
    Date date = Date.getCurrentDate();
    for (int days = 0; days < 30; days++) {
      for (int i = 0; i < channels.length; ++i) {
        Iterator<Program> iter = devplugin.Plugin.getPluginManager()
            .getChannelDayProgram(date, channels[i]);
        if (iter != null) {
          while (iter.hasNext()) {
            Program program = iter.next();
            // checking the program automatically leads to adding new awards
            hasAwards(program);
          }
        }
      }
      date = date.addDays(1);
    }
  }

}
