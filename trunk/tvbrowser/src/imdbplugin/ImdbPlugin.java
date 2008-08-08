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
        rating = mImdbDatabase.getRatingForId(mImdbDatabase.getMovieId(program.getTitle(), "", -1));
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

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    ImdbRating rating = getRatingFor(program);
    if (getPluginManager().getExampleProgram().equals(program)) {
    	rating = new ImdbRating(75, 1000, "", "");
    }
    if (rating != null) {
      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
          showRatingDialog(program);
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("contextMenuDetails", "Details zur Imdb-Bewertung ({0})",  new DecimalFormat("##.#").format((double)rating.getRating() / 10)));
      action.putValue(Action.SMALL_ICON, new ImdbIcon(rating));
      return new ActionMenu(action);
    }
    return null;
  }

  private void showRatingDialog(Program prg) {
    ImdbRating rating = getRatingFor(prg);
    if (rating != null) {
      JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(getParentFrame()),
              "Rating for " + prg.getTitle() + ":\n" +
              "Rating : " +  new DecimalFormat("##.#").format((double)rating.getRating() / 10) + "\n" +
              "Votes : " + rating.getVotes()
      );
    } else {
      JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(getParentFrame()), "No rating found!");
    }
  }

  @Override
  public String getProgramTableIconText() {
    return mLocalizer.msg("iconText", "Imdb Rating");
  }

  @Override
  public void handleTvBrowserStartFinished() {
    mImdbDatabase = new ImdbDatabase(new File(Plugin.getPluginManager()
        .getTvBrowserSettings().getTvBrowserUserHome(), "imdbDatabase"));
    mImdbDatabase.init();
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
    int version = in.readInt();

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
    // dont update the UI if the rating updater runs on TV-Browser start
    if (!mStartFinished) {
      return;
    }
    mRatingCache.clear();
    Date currentDate = getPluginManager().getCurrentDate();
    final Channel[] channels = getPluginManager().getSubscribedChannels();
    int i = 0;
    for (Channel channel : channels) {
      final Iterator<Program> iter = getPluginManager().getChannelDayProgram(currentDate, channel);
      if (null != iter) {
        while (iter.hasNext()) {
          Program prog = iter.next();
          prog.validateMarking();
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
}
