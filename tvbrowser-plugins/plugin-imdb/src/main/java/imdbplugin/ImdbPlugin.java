/*
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
package imdbplugin;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import tvbrowser.core.icontheme.IconLoader;
import util.misc.SoftReferenceCache;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;
import devplugin.ProgramRatingIf;
import devplugin.SettingsTab;
import devplugin.Version;

public final class ImdbPlugin extends Plugin {

  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbPlugin.class);

  private static final boolean IS_STABLE = true;

  private static final Version mVersion = new Version(1, 5, 0, IS_STABLE);

  // Empty Rating for Cache
  private static final ImdbRating DUMMY_RATING = new ImdbRating(0, 0, "", "");
  private static final ImdbRating EXAMPLE_RATING = new ImdbRating(70, 123, "123456789", null);

  private static ImdbPlugin instance;

  private PluginInfo mPluginInfo;
  private ImdbDatabase mImdbDatabase;
  private SoftReferenceCache<String, ImdbRating> mRatingCache = new SoftReferenceCache<String, ImdbRating>();
  private ImdbSettings mSettings;
  private boolean mStartFinished = false;
  private ArrayList<Channel> mExcludedChannels = new ArrayList<Channel>();

  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private ImdbHistogram mHistogram;

  @Override
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("pluginName", "Imdb Ratings");
      final String desc = mLocalizer.msg("description",
          "Display IMDb ratings of programs");
      final String author = "TV-Browser Team";

      mPluginInfo = new PluginInfo(ImdbPlugin.class, name, desc, author,
          "GPL 3");
    }

    return mPluginInfo;
  }

  public static Version getVersion() {
    return mVersion;
  }

  @Override
  public Icon[] getProgramTableIcons(final Program program) {
    final ImdbRating rating = getRatingFor(program);
    if (rating == null) {
      return null;
    }

    return new Icon[]{new ImdbIcon(rating)};
  }

  /**
   * get rating for a program
   * @param program
   * @return
   */
  ImdbRating getRatingFor(final Program program) {
    // have extra rating for example program to avoid full database initialization when only getting the plugin icon
    if (Plugin.getPluginManager().getExampleProgram().equals(program)) {
      return EXAMPLE_RATING;
    }
    if (mImdbDatabase == null) {
    	return null;
    }
    // synchronized because this method is exposed to other plugins via rating interface
    synchronized (this) {
      ImdbRating rating = null;
      if (!mExcludedChannels.contains(program.getChannel())) {
        final String cacheKey = getCacheKey(program);
        rating = mRatingCache.get(cacheKey);
        if (rating == null) {
          rating = getEpisodeRating(program);
          if (rating == null) {
            rating = getProgramRating(program);
          }
          if (rating != null) {
            mRatingCache.put(cacheKey, rating);
          } else {
            mRatingCache.put(cacheKey, DUMMY_RATING);
          }
        }

        if (rating == DUMMY_RATING) {
          rating = null;
        }
      }
      return rating;
    }
  }

  /**
   * @param program
   * @return
   */
  private String getCacheKey(final Program program) {
    final StringBuilder builder = new StringBuilder(program.getTitle());
    builder.append('~');
    final String episode = program.getTextField(ProgramFieldType.EPISODE_TYPE);
    if (episode != null) {
      builder.append(episode);
    }
    builder.append('~');
    builder.append(program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE));
    return builder.toString();
  }

  public ImdbRating getEpisodeRating(final Program program) {
    return mImdbDatabase.getRatingForId(mImdbDatabase.getMovieEpisodeId(program
        .getTitle(), program.getTextField(ProgramFieldType.EPISODE_TYPE),
        program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE),
        program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE), program
            .getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE)));
  }

  public ImdbRating getProgramRating(final Program program) {
    return mImdbDatabase.getRatingForId(mImdbDatabase.getMovieId(program.getTitle(), "",
        program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE),
        program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE),
        program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE)));
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    ImdbRating rating = getRatingFor(program);
    if (rating == null
        && getPluginManager().getExampleProgram().equals(program)) {
    	rating = EXAMPLE_RATING;
    }
    if (rating != null) {
      final AbstractAction action = new AbstractAction() {
        public void actionPerformed(final ActionEvent evt) {
          showRatingDialog(program);
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("contextMenuDetails",
          "Details for the IMDb rating ({0})", rating.getRatingText()));
      action.putValue(Action.SMALL_ICON, new ImdbIcon(rating));
      return new ActionMenu(action);
    }
    return null;
  }

  private void showRatingDialog(final Program program) {
    Window dlgParent = UiUtilities.getBestDialogParent(getParentFrame());
		ImdbRatingsDialog dialog;
		if (dlgParent instanceof JDialog) {
			dialog = new ImdbRatingsDialog((JDialog) dlgParent, program);
		}
		else {
			dialog = new ImdbRatingsDialog((JFrame) dlgParent, program);
		}
    UiUtilities.centerAndShow(dialog);
  }

  @Override
  public String getProgramTableIconText() {
    return mLocalizer.msg("iconText", "Imdb Rating");
  }

  @Override
  public void handleTvBrowserStartFinished() {
    initializeDatabase();
    if (mImdbDatabase.isInitialised() && !mSettings.isDatabaseCurrentVersion()) {
      SwingUtilities.invokeLater(new Runnable(){
        public void run() {
        	String[] buttons = new String[] {mLocalizer.msg("buttonImport", "Import now"), mLocalizer.msg("buttonLater", "Later")};
					if (JOptionPane
							.showOptionDialog(
									getParentFrame(),
									mLocalizer
											.msg(
													"version.message",
													"Your local IMDb database must be imported again because the\ndatabase format has changed with this plugin version."),
									mLocalizer.msg("version.title", "Database upgrade"),
									JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
									null, buttons, buttons[0]) == JOptionPane.YES_OPTION) {
						showUpdateDialog();
					}
        }
      });
    }
    if (mSettings.askCreateDatabase() && !mImdbDatabase.isInitialised() && getPluginManager().getSubscribedChannels().length > 0) {
      SwingUtilities.invokeLater(new Runnable(){
        public void run() {
          final JCheckBox askAgain = new JCheckBox(mLocalizer.msg(
              "dontShowAgain", "Don't show this message again"));
          Object[] shownObjects = new Object[2];
          shownObjects[0] = mLocalizer.msg("downloadData", "No IMDb-Database available, should I download the ImDB-Data now (approx. {0} MB)? It will take around {1} MB on disk.", 20, 400);
          shownObjects[1] = askAgain;

          final int ret = JOptionPane.showConfirmDialog(getParentFrame(),
              shownObjects, mLocalizer.msg("downloadDataTitle",
                  "No data available"), JOptionPane.YES_NO_OPTION);

          if (askAgain.isSelected()) {
            mSettings.askCreateDatabase(true);
          }

          if (ret == JOptionPane.YES_OPTION) {
            showUpdateDialog();
          }
        }
      });
    }
    mStartFinished = true;
    // force an update of the plugin tree
    updateCurrentDateAndClearCache();
  }

  private void initializeDatabase() {
    try {
			if (mImdbDatabase == null) {
			  mImdbDatabase = new ImdbDatabase(new File(Plugin.getPluginManager()
			      .getTvBrowserSettings().getTvBrowserUserHome(), "imdbDatabase"));
			  mImdbDatabase.init();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
  }

  public void showUpdateDialog() {
    final JComboBox box = new JComboBox(new String[] { "ftp.fu-berlin.de",
        "ftp.funet.fi", "ftp.sunet.se" });
    Object[] shownObjects = new Object[2];
    shownObjects[0] = mLocalizer.msg("serverMsg", "Choose server:");
    shownObjects[1] = box;

    final int ret = JOptionPane.showConfirmDialog(getParentFrame(),
        shownObjects, mLocalizer.msg("serverTitle", "Choose Server"),
        JOptionPane.OK_CANCEL_OPTION);

    if (ret == JOptionPane.OK_OPTION) {
      String server = null;
      switch (box.getSelectedIndex()) {
        case 1 : server = "ftp://ftp.funet.fi/pub/mirrors/ftp.imdb.com/pub/";
                 break;
        case 2 : server = "ftp://ftp.sunet.se/pub/tv+movies/imdb/";
                 break;
        default:
        server = "ftp://ftp.fu-berlin.de/pub/misc/movies/database/";
      }
      final Window w = UiUtilities.getBestDialogParent(getParentFrame());

      ImdbUpdateDialog dialog = null;
      if (w instanceof JFrame) {
        dialog = new ImdbUpdateDialog(this, (JFrame) UiUtilities.getBestDialogParent(getParentFrame()), server, mImdbDatabase);
      } else {
        dialog = new ImdbUpdateDialog(this, (JDialog) UiUtilities.getBestDialogParent(getParentFrame()), server, mImdbDatabase);
      }

      UiUtilities.centerAndShow(dialog);
    }
  }

  @Override
  public void loadSettings(final Properties properties) {
    mSettings = new ImdbSettings(properties);
    mHistogram = new ImdbHistogram(mSettings);
  }

  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  @Override
  public void readData(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.readInt(); // version

    final int count = in.readInt();

    mExcludedChannels.clear();
    for (int i = 0;i< count;i++) {
      final Channel ch = Channel.readData(in, true);
      if (ch != null) {
        mExcludedChannels.add(ch);
      }
    }
  }

  @Override
  public void writeData(final ObjectOutputStream out) throws IOException {
    out.writeInt(1);

    out.writeInt(mExcludedChannels.size());
    for (Channel ch : mExcludedChannels) {
      ch.writeData(out);
    }

  }

  @Override
  public SettingsTab getSettingsTab() {
    return new ImdbSettingsTab((JFrame)getParentFrame(), this, mSettings);
  }

  /**
   * Force an update of the currently shown programs in the program table
   * where we need to add/update a rating.
   *
   * Internally called after a successful update of the imdb ratings database.
   */
  public void updateCurrentDateAndClearCache() {
    // don't update the UI if the rating updater runs on TV-Browser start
    if (!mStartFinished) {
      return;
    }
    mRatingCache.clear();
    final Map<String, RatingNode> nodes = new HashMap<String, RatingNode>();
    final Date currentDate = getPluginManager().getCurrentDate();
    final ProgramFilter filter = getPluginManager().getFilterManager()
        .getCurrentFilter();
    final Channel[] channels = getPluginManager().getSubscribedChannels();
    int minimumRating = mSettings.getMinimumRating();
    for (Channel channel : channels) {
      final Iterator<Program> iter = getPluginManager().getChannelDayProgram(currentDate, channel);
      if (null != iter) {
        while (iter.hasNext()) {
          final Program program = iter.next();
          final ImdbRating rating = getRatingFor(program);
          if (rating != null && rating != DUMMY_RATING && rating.getRating() > minimumRating) {
            final String key = rating.getRatingText() + program.getTitle();
            RatingNode ratingNode = nodes.get(key);
            if (ratingNode == null) {
              ratingNode = new RatingNode(rating, program);
              nodes.put(key, ratingNode);
            }
            ratingNode.addProgram(program);
          }
          if (filter.accept(program)) {
            program.validateMarking();
          }
        }
      }
    }
    final ArrayList<RatingNode> nodeList = new ArrayList<RatingNode>(nodes
        .size());
    nodeList.addAll(nodes.values());
    Collections.sort(nodeList, Collections.reverseOrder());
    mRootNode.clear();
    for (RatingNode node : nodeList) {
      mRootNode.add(node);
    }
    mRootNode.update();
  }

  public Channel[] getExcludedChannels() {
    return mExcludedChannels.toArray(new Channel[mExcludedChannels.size()]);
  }

  public void setExcludedChannels(final Channel[] excludedChannels) {
    mExcludedChannels = new ArrayList<Channel>(Arrays.asList(excludedChannels));
  }

  public ProgramRatingIf[] getRatingInterfaces() {
    return new ProgramRatingIf[] {new ProgramRatingIf() {

      public String getName() {
        return mLocalizer.msg("pluginName", "Imdb Ratings");
      }

      public Icon getIcon() {
        return new ImdbIcon(new ImdbRating(75, 100, "", ""));
      }

      public int getRatingForProgram(final Program p) {
        final ImdbRating rating = getRatingFor(p);
        if (rating != null) {
          return rating.getRating();
        }

        return -1;
      }

      public Icon getIconForProgram(final Program p) {
        final ImdbRating rating = getRatingFor(p);
        if (rating != null) {
          return new ImdbIcon(rating);
        }
        return null;
      }

      public boolean hasDetailsDialog() {
        return true;
      }

      public void showDetailsFor(final Program p) {
        showRatingDialog(p);
      }
    }};
  }

  @Override
  public void onActivation() {
    if (mStartFinished && (mImdbDatabase == null)) {
      initializeDatabase();
    }
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  public static ImdbPlugin getInstance() {
    return instance;
  }

  public ImdbPlugin() {
    super();
    instance = this;
  }

  public void setCurrentDatabaseVersion(int ratingCount) {
    mSettings.setCurrentDatabaseVersion();
    mSettings.setNumberOfMovies(ratingCount);
    mSettings.setUpdateDate(Date.getCurrentDate());
  }

  @Override
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return (Class<? extends PluginsFilterComponent>[]) new Class[] { ImdbFilterComponent.class, VoteCountFilterComponent.class};
  }

  public ImdbDatabase getDatabase() {
    return mImdbDatabase;
  }

  public String getDatabaseSizeMB() {
    return mImdbDatabase.getDatabaseSizeMB();
  }

  @Override
  public ActionMenu getButtonAction() {
  	AbstractAction action = new AbstractAction(mLocalizer.msg("download", "Download new IMDb data")) {

			public void actionPerformed(ActionEvent e) {
				showUpdateDialog();
			}
		};
		// TODO: use constants for icon size after 3.0 release
		action.putValue(Action.SMALL_ICON, IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 16));
		action.putValue(BIG_ICON, IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 22));
		return new ActionMenu(action);
  }

  void storeHistogram(final ImdbHistogram histogram) {
    mHistogram = histogram;
    mSettings.setHistogram(mHistogram);
  }

  ImdbHistogram getHistogram() {
    return mHistogram;
  }
}
