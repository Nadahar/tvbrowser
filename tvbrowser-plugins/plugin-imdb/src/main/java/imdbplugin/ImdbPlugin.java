package imdbplugin;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

import util.misc.SoftReferenceCache;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;
import devplugin.ProgramRatingIf;
import devplugin.SettingsTab;
import devplugin.Version;

public class ImdbPlugin extends Plugin {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbPlugin.class);

  private static final Version mVersion = new Version(1, 0);

  // Empty Rating for Cache
  private static final ImdbRating DUMMY_RATING = new ImdbRating(0, 0, "", "");

  private PluginInfo mPluginInfo;
  private ImdbDatabase mImdbDatabase;
  private SoftReferenceCache<String, ImdbRating> mRatingCache = new SoftReferenceCache<String, ImdbRating>();
  private Properties mProperties;
  private boolean mStartFinished = false;
  private ArrayList<Channel> mExcludedChannels = new ArrayList<Channel>();

  @Override
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      String name = mLocalizer.msg("pluginName", "Imdb Ratings");
      String desc = mLocalizer.msg("description", "Display Imdb ratings in programs");
      String author = "TV-Browser Team";

      mPluginInfo = new PluginInfo(ImdbPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  public static Version getVersion() {
    return mVersion;
  }

  @Override
  public Icon[] getProgramTableIcons(Program program) {
    ImdbRating rating = getRatingFor(program);
    if (rating == null) {
      return null;
    }

    return new Icon[]{new ImdbIcon(rating)};
  }

  private ImdbRating getRatingFor(Program program) {
    ImdbRating rating = null;
    if (!mExcludedChannels.contains(program.getChannel())) {
      rating = mRatingCache.get(program.getID());
      if (rating == null) {
        rating = getEpisodeRating(program);
        if (rating == null) {
          rating = getProgramRating(program);
        }
        if (rating != null) {
          mRatingCache.put(program.getID(), rating);
        } else {
          mRatingCache.put(program.getID(), DUMMY_RATING);
        }
      }

      if (rating == DUMMY_RATING) {
        rating = null;
      }
    }

    return rating;
  }

  private ImdbRating getEpisodeRating(Program program) {
    return mImdbDatabase.getRatingForId(mImdbDatabase.getMovieEpisodeId(program
        .getTitle(), program.getTextField(ProgramFieldType.EPISODE_TYPE),
        program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE), program
            .getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE)));
  }

  private ImdbRating getProgramRating(Program program) {
    return mImdbDatabase.getRatingForId(mImdbDatabase.getMovieId(program
        .getTitle(), "", program
        .getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE)));
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    ImdbRating rating = getRatingFor(program);
    if (rating == null
        && getPluginManager().getExampleProgram().equals(program)) {
    	rating = new ImdbRating(75, 1000, "", "");
    }
    if (rating != null) {
      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
          showRatingDialog(program);
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("contextMenuDetails",
          "Details for the IMDb rating ({0})", new DecimalFormat("##.#")
              .format((double) rating.getRating() / 10)));
      action.putValue(Action.SMALL_ICON, new ImdbIcon(rating));
      return new ActionMenu(action);
    }
    return null;
  }

  private void showRatingDialog(Program program) {
    ImdbRating episodeRating = getEpisodeRating(program);
    ImdbRating rating = getProgramRating(program);
    StringBuffer message = new StringBuffer();
    if (rating != null || episodeRating != null) {
      if (episodeRating != null) {
        message.append(ratingMessage(program.getTitle() + " - "
            + program.getTextField(ProgramFieldType.EPISODE_TYPE),
            episodeRating));
      }
      if (rating != null) {
        if (message.length() > 0) {
          message.append("\n\n");
        }
        message.append(ratingMessage(program.getTitle(), rating));
      }
    }
    else {
      message.append(mLocalizer.msg("noRating", "No rating found!", program
          .getTitle()));
    }
    JOptionPane.showMessageDialog(UiUtilities
        .getBestDialogParent(getParentFrame()), message.toString());
  }

  private String ratingMessage(String title, ImdbRating rating) {
    return mLocalizer.msg("ratingFor", "Rating for \"{0}\":", title)
        + "\n"
        + mLocalizer.msg("rating", "Rating: {0}", new DecimalFormat("##.#")
            .format((double) rating.getRating() / 10)) + "\n"
        + mLocalizer.msg("votes", "Votes: {0}", rating.getVotes());
  }

  @Override
  public String getProgramTableIconText() {
    return mLocalizer.msg("iconText", "Imdb Rating");
  }

  @Override
  public void handleTvBrowserStartFinished() {
    initializeDatabase();
    if (!mProperties.getProperty("dontAskCreateDatabase", "false").equals("true") && !mImdbDatabase.isInitialised()) {
      SwingUtilities.invokeLater(new Runnable(){
        public void run() {
          JCheckBox askAgain = new JCheckBox(mLocalizer.msg("dontShowAgain", "Don't show this message again"));
          Object[] shownObjects = new Object[2];
          shownObjects[0] = mLocalizer.msg("downloadData", "No IMDB-Database available, should I download the ImDB-Data now (aprox. 10MB) ?");
          shownObjects[1] = askAgain;

          int ret = JOptionPane.showConfirmDialog(getParentFrame(), shownObjects, mLocalizer.msg("downloadDataTitle","No data available"), JOptionPane.YES_NO_OPTION);

          if (askAgain.isSelected()) {
            mProperties.setProperty("dontAskCreateDatabase", "true");
          }

          if (ret == JOptionPane.YES_OPTION) {
            showUpdateDialog();
          }
        }
      });
    }
    mStartFinished = true;
  }

  private void initializeDatabase() {
    if (mImdbDatabase == null) {
      mImdbDatabase = new ImdbDatabase(new File(Plugin.getPluginManager()
          .getTvBrowserSettings().getTvBrowserUserHome(), "imdbDatabase"));
      mImdbDatabase.init();
    }
  }

  public void showUpdateDialog() {
    JComboBox box = new JComboBox(new String[] {"ftp.fu-berlin.de", "ftp.funet.fi", "ftp.sunet.se"});
    Object[] shownObjects = new Object[2];
    shownObjects[0] = mLocalizer.msg("serverMsg", "Choose server:");
    shownObjects[1] = box;

    int ret = JOptionPane.showConfirmDialog(getParentFrame(), shownObjects, mLocalizer.msg("serverTitle","Choose Server"), JOptionPane.OK_CANCEL_OPTION);

    if (ret == JOptionPane.OK_OPTION) {
      String server = null;
      switch (box.getSelectedIndex()) {
        case 0 : server = "ftp://ftp.fu-berlin.de/pub/misc/movies/database/";
                 break;
        case 1 : server = "ftp://ftp.funet.fi/pub/mirrors/ftp.imdb.com/pub/";
                 break;
        case 2 : server = "ftp://ftp.sunet.se/pub/tv+movies/imdb/";
                 break;
      }
      Window w = UiUtilities.getBestDialogParent(getParentFrame());

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
  public void loadSettings(Properties settings) {
    mProperties = settings;
  }

  @Override
  public Properties storeSettings() {
    return mProperties;
  }

  @Override
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); // version

    int count = in.readInt();

    mExcludedChannels.clear();
    for (int i = 0;i< count;i++) {
      Channel ch = Channel.readData(in, true);
      if (ch != null) {
        mExcludedChannels.add(ch);
      }
    }
  }

  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);

    out.writeInt(mExcludedChannels.size());
    for (Channel ch : mExcludedChannels) {
      ch.writeData(out);
    }

  }

  @Override
  public SettingsTab getSettingsTab() {
    return new ImdbSettings((JFrame)getParentFrame(), this);
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
    Date currentDate = getPluginManager().getCurrentDate();
    ProgramFilter filter = getPluginManager().getFilterManager()
        .getCurrentFilter();
    final Channel[] channels = getPluginManager().getSubscribedChannels();
    for (Channel channel : channels) {
      final Iterator<Program> iter = getPluginManager().getChannelDayProgram(currentDate, channel);
      if (null != iter) {
        while (iter.hasNext()) {
          Program program = iter.next();
          if (filter.accept(program)) {
            program.validateMarking();
          }
        }
      }
    }
  }

  public Channel[] getExcludedChannels() {
    return mExcludedChannels.toArray(new Channel[mExcludedChannels.size()]);
  }

  public void setExcludedChannels(Channel[] excludedChannels) {
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

      public int getRatingForProgram(Program p) {
        ImdbRating rating = getRatingFor(p);
        if (rating != null) {
          return rating.getRating();
        }

        return -1;
      }

      public Icon getIconForProgram(Program p) {
        ImdbRating rating = getRatingFor(p);
        if (rating != null) {
          return new ImdbIcon(rating);
        }
        return null;
      }

      public boolean hasDetailsDialog() {
        return true;
      }

      public void showDetailsFor(Program p) {
        showRatingDialog(p);
      }
    }};
  }

  @Override
  public void onActivation() {
    initializeDatabase();
  }
}
