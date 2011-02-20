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

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import tvdataservice.MutableChannelDayProgram;
import util.misc.SoftReferenceCache;
import util.misc.StringPool;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

final public class MovieAwardPlugin extends Plugin {
  /**
   * all known awards (for movies, documentaries and so on)
   */
  private static final String[] KNOWN_AWARDS = { "cannes", "deutscher_comedypreis", "grimme",
            "max_ophuels", "menschenrechtsfilmpreis", "oscars", "golden_globe" };
  /**
   * all known awards (for movies ONLY)
   */
  private static final String[] KNOWN_MOVIE_AWARDS = { "deutscher_filmpreis",
            "europeanmovieawards", "internationaler_literaturfilmpreis" };
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(MovieAwardPlugin.class);
  private static final Logger mLog = Logger.getLogger(MovieAwardPlugin.class.getName());
  private static final Version mVersion = new Version(0, 12, 0);

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

  private static MovieAwardPlugin mInstance;
  private PluginsProgramFilter mFilter;

  /**
   * Database of movies to reduce duplication
   */
  private MovieDatabase mMovieDatabase = new MovieDatabase();

  /**
   * disable graphical updates of root node during full award search
   */
  private boolean mUpdateRootEnabled;

  private MovieAwardSettings mSettings;

  /**
   * object to synchronize different threads
   */
  private Object mSynchronizationLock = new Object();

  public MovieAwardPlugin() {
    mInstance = this;
  }

  @Override
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("pluginName", "Movie Awards");
      final String desc = mLocalizer.msg("description", "Shows movie awards");
      final String author = "TV-Browser Team";

      mPluginInfo = new PluginInfo(MovieAwardPlugin.class, name, desc, author,
          "GPL 3");
    }

    return mPluginInfo;
  }

  public static Version getVersion() {
    return mVersion;
  }

  public void initDatabase() {
    // might be called multiple times
    synchronized (mSynchronizationLock ) {
      if (mMovieAwards == null) {
        MovieDataFactory.loadMovieDatabase(mMovieDatabase, getClass()
            .getResourceAsStream("data/moviedatabase.xml"));
        mMovieAwards = new ArrayList<MovieAward>();

        for (String awardName : KNOWN_AWARDS) {
        	if (mSettings.isAwardEnabled(awardName)) {
						MovieAward award = MovieDataFactory.loadMovieDataFromStream(
								getStream(awardName),
								mMovieDatabase);

						if (award != null) {
							mMovieAwards.add(award);
						} else {
							mLog.warning("Could not load award " + awardName);
						}
        	}
        }
        for (String awardName : KNOWN_MOVIE_AWARDS) {
        	if (mSettings.isAwardEnabled(awardName)) {
	          MovieAward award = MovieDataFactory.loadMovieDataFromStream(getStream(awardName),
	              new MovieAwardForMovies(mMovieDatabase));
	          if (award != null) {
	            mMovieAwards.add(award);
	          }
	          else {
	            mLog.warning("Could not load award " + awardName);
	          }
        	}
        }
      }
    }
  }

	private InputStream getStream(String awardName) {
		return getClass().getResourceAsStream("data/" + awardName + ".xml");
	}

  @Override
  public void onActivation() {
    if (mStartFinished) {
      handleTvBrowserStartFinished();
    }
    // initialize sub nodes to be able to store entries
    // but do not initialize root node, to avoid updates
    initializeSubNodes();
  }

	private void initializeSubNodes() {
		mDateNode = new PluginTreeNode(mLocalizer.msg("dateNode", "Datum"));
    mAwardNode = new PluginTreeNode(mLocalizer.msg("awardNode", "Award"));
    mAwardNode.setGroupingByDateEnabled(false);
	}

  @Override
  public Icon[] getProgramTableIcons(final Program program) {
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
      final AbstractAction action = new AbstractAction() {
        public void actionPerformed(final ActionEvent evt) {
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

  private void showAwardDialog(final Program program) {
    final Window window = UiUtilities.getLastModalChildOf(getParentFrame());

    final MovieAwardDialog dialog;

    if (window instanceof JDialog) {
      dialog = new MovieAwardDialog((JDialog)window, mMovieAwards, program);
    } else {
      dialog = new MovieAwardDialog((JFrame)window, mMovieAwards, program);
    }
    //Bolle Edit: layoutWindow
    layoutWindow("movieAwardDlg", dialog, new Dimension(500,450));
    //
    UiUtilities.registerForClosing(dialog);
    UiUtilities.centerAndShow(dialog);

  }

  public boolean hasAwards(final Program program) {
    try {
			// this can be called before startFinished due to the movie award filter
			if (mMovieAwards == null) {
			  initDatabase();
			}
			// no awards for very short programs
			final int length = program.getLength();
			if (length > 0 && length < 5) {
			  return false;
			}

			synchronized(mSynchronizationLock) {

			  // did we already check this program ?
			  if (mAwardCache.containsKey(program)) {
			    return mAwardCache.get(program);
			  }

        boolean hasAward = false;
			  for (MovieAward award : mMovieAwards) {
			    if (award.containsAwardFor(program)) {
			      addToPluginTree(program, award);
			      hasAward = true;
			    }
			  }
        mAwardCache.put(program, hasAward);
			  if (hasAward) {
			    return true;
			  }
			}

		} catch (Exception e) {
			// catch any exception and just don't show an award
			e.printStackTrace();
		}
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

  public int getMarkPriorityForProgram(Program p) {
    return Program.NO_MARK_PRIORITY;
  }

  @Override
  public PluginTreeNode getRootNode() {
    if (mRootNode == null) {
      mRootNode = new PluginTreeNode(this, false);
      mRootNode.getMutableTreeNode().setShowLeafCountEnabled(false);
      addPluginTreeActions();
      if (mStartFinished) {
        // update the tree as the plugin view has been switched on for the first
        // time after start
        new Thread(new Runnable() {
            public void run() {
                updateRootNode();
            }
        }).start();
      }
    }
    return mRootNode;
  }

  private void addPluginTreeActions() {
    mRootNode.removeAllActions();
    final ActionMenu displayBoth = new ActionMenu(new AbstractAction(mLocalizer
        .msg(
        "grouping.both", "By award and date")) {
      public void actionPerformed(final ActionEvent e) {
        mSettings.setNodeGroupingBoth();
        updateNodeGrouping();
      }
    }, mSettings.isGroupingByBoth());
    final ActionMenu displayAwards = new ActionMenu(new AbstractAction(
        mLocalizer
        .msg("grouping.award", "By award")) {
      public void actionPerformed(final ActionEvent e) {
        mSettings.setNodeGroupingAward();
        updateNodeGrouping();
      }
    }, mSettings.isGroupingByAward());
    final ActionMenu displayDate = new ActionMenu(new AbstractAction(mLocalizer
        .msg(
        "grouping.date", "By date")) {
      public void actionPerformed(final ActionEvent e) {
        mSettings.setNodeGroupingDate();
        updateNodeGrouping();
      }
    }, mSettings.isGroupingByDate());
    final ActionMenu[] groupActions = new ActionMenu[] { displayBoth,
        displayAwards,
        displayDate };
    mRootNode.addActionMenu(new ActionMenu(new ContextMenuAction(mLocalizer
        .msg("grouping.grouping", "Grouping")), groupActions));
  }

  void updateNodeGrouping() {
    // clear children and reset root node
    if (mRootNode != null) {
      mRootNode.clear();
      addPluginTreeActions();
    }
    updateRootNode();
  }

  private void addToPluginTree(final Program program, final MovieAward award) {
    mDateNode.addProgram(program);
    PluginTreeNode node = mAwardNodes.get(award);
    if (node == null) {
      node = new PluginTreeNode(award.getName());
      mAwardNodes.put(award, node);
      mAwardNode.add(node);
    }
    node.addProgram(program);
    // defer update until tree is initialized
    if (mRootNode != null && mUpdateRootEnabled) {
      mRootNode.update();
    }
  }

  @Override
  public void handleTvBrowserStartFinished() {
    initDatabase();
    updateRootNodeIfVisible();
  }

  /**
   * trigger an update of the plugin tree, but only if it is visible at all
   */
  private void updateRootNodeIfVisible() {
    mStartFinished = true;
    // update tree, but only if it is shown at all
    if (mRootNode != null) {
      updateRootNode();
    }
    // update program table
    final Channel[] channels = devplugin.Plugin.getPluginManager()
        .getSubscribedChannels();
    final Date date = getPluginManager().getCurrentDate();
    for (Channel channel : channels) {
      final Iterator<Program> iter = Plugin.getPluginManager()
          .getChannelDayProgram(date, channel);
      if (iter != null) {
        while (iter.hasNext()) {
          final Program program = iter.next();
          program.validateMarking();
        }
      }
    }
  }

  private void updateRootNode() {
    // do nothing if the tree is not visible or the main program still has work
    // to do
    if (!mStartFinished) {
      return;
    }
    // search all awards
    final Channel[] channels = devplugin.Plugin.getPluginManager()
        .getSubscribedChannels();
    Date date = Date.getCurrentDate();
    mUpdateRootEnabled = false;
    for (int days = 0; days < 30; days++) {
      for (Channel channel : channels) {
        final Iterator<Program> iter = Plugin.getPluginManager()
            .getChannelDayProgram(date, channel);
        if (iter != null) {
          while (iter.hasNext()) {
            final Program program = iter.next();
            // checking the program automatically leads to adding new awards
            hasAwards(program);
          }
        }
      }
      date = date.addDays(1);
    }
    mUpdateRootEnabled = true;
    // now insert the dangling sub nodes
    if (mRootNode.isEmpty()) {
      if (mSettings.isGroupingByBoth()) {
        mRootNode.add(mAwardNode);
        mRootNode.add(mDateNode);
      }
      else if (mSettings.isGroupingByAward()) {
        ArrayList<PluginTreeNode> nodes = new ArrayList<PluginTreeNode>(mAwardNodes.size());
        nodes.addAll(mAwardNodes.values());
        Collections.sort(nodes, new Comparator<PluginTreeNode>() {

          public int compare(PluginTreeNode o1, PluginTreeNode o2) {
            return o1.toString().compareTo(o2.toString());
          }
        });
        for (PluginTreeNode award : nodes) {
          mRootNode.add(award);
        }
      } else if (mSettings.isGroupingByDate()) {
        mRootNode.addPrograms(Arrays.asList(mDateNode.getPrograms()));
      }
    }
    mRootNode.update();
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new MovieAwardSettingsTab(this, mSettings);
  }

  public List<MovieAward> getMovieAwards() {
    return mMovieAwards;
  }

  @SuppressWarnings("unchecked")
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return (Class<? extends PluginsFilterComponent>[]) new Class[] {MovieAwardFilterComponent.class, SelectedAwardsFilterComponent.class};
  }

  @Override
  public PluginsProgramFilter[] getAvailableFilter() {
    if (mFilter == null) {
      mFilter = new PluginsProgramFilter(this) {
        public String getSubName() {
          return "";
        }

        public boolean accept(final Program prog) {
         return hasAwards(prog);
        }
      };
    }

    return new PluginsProgramFilter[] {mFilter};
  }

  public static MovieAwardPlugin getInstance() {
    return mInstance;
  }

  @Override
  public void handleTvDataAdded(final MutableChannelDayProgram newProg) {
    // do nothing, we use handleTvDataUpdateFinished instead
  }

  @Override
  synchronized public void handleTvDataDeleted(final ChannelDayProgram oldProg) {
    // handle deletion independent of updateFinished as this may be called
    // outside data update
    if (oldProg == null) {
      return;
    }
    boolean updateNeeded = false;
    final Iterator<Program> iter = oldProg.getPrograms();
    if (iter != null) {
      while (iter.hasNext()) {
        final Program program = iter.next();
        // do not use program.getTitle() as the title field in the ondemand file
        // can already be deleted at this time
        if (mAwardCache.containsKey(program) && mDateNode.contains(program)) {
          mDateNode.removeProgram(program);
          updateNeeded = true;
          for (MovieAward award : mMovieAwards) {
            final PluginTreeNode node = mAwardNodes.get(award);
            if (node != null) {
              node.removeProgram(program);
            }
          }
          mAwardCache.remove(program);
        }
      }
    }
    // now refresh the tree
    if (updateNeeded && mRootNode != null) {
      mRootNode.update();
    }
  }

  @Override
  public void handleTvDataUpdateFinished() {
    updateRootNodeIfVisible();
  }

  @Override
  public void loadSettings(final Properties properties) {
    mSettings = new MovieAwardSettings(properties);
  }

  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

	public static String poolString(String input) {
		//TODO: after 3.0 release use an own string pool
		return StringPool.getString(input);
	}

	public static String[] getAvailableAwards() {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(Arrays.asList(KNOWN_AWARDS));
		list.addAll(Arrays.asList(KNOWN_MOVIE_AWARDS));
		return list.toArray(new String[list.size()]);
	}

	public static String getNameOfAward(String award) {
		return MovieDataFactory.getAwardName(MovieAwardPlugin.getInstance().getStream(award), award);
	}

	public MovieAwardSettings getSettings() {
		return mSettings;
	}

	public void reloadAwards() {
		mMovieAwards = null;
		if (mRootNode != null) {
			mRootNode.clear();
		}
		mAwardCache.clear();
		initializeSubNodes();
		handleTvBrowserStartFinished();
	}

}
